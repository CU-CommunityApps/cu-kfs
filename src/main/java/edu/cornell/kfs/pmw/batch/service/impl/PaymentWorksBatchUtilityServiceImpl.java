package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksBankAccountType;
import edu.cornell.kfs.pmw.batch.PaymentWorksDataTransformation;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsAchDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoCountryToFipsCountryAssociation;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class PaymentWorksBatchUtilityServiceImpl implements PaymentWorksBatchUtilityService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksBatchUtilityServiceImpl.class);

    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected PersonService personService;
    protected PaymentWorksVendorDao paymentWorksVendorDao;
    
    private Person systemUser = null;

    @Override
    public String retrievePaymentWorksParameterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(PaymentWorksConstants.PAYMENTWORKS_NAMESPACE_CODE, CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }
    
    @Override
    public boolean isPaymentWorksIntegrationProcessingEnabled() {
        return (retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PMW_INTEGRATION_IS_ACTIVE_IND).equalsIgnoreCase(KFSConstants.ParameterValues.YES));
    }
    
    @Override
    public String getFileContents(String fileName) {
        try {
            byte[] fileByteArray = LoadFileUtils.safelyLoadFileBytes(fileName);
            String formattedString = new String(fileByteArray);
            return formattedString;
        } catch (RuntimeException e) {
            LOG.error("getFileContents: unable to read the file.", e);
            return StringUtils.EMPTY;
        }
    }
    
    @Override 
    public KfsVendorDataWrapper createNoteRecordingAnyErrors(KfsVendorDataWrapper kfsVendorDataWrapper, String noteText, String noteErrorDescriptor) {
        Note newNote = createNoteWithErrorsLogged(noteText, noteErrorDescriptor);
        if (!ObjectUtils.isNull(newNote)) {
            kfsVendorDataWrapper.getVendorNotes().add(newNote);
        }
        return kfsVendorDataWrapper;
    }
    
    @Override 
    public KfsAchDataWrapper createNoteRecordingAnyErrors(KfsAchDataWrapper kfsAchDataWrapper, String noteText, String noteErrorDescriptor) {
        Note newNote = createNoteWithErrorsLogged(noteText, noteErrorDescriptor);
        if (!ObjectUtils.isNull(newNote)) {
            kfsAchDataWrapper.getPayeeAchAccountNotes().add(newNote);
        }
        return kfsAchDataWrapper;
    }
    
    private Note createNoteWithErrorsLogged(String noteText, String noteErrorDescriptor) {
        Note noteBeingCreated = null;
        if (noteTextSizeIsWithinLimit(noteText)) {
            noteBeingCreated = createNote(noteText);
        } else {
            LOG.error("createNoteWithErrorsLogged: The " + noteErrorDescriptor + " Note contained " + noteText.length() + "characters and it can only have " + PaymentWorksConstants.NOTE_TEXT_DEFAULT_MAX_LENGTH);
        }
        return noteBeingCreated;
    }
    
    private boolean noteTextSizeIsWithinLimit(String noteText) {
        return (noteTextIsNotBlank(noteText) && (noteText.length() <= PaymentWorksConstants.NOTE_TEXT_DEFAULT_MAX_LENGTH));
    }
    
    private boolean noteTextIsNotBlank(String noteText) {
        return (!StringUtils.isBlank(noteText));
    }
    
    private Note createNote(String noteText) {
        Note newNote = new Note();
        newNote.setNoteText(noteText);
        newNote.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
        newNote.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
        newNote.setNotePostedTimestampToCurrent();
        return newNote;
    }
    
    @Override
    public Person getSystemUser() {
        if (ObjectUtils.isNull(systemUser)) {
            setSystemUser(getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER));
        }
        return systemUser;
    }
    
    @Override
    public boolean foundExistingPaymentWorksVendorByKfsDocumentNumber(String kfsDocumentNumber) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_DOCUMENT_NUMBER, kfsDocumentNumber);
        return(getBusinessObjectService().countMatching(PaymentWorksVendor.class, fieldValues) == 1);
    }

    @Override
    public boolean foundExistingPaymentWorksVendorByPaymentWorksVendorId(String pmwVendorId) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.PMW_VENDOR_REQUEST_ID, pmwVendorId);
        return(getBusinessObjectService().countMatching(PaymentWorksVendor.class, fieldValues) == 1);
    }

    @Override
    public void registerKfsPvenApprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail) {
        PaymentWorksVendor pmwVendor = getPaymentWorksVendorByDocumentNumber(kfsVendorDocumentNumber);
        if (ObjectUtils.isNotNull(pmwVendor)) {
            LOG.info("registerKfsPvenApprovalForExistingPaymentWorksVendor: Approving PaymentWorks originating Vendor with table ID '" + pmwVendor.getId()
                     + "' and kfsVendorDocumentNumber '" + kfsVendorDocumentNumber + "' and pmwVendorRequestId '" + pmwVendor.getPmwVendorRequestId()
                     + "' and vendor number '");
            getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_APPROVED, vendorDetail.getVendorHeaderGeneratedIdentifier(),
                                                                                      vendorDetail.getVendorDetailAssignedIdentifier(), getDateTimeService().getCurrentTimestamp());
        } else {
            LOG.error("registerKfsPvenApprovalForExistingPaymentWorksVendor: PaymentWorks staging table retrieval by KFS document number '" + kfsVendorDocumentNumber + "' failed to find vendor to set KFS Approve status values.");
        }
    }

    @Override
    public void registerKfsPvenApprovalForKfsEnteredVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail) {
        if (isDomesticVendor(vendorDetail) && (isDisbursementVoucherVendor(vendorDetail) || isPurchaseOrderVendor(vendorDetail))) {
            PaymentWorksVendor pmwVendor = populateKfsEnteredPaymentWorksVendor(kfsVendorDocumentNumber, vendorDetail, PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_APPROVED);
            LOG.info("registerKfsPvenApprovalForKfsEnteredVendor: Approving KFS originating vendor '"
                     + vendorDetail.getVendorHeaderGeneratedIdentifier() + "-" + vendorDetail.getVendorDetailAssignedIdentifier() + "' with kfsVendorDocumentNumber '" + kfsVendorDocumentNumber + "'");
            PaymentWorksVendor savedPmwVendor = getBusinessObjectService().save(pmwVendor);
        } else {
            LOG.error("registerKfsPvenApprovalForKfsEnteredVendor: Vendor '" + vendorDetail.getVendorHeaderGeneratedIdentifier()
                      + "-" + vendorDetail.getVendorDetailAssignedIdentifier() + "' with KFS document number '" + kfsVendorDocumentNumber
                      + "' was NOT inserted into PaymentWorks table because of either it's KFS vendorForeignIndicator being '" + vendorDetail.getVendorHeader().getVendorForeignIndicator().booleanValue()
                      + "' or its KFS vendorTypeCode being '" + vendorDetail.getVendorHeader().getVendorTypeCode() + "'.");
        }
    }

    @Override
    public void registerKfsPvenDisapprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail) {
        PaymentWorksVendor pmwVendor = getPaymentWorksVendorByDocumentNumber(kfsVendorDocumentNumber);
        if (ObjectUtils.isNotNull(pmwVendor)) {
            LOG.info("registerKfsPvenDisapprovalForExistingPaymentWorksVendor: Disapproving PaymentWorks originating Vendor with table ID '" + pmwVendor.getId()
                     + "' and kfsVendorDocumentNumber '" + kfsVendorDocumentNumber + "' and pmwVendorRequestId '" + pmwVendor.getPmwVendorRequestId() + "'");
            getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_DISAPPROVED, vendorDetail.getVendorHeaderGeneratedIdentifier(),
                                                                                      vendorDetail.getVendorDetailAssignedIdentifier(), getDateTimeService().getCurrentTimestamp());
        } else {
            LOG.error("registerKfsPvenDisapprovalForExistingPaymentWorksVendor: Vendor retrieval by KFS document number '" + kfsVendorDocumentNumber
                    + "' did not find vendor with pmwVendorRequestId '" + pmwVendor.getPmwVendorRequestId() + "'.");
        }
    }

    @Override
    public void registerKfsPvenApprovalForKfsEditedVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail) {
        LOG.info("registerKfsPvenApprovalForKfsEditedVendor: Approval for Edited Vendor with KFS document number '" + kfsVendorDocumentNumber + "'  NOT inserted into PaymentWorks table. PMW Edit functionality not present yet.");
    }

    private PaymentWorksVendor getPaymentWorksVendorByDocumentNumber(String documentNumber) {
        Map criteria = new HashMap();
        criteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_DOCUMENT_NUMBER, documentNumber);
        Collection<PaymentWorksVendor> newVendors = getBusinessObjectService().findMatching(PaymentWorksVendor.class, criteria);
        if (ObjectUtils.isNull(newVendors) || newVendors.isEmpty()) {
            return null;
        } else {
            return newVendors.iterator().next();
        } 
    }
    
    private boolean isDisbursementVoucherVendor(VendorDetail vendorDetail) {
        return (StringUtils.equalsIgnoreCase(vendorDetail.getVendorHeader().getVendorTypeCode(), VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER));
    }
    
    private boolean isPurchaseOrderVendor(VendorDetail vendorDetail) {
        return (StringUtils.equalsIgnoreCase(vendorDetail.getVendorHeader().getVendorTypeCode(), VendorConstants.VendorTypes.PURCHASE_ORDER));
    }

    private boolean isDomesticVendor(VendorDetail vendorDetail) {
        LOG.info("isDomesticVendor: Vendor Foreign Indicator =  " + vendorDetail.getVendorHeader().getVendorForeignIndicator().booleanValue());
        return (!vendorDetail.getVendorHeader().getVendorForeignIndicator().booleanValue());
    }

    private PaymentWorksVendor populateKfsEnteredPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail, String kfsVendorProcessingStatus) {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.READY_FOR_UPLOAD);
        pmwVendor.setPmwTransactionType(PaymentWorksConstants.PaymentWorksTransactionType.KFS_ORIGINATING_VENDOR);
        pmwVendor.setKfsVendorProcessingStatus(kfsVendorProcessingStatus);
        pmwVendor.setKfsVendorDocumentNumber(kfsVendorDocumentNumber);
        pmwVendor.setProcessTimestamp(getDateTimeService().getCurrentTimestamp());
        
        pmwVendor.setKfsVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
        pmwVendor.setKfsVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());
        pmwVendor.setRequestingCompanyTin(vendorDetail.getVendorHeader().getVendorTaxNumber());
        populateVendorLegalName(pmwVendor, vendorDetail);
        populateRemitAddress(pmwVendor, vendorDetail);
        populateContactEmail(pmwVendor, vendorDetail);
        return pmwVendor;
    }
    
    private void populateVendorLegalName(PaymentWorksVendor pmwVendor, VendorDetail vendorDetail) {
        pmwVendor.setRequestingCompanyLegalName(
                PaymentWorksDataTransformation.formatVendorName(
                        vendorDetail.getVendorName(), vendorDetail.getVendorFirstName(), vendorDetail.getVendorLastName()));
    }

    private void populateRemitAddress(PaymentWorksVendor pmwVendor, VendorDetail vendorDetail) {
        List<VendorAddress> remitAddresses = findAllActiveRemitAddresses(vendorDetail.getVendorAddresses());
        if (ObjectUtils.isNotNull(remitAddresses) && remitAddresses.size() == 1) {
            VendorAddress remitAddress = remitAddresses.get(0);
            pmwVendor.setRemittanceAddressStreet1(remitAddress.getVendorLine1Address());
            pmwVendor.setRemittanceAddressStreet2(remitAddress.getVendorLine2Address());
            pmwVendor.setRemittanceAddressCity(remitAddress.getVendorCityName());
            pmwVendor.setRemittanceAddressState(remitAddress.getVendorStateCode());
            pmwVendor.setRemittanceAddressCountry(convertFipsCountryCodeToIsoCountryCode(remitAddress.getVendorCountryCode()));
            pmwVendor.setRemittanceAddressZipCode(remitAddress.getVendorZipCode());
        } else {
            LOG.error("populateRemitAddress: KFS Approved PVEN does not contain one and only one Active Remit Address for vendor " + vendorDetail.getVendorHeaderGeneratedIdentifier() + "-" + vendorDetail.getVendorDetailAssignedIdentifier());
        }
    }
    
    private List<VendorAddress> findAllActiveRemitAddresses(List<VendorAddress> addresses) {
        return (addresses.stream()
                         .filter(address -> address.getVendorAddressTypeCode().equalsIgnoreCase(VendorConstants.AddressTypes.REMIT))
                         .filter(address -> address.isActive())
                         .collect(Collectors.toList()));
    }
    
    private void populateContactEmail(PaymentWorksVendor pmwVendor, VendorDetail vendorDetail) {
        List<VendorContact> vendorInformationContacts = findAllActiveVendorInformationContacts(vendorDetail.getVendorContacts());
        if (ObjectUtils.isNotNull(vendorInformationContacts) && vendorInformationContacts.size() == 1) {
            VendorContact vendorContact = vendorInformationContacts.get(0);
            pmwVendor.setVendorInformationEmail(vendorContact.getVendorContactEmailAddress());
        } else {
            LOG.error("populateContactEmail: KFS Approved PVEN does not contain one and only one Active Vendor Information Contact for vendor " + vendorDetail.getVendorHeaderGeneratedIdentifier() + "-" + vendorDetail.getVendorDetailAssignedIdentifier());
        }
    }
    
    private List<VendorContact> findAllActiveVendorInformationContacts(List<VendorContact> contacts) {
        return (contacts.stream()
                        .filter(contact -> contact.getVendorContactTypeCode().equalsIgnoreCase(PaymentWorksConstants.KFSVendorContactTypes.VENDOR_INFORMATION_FORM))
                        .filter(contact -> contact.isActive())
                        .collect(Collectors.toList()));
    }
    
    public String convertFipsCountryCodeToIsoCountryCode(String fipsCountryCode) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(PaymentWorksPropertiesConstants.PaymentWorksIsoCountryToFipsCountryAssociation.FIPS_CNTRY_CD, fipsCountryCode);
        Collection<PaymentWorksIsoCountryToFipsCountryAssociation> fipsIsoCollection = getBusinessObjectService().findMatching(PaymentWorksIsoCountryToFipsCountryAssociation.class, fieldValues);
        if (fipsIsoCollection.isEmpty()) {
            return null;
        } else {
            PaymentWorksIsoCountryToFipsCountryAssociation fipsToIso = fipsIsoCollection.iterator().next();
            return fipsToIso.getIsoCountryCode();
        } 
    }
    
    public List<String> convertReportDataValidationErrors(Map<String, AutoPopulatingList<ErrorMessage>> kfsGlobalVariablesMessageMap) {
        List<String> reportDataErrors = new ArrayList<String>();
        ErrorMessage errorMessage = null;
        String errorText = KFSConstants.EMPTY_STRING;
        
        for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> errorEntry : kfsGlobalVariablesMessageMap.entrySet()) {
            Iterator<ErrorMessage> iterator = errorEntry.getValue().iterator();
            while (iterator.hasNext()) {
                errorMessage = iterator.next();
                errorText = getConfigurationService().getPropertyValueAsString(errorMessage.getErrorKey());
                errorText = MessageFormat.format(errorText, (Object[]) errorMessage.getMessageParameters());
                reportDataErrors.add(errorText);
                LOG.error("convertReportDataValidationErrors: errorKey: " + errorMessage.getErrorKey() + "  errorMessages:: " + errorText);
            }
        }
        return reportDataErrors;
    }
    
    public List<PaymentWorksBankAccountType> findAllPmwBankAccountTypesMatching(String pmwBankAccountTypeToVerify) {
        List<PaymentWorksBankAccountType> matchingValues = Arrays.stream(PaymentWorksConstants.PaymentWorksBankAccountType.values())
                                                                 .filter(pmwBankAccountType -> pmwBankAccountType.getPmwCode().equalsIgnoreCase(pmwBankAccountTypeToVerify))
                                                                 .collect(Collectors.toList());
        return matchingValues;
    }
    
    public void setSystemUser(Person systemUser) {
        this.systemUser = systemUser;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public PaymentWorksVendorDao getPaymentWorksVendorDao() {
        return paymentWorksVendorDao;
    }

    public void setPaymentWorksVendorDao(PaymentWorksVendorDao paymentWorksVendorDao) {
        this.paymentWorksVendorDao = paymentWorksVendorDao;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
