package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksIsoFipsCountryDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDataProcessingIntoKfsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;

import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorKeyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;

public class PaymentWorksNewVendorRequestsServiceImpl implements PaymentWorksNewVendorRequestsService {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorRequestsServiceImpl.class);

    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;
    protected PaymentWorksDataProcessingIntoKfsService paymentWorksDataProcessingIntoKfsService;
    protected PaymentWorksNewVendorRequestsReportService paymentWorksNewVendorRequestsReportService;
    protected PaymentWorksVendorDao paymentWorksVendorDao;
    protected PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;
    protected PaymentWorksIsoFipsCountryDao paymentWorksIsoFipsCountryDao;
    
    protected Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap = null; 
    protected Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap = null;
    
    @Override
    public void createKfsVendorsFromPmwNewVendorRequests() {
        LOG.info("createKfsVendorsFromPmwNewVendorRequests: was invoked");
        List<String> pmwNewVendorIdentifers = getPaymentWorksWebServiceCallsService().obtainPmwIdentifiersForPendingNewVendorRequests();
        if (pmwNewVendorIdentifers.isEmpty()) {
            LOG.info("createKfsVendorsFromPmwNewVendorRequests: No PENDING New Vendors found to process.");
            getPaymentWorksNewVendorRequestsReportService().sendEmailThatNoDataWasFoundToProcess();
        } 
        else {
            LOG.info("createKfsVendorsFromPmwNewVendorRequests: Found " + pmwNewVendorIdentifers.size() + " PENDING New Vendors to process.");
            processEachPaymentWorksNewVendorRequestIntoKFS(pmwNewVendorIdentifers);
        }
        LOG.info("createKfsVendorsFromPmwNewVendorRequests: leaving method");
    }
    
    private void processEachPaymentWorksNewVendorRequestIntoKFS(List<String> identifiersForPendingNewVendorsToProcess) {
        PaymentWorksNewVendorRequestsBatchReportData reportData = new PaymentWorksNewVendorRequestsBatchReportData();
        reportData.getPendingNewVendorsFoundInPmw().setRecordCount(identifiersForPendingNewVendorsToProcess.size());

        for (String pmwNewVendorRequestId : identifiersForPendingNewVendorsToProcess) {
            LOG.info("processEachPaymentWorksNewVendorRequestIntoKFS: Processing started for PMW-vendor-id=" + pmwNewVendorRequestId);
            PaymentWorksVendor stgNewVendorRequestDetailToProcess = getPaymentWorksWebServiceCallsService().obtainPmwNewVendorRequestDetailForPmwIdentifier(pmwNewVendorRequestId, reportData);
            if (canPaymentWorksNewVendorRequestProcessingContinueForVendor(stgNewVendorRequestDetailToProcess, reportData)) {
                PaymentWorksVendor savedStgNewVendorRequestDetailToProcess = pmwPendingNewVendorRequestInitialSaveToKfsStagingTable(stgNewVendorRequestDetailToProcess);
                if (pmwNewVendorSaveToStagingTableWasSuccessful(savedStgNewVendorRequestDetailToProcess, reportData)) {
                    if (pmwNewVendorRequestProcessingIntoKfsWasSuccessful(savedStgNewVendorRequestDetailToProcess, reportData)) {
                        getPaymentWorksWebServiceCallsService().sendApprovedStatusToPaymentWorksForNewVendor(savedStgNewVendorRequestDetailToProcess.getPmwVendorRequestId());
                    }
                    else {
                        getPaymentWorksWebServiceCallsService().sendRejectedStatusToPaymentWorksForNewVendor(savedStgNewVendorRequestDetailToProcess.getPmwVendorRequestId());
                    }
                }
                else {
                    //save of pmw data to staging table failed for some reason, cannot process or track request
                    getPaymentWorksWebServiceCallsService().sendRejectedStatusToPaymentWorksForNewVendor(savedStgNewVendorRequestDetailToProcess.getPmwVendorRequestId());
                }
            }
            else {
              //either duplicate request or data issue exists preventing save to staging table
              getPaymentWorksWebServiceCallsService().sendRejectedStatusToPaymentWorksForNewVendor(stgNewVendorRequestDetailToProcess.getPmwVendorRequestId());
            }
        }
        getPaymentWorksNewVendorRequestsReportService().generateAndEmailProcessingReport(reportData);
    }
    
    private boolean pmwNewVendorRequestProcessingIntoKfsWasSuccessful(PaymentWorksVendor savedStgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (getPaymentWorksDataProcessingIntoKfsService().ableToCreateValidateRouteKfsVendor(savedStgNewVendorRequestDetailToProcess, getPaymentWorksIsoToFipsCountryMap(), getPaymentWorksToKfsDiversityMap(), reportData)) { 
            updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing(savedStgNewVendorRequestDetailToProcess);
            updatePmwNewVendorReportBasedOnSuccessfulCreateVendorProcessing(savedStgNewVendorRequestDetailToProcess, reportData);
            return true;
        }
        else {
            updatePmwNewVendorStagingTableBasedOnCreateVendorFailing(savedStgNewVendorRequestDetailToProcess);
            return false;
        }
    }
    
    private boolean canPaymentWorksNewVendorRequestProcessingContinueForVendor(PaymentWorksVendor stgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        List<String> errorMessages = new ArrayList<String>();
        if (pmwDtosCouldConvertCustomAttributesToPmwJavaClassAttributes(stgNewVendorRequestDetailToProcess) &&
            pmwNewVendorAttributesConformToKfsLengthsOrFormats(stgNewVendorRequestDetailToProcess, errorMessages) &&
            allPmwNewVendorIsoCountriesMapToSingleFipsCountry(stgNewVendorRequestDetailToProcess, errorMessages) &&
            pmwNewVendorIdentifierDoesNotExistInKfsStagingTable(stgNewVendorRequestDetailToProcess, errorMessages)){
            return true;
        }
        else {
            if (!errorMessages.isEmpty()){
                reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().incrementRecordCount();
                reportData.addPmwVendorsThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(stgNewVendorRequestDetailToProcess.toString(), errorMessages));
            }
            return false;
        }
    }
    
    private boolean pmwDtosCouldConvertCustomAttributesToPmwJavaClassAttributes(PaymentWorksVendor stgNewVendorRequestDetailToProcess) {
        if (stgNewVendorRequestDetailToProcess.isCustomFieldConversionErrors()) {
            return false;
        }
        else{
            return true;
        }
    }
    
    private boolean pmwNewVendorAttributesConformToKfsLengthsOrFormats(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = true;
        if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName()) &
            StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName()))  {
            int lastDelimiterFirstCombinedLength = stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName().length() + 
                                                   VendorConstants.NAME_DELIM.length() + 
                                                   stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName().length();
            
            if (lastDelimiterFirstCombinedLength > VendorConstants.MAX_VENDOR_NAME_LENGTH) {
              errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NEW_VENDOR_REQUEST_COMBINED_LEGAL_FIRST_LAST_NAME_TOO_LONG_FOR_KFS);
              allValidationPassed = false;
              LOG.info("pmwNewVendorAttributesConformToKfsAttributesMaxLengths: PMW Legal Last Name length is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName().length() +
                       " PMW Legal First Name length is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName().length() +
                       " which when combined with delimiters must be less that the KFS legal name max length of " + (VendorConstants.MAX_VENDOR_NAME_LENGTH - VendorConstants.NAME_DELIM.length()));
            }
        }
        else {
            if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName())) {
                if (stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName().length() > VendorConstants.MAX_VENDOR_NAME_LENGTH) {
                  errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NEW_VENDOR_REQUEST_LEGAL_NAME_TOO_LONG_FOR_KFS);
                  allValidationPassed = false;
                  LOG.info("pmwNewVendorAttributesConformToKfsAttributesMaxLengths: KFS legal name max length is " + VendorConstants.MAX_VENDOR_NAME_LENGTH +
                           " PMW legal name length received is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName().length());
                }
            }
            else {
                errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NEW_VENDOR_REQUEST_LEGAL_NAME_NULL_OR_BLANK);
                allValidationPassed = false;
            }
        }
        
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyW8W9())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.W8_W9_URL_IS_NULL_OR_BLANK);
            allValidationPassed = false;
        }
        
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTin())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.TAX_NUMBER_IS_NULL_OR_BLANK);
            allValidationPassed = false;
        }
        
        if (ObjectUtils.isNull(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxClassificationCode())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.TAX_NUMBER_TYPE_IS_NULL_OR_BLANK);
            allValidationPassed = false;
        }
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getMbeCertificationExpirationDate(), PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NYS_CERTIFIED_MINORTY_BUSINESS_DESCRIPTION, errorMessages);
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getWbeCertificationExpirationDate(), PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NYS_CERTIFIED_WOMAN_OWNED_BUSINESS_DESCRIPTION, errorMessages);
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getVeteranCertificationExpirationDate(), PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.NYS_CERTIFIED_DISABLED_VETERAN_BUSINESS_DESCRIPTION, errorMessages);
        
        return allValidationPassed;
    }
    
    private boolean enteredDateIsFormattedProperly(String dateToValidate, String dateDescriptionForErrorMessage, List<String> errorMessages) {
        boolean dateIsFormattedCorrectly = true;
        if (ObjectUtils.isNotNull(dateToValidate)) {
            if (!PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY.matcher(dateToValidate).matches()) {
                errorMessages.add((dateDescriptionForErrorMessage + PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.DATE_IS_NOT_FORMATTED_CORRECTLY + dateToValidate));
                dateIsFormattedCorrectly = false;
            }
        }
        return dateIsFormattedCorrectly;
    }

    private boolean allPmwNewVendorIsoCountriesMapToSingleFipsCountry(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        //WHEN FUNCTIONALITY ADDED FOR PO VENDORS NEED TO ADD THE FOLLOWING:
        //  NEED TO ADD METHOD "allOptionalIsoCountriesContainData" FOR PO VENDORS and check poCountry data value with those methods
        //  NEED TO ADD CALL TO METHOD "isoCountryMapsToSingleFipsCountry" for poCountry
        boolean allCountryChecksPassed = false;
        if (allRequiredIsoCountriesContainData(stgNewVendorRequestDetailToProcess, errorMessages) &&
            allRequiredFipsCountriesContainData(stgNewVendorRequestDetailToProcess, errorMessages) &&
            isoCountryCodeTranslatesToSingleFipsCountryCode(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxCountry(), errorMessages) &&
            isoCountryCodeTranslatesToSingleFipsCountryCode(stgNewVendorRequestDetailToProcess.getCorpAddressCountry(), errorMessages) &&
            isoCountryCodeTranslatesToSingleFipsCountryCode(stgNewVendorRequestDetailToProcess.getRemittanceAddressCountry(), errorMessages) &&
            isoCountryCodeTranslatesToSingleFipsCountryCode(stgNewVendorRequestDetailToProcess.getBankAddressCountry(), errorMessages)) {
            allCountryChecksPassed = true;
        }
        return allCountryChecksPassed;
    }
    
    private boolean allRequiredIsoCountriesContainData(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allRequiredAreNotBlank = true;
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxCountry())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.COUNTRY_OF_INCORPORATION_BLANK);
            allRequiredAreNotBlank = false;
        }
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getCorpAddressCountry())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.PRIMARY_ADDRESS_COUNTRY_BLANK);
            allRequiredAreNotBlank = false;
        }
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRemittanceAddressCountry())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.REMITTANCE_ADDRESS_COUNTRY_BLANK);
            allRequiredAreNotBlank = false;
        }
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getBankAddressCountry())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.BANK_ADDRESS_COUNTRY_BLANK);
            allRequiredAreNotBlank = false;
        }
        return allRequiredAreNotBlank;
    }
    
    private boolean allRequiredFipsCountriesContainData(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allRequiredAreNotBlank = true;
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxCountry())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.FIPS_TAX_COUNTRY_BLANK);
            allRequiredAreNotBlank = false;
        }
        return allRequiredAreNotBlank;
    }
    
    private boolean isoCountryCodeTranslatesToSingleFipsCountryCode(String isoCountryCode, List<String> errorMessages) {
        if (getPaymentWorksIsoToFipsCountryMap().containsKey(isoCountryCode)) {
            if((getPaymentWorksIsoToFipsCountryMap().get(isoCountryCode)).size() == 1) {
                return true;
            }
            else {
                errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.SINGLE_ISO_MAPS_TO_MULTIPLE_FIPS + isoCountryCode);
                return false;
            }
        }
        else {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBusinessRuleFailureMessages.ISO_COUNTRY_NOT_FOUND + isoCountryCode);
            return false;
        }
    }
    
    private boolean pmwNewVendorIdentifierDoesNotExistInKfsStagingTable(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        if (getPaymentWorksVendorDao().isExistingPaymentWorksVendor(stgNewVendorRequestDetailToProcess.getPmwVendorRequestId())) {
            errorMessages.add(PaymentWorksConstants.PaymentWorksBatchReportMessages.DUPLICATE_NEW_VENDOR_REQUEST_MESSAGE);
            return false;
        } 
        else {
            return true;
        }
    }
    
    private PaymentWorksVendor pmwPendingNewVendorRequestInitialSaveToKfsStagingTable(PaymentWorksVendor pmwVendor) {
        LOG.info("pmwPendingNewVendorRequestSavesToKfsStagingTableSuccessful: entered");
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PENDING.getText());
        pmwVendor.setPmwTransactionType(PaymentWorksConstants.PaymentWorksTransactionType.NEW_VENDOR);
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_REQUESTED);
        PaymentWorksVendor savedNewVendorRequestedDetail = null;
        savedNewVendorRequestedDetail = getPaymentWorksVendorDao().savePaymentWorksVendorToStagingTable(pmwVendor);
        return savedNewVendorRequestedDetail;
    }
    
    private void updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing(PaymentWorksVendor pmwVendor) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing: entered");
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.getText());
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_CREATED);
        pmwVendor.setKfsAchProcessingStatus(PaymentWorksConstants.KFSAchProcessingStatus.PENDING_PVEN);
        
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getPmwVendorRequestId(), 
                                                                                  pmwVendor.getPmwRequestStatus(), 
                                                                                  pmwVendor.getKfsVendorProcessingStatus(), 
                                                                                  pmwVendor.getKfsAchProcessingStatus(), 
                                                                                  pmwVendor.getKfsVendorDocumentNumber(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private void updatePmwNewVendorStagingTableBasedOnCreateVendorFailing(PaymentWorksVendor pmwVendor) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnCreateVendorFailing: entered");
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.REJECTED.getText());
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_REJECTED);
        
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getPmwVendorRequestId(), 
                                                                                  pmwVendor.getPmwRequestStatus(), 
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private boolean pmwNewVendorSaveToStagingTableWasSuccessful(PaymentWorksVendor pmwNewVendorRequestModifiedByOjbSave, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (ObjectUtils.isNotNull(pmwNewVendorRequestModifiedByOjbSave)) {
            return true;
        }
        else {
            reportData.getPendingPaymentWorksVendorsThatCouldNotBeProcessed().incrementRecordCount();
            reportData.addPmwVendorsThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwNewVendorRequestModifiedByOjbSave.toString(), PaymentWorksConstants.PaymentWorksBatchReportMessages.INITIAL_SAVE_TO_PMW_STAGING_TABLE_FAILED));
            return false;
        }
    }
    
    private void updatePmwNewVendorReportBasedOnSuccessfulCreateVendorProcessing(PaymentWorksVendor savedStgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        reportData.getPendingPaymentWorksVendorsProcessed().incrementRecordCount();
        reportData.getPmwVendorsProcessed().add(getPaymentWorksNewVendorRequestsReportService().createBatchReportVendorItem(savedStgNewVendorRequestDetailToProcess));
    }
    
    public PaymentWorksWebServiceCallsService getPaymentWorksWebServiceCallsService() {
        return paymentWorksWebServiceCallsService;
    }

    public void setPaymentWorksWebServiceCallsService(PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService) {
        this.paymentWorksWebServiceCallsService = paymentWorksWebServiceCallsService;
    }

    public PaymentWorksVendorDao getPaymentWorksVendorDao() {
        return paymentWorksVendorDao;
    }

    public void setPaymentWorksVendorDao(PaymentWorksVendorDao paymentWorksVendorDao) {
        this.paymentWorksVendorDao = paymentWorksVendorDao;
    }

    public PaymentWorksNewVendorRequestsReportService getPaymentWorksNewVendorRequestsReportService() {
        return paymentWorksNewVendorRequestsReportService;
    }

    public void setPaymentWorksNewVendorRequestsReportService(
            PaymentWorksNewVendorRequestsReportService paymentWorksNewVendorRequestsReportService) {
        this.paymentWorksNewVendorRequestsReportService = paymentWorksNewVendorRequestsReportService;
    }

    public PaymentWorksDataProcessingIntoKfsService getPaymentWorksDataProcessingIntoKfsService() {
        return paymentWorksDataProcessingIntoKfsService;
    }

    public void setPaymentWorksDataProcessingIntoKfsService(
            PaymentWorksDataProcessingIntoKfsService paymentWorksDataProcessingIntoKfsService) {
        this.paymentWorksDataProcessingIntoKfsService = paymentWorksDataProcessingIntoKfsService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
    
    public PaymentWorksIsoFipsCountryDao getPaymentWorksIsoFipsCountryDao() {
        return paymentWorksIsoFipsCountryDao;
    }

    public void setPaymentWorksIsoFipsCountryDao(PaymentWorksIsoFipsCountryDao paymentWorksIsoFipsCountryDao) {
        this.paymentWorksIsoFipsCountryDao = paymentWorksIsoFipsCountryDao;
    }

    public Map<String, List<PaymentWorksIsoFipsCountryItem>> getPaymentWorksIsoToFipsCountryMap() {
        if (ObjectUtils.isNull(paymentWorksIsoToFipsCountryMap)) {
            setPaymentWorksIsoToFipsCountryMap(getPaymentWorksIsoFipsCountryDao().buildIsoToFipsMapFromDatabase());
        }
        return paymentWorksIsoToFipsCountryMap;
    }

    public void setPaymentWorksIsoToFipsCountryMap(Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        this.paymentWorksIsoToFipsCountryMap = paymentWorksIsoToFipsCountryMap;
    }

    public Map<String, SupplierDiversity> getPaymentWorksToKfsDiversityMap() {
        if (ObjectUtils.isNull(paymentWorksToKfsDiversityMap)) {
            setPaymentWorksToKfsDiversityMap(getKfsSupplierDiversityDao().buildPmwToKfsSupplierDiversityMap());
        }
        return paymentWorksToKfsDiversityMap;
    }

    public void setPaymentWorksToKfsDiversityMap(Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        this.paymentWorksToKfsDiversityMap = paymentWorksToKfsDiversityMap;
    }

    public KfsSupplierDiversityDao getKfsSupplierDiversityDao() {
        return kfsSupplierDiversityDao;
    }

    public void setKfsSupplierDiversityDao(KfsSupplierDiversityDao kfsSupplierDiversityDao) {
        this.kfsSupplierDiversityDao = kfsSupplierDiversityDao;
    }

}
