package edu.cornell.kfs.pmw.batch.service.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksIsoFipsCountryDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorDataProcessingIntoKfsService;
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

    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected DataDictionaryService dataDictionaryService;
    protected DateTimeService dateTimeService;
    protected KfsSupplierDiversityDao kfsSupplierDiversityDao;
    protected PaymentWorksVendorDataProcessingIntoKfsService paymentWorksVendorDataProcessingIntoKfsService;
    protected PaymentWorksNewVendorRequestsReportService paymentWorksNewVendorRequestsReportService;
    protected PaymentWorksVendorDao paymentWorksVendorDao;
    protected PaymentWorksWebServiceCallsService paymentWorksWebServiceCallsService;
    protected PaymentWorksIsoFipsCountryDao paymentWorksIsoFipsCountryDao;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    
    protected Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap = null; 
    protected Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap = null;
    

    @Override
    public void createKfsVendorsFromPmwNewVendorRequests() {
        LOG.info("createKfsVendorsFromPmwNewVendorRequests: was invoked");
        List<String> pmwNewVendorIdentifers = getPaymentWorksWebServiceCallsService().obtainPmwIdentifiersForApprovedNewVendorRequests();
        if (pmwNewVendorIdentifers.isEmpty()) {
            LOG.info("createKfsVendorsFromPmwNewVendorRequests: No PaymentWorks APPROVED New Vendors found to process.");
            sendEmailThatNoPmwDataWasFoundToCreateNewKfsVendors();
        } 
        else {
            LOG.info("createKfsVendorsFromPmwNewVendorRequests: Found " + pmwNewVendorIdentifers.size() + " PaymentWorks APPROVED New Vendors to process.");
            processEachPaymentWorksNewVendorRequestIntoKFS(pmwNewVendorIdentifers);
        }
        LOG.info("createKfsVendorsFromPmwNewVendorRequests: leaving method");
    }
    
    private void sendEmailThatNoPmwDataWasFoundToCreateNewKfsVendors() {
        List<String> emailBodyItems = new ArrayList<String>();
        emailBodyItems.add(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_APPROVED_VENDORS_FOUND_EMAIL_BODY);
        List<String> emailSubjectItems = new ArrayList<String>();
        emailSubjectItems.add(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_APPROVED_VENDORS_FOUND_EMAIL_SUBJECT);
        getPaymentWorksNewVendorRequestsReportService().sendEmailThatNoDataWasFoundToProcess(emailSubjectItems, emailBodyItems);
    }
    
    private void processEachPaymentWorksNewVendorRequestIntoKFS(List<String> identifiersForPendingNewVendorsToProcess) {
        PaymentWorksNewVendorRequestsBatchReportData reportData = new PaymentWorksNewVendorRequestsBatchReportData();
        reportData.getRecordsFoundToProcessSummary().setRecordCount(identifiersForPendingNewVendorsToProcess.size());

        for (String pmwNewVendorRequestId : identifiersForPendingNewVendorsToProcess) {
            LOG.info("processEachPaymentWorksNewVendorRequestIntoKFS: Processing started for PMW-vendor-id=" + pmwNewVendorRequestId);
            PaymentWorksVendor stgNewVendorRequestDetailToProcess = getPaymentWorksWebServiceCallsService().obtainPmwNewVendorRequestDetailForPmwIdentifier(pmwNewVendorRequestId, reportData);
            if (canPaymentWorksNewVendorRequestProcessingContinueForVendor(stgNewVendorRequestDetailToProcess, reportData)) {
                PaymentWorksVendor savedStgNewVendorRequestDetailToProcess = pmwPendingNewVendorRequestInitialSaveToKfsStagingTable(stgNewVendorRequestDetailToProcess);
                if (pmwNewVendorSaveToStagingTableWasSuccessful(savedStgNewVendorRequestDetailToProcess, reportData)) {
                    if (pmwNewVendorRequestProcessingIntoKfsWasSuccessful(savedStgNewVendorRequestDetailToProcess, reportData)) {
                        getPaymentWorksWebServiceCallsService().sendProcessedStatusToPaymentWorksForNewVendor(savedStgNewVendorRequestDetailToProcess.getPmwVendorRequestId());
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
        if (getPaymentWorksVendorDataProcessingIntoKfsService().createValidateAndRouteKFSVendor(savedStgNewVendorRequestDetailToProcess, getPaymentWorksIsoToFipsCountryMap(), getPaymentWorksToKfsDiversityMap(), reportData)) { 
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
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(stgNewVendorRequestDetailToProcess.toString(), errorMessages));
            }
            return false;
        }
    }
    
    private boolean pmwDtosCouldConvertCustomAttributesToPmwJavaClassAttributes(PaymentWorksVendor stgNewVendorRequestDetailToProcess) {
        return !stgNewVendorRequestDetailToProcess.isCustomFieldConversionErrors();
    }
    
    private boolean pmwNewVendorAttributesConformToKfsLengthsOrFormats(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = enteredLegalNameConformsToKfsLength(stgNewVendorRequestDetailToProcess, errorMessages);
        
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyW8W9())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_W8_W9_URL_IS_NULL_OR_BLANK));
            allValidationPassed = false;
        }
        
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTin())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_TAX_NUMBER_IS_NULL_OR_BLANK));
            allValidationPassed = false;
        }
        
        if (ObjectUtils.isNull(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxClassificationCode())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_TAX_NUMBER_TYPE_IS_NULL_OR_BLANK));
            allValidationPassed = false;
        }
        
        allValidationPassed = validateVendorType(stgNewVendorRequestDetailToProcess.getVendorType(), errorMessages) && allValidationPassed;
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getMbeCertificationExpirationDate(), 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_MINORTY_BUSINESS_DESCRIPTION), errorMessages) 
                && allValidationPassed;
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getWbeCertificationExpirationDate(), 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_WOMAN_OWNED_BUSINESS_DESCRIPTION), errorMessages) 
                && allValidationPassed;
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getVeteranCertificationExpirationDate(), 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_DISABLED_VETERAN_BUSINESS_DESCRIPTION), errorMessages) 
                && allValidationPassed;
        
        return allValidationPassed;
    }
    
    private boolean validateVendorType(String vendorType, List<String> errorMessages) {
        boolean valid = true;
        if (StringUtils.isBlank(vendorType) || StringUtils.equalsIgnoreCase(vendorType, PaymentWorksConstants.NULL_STRING)) {
            valid = false;
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_VENDOR_TYPE_EMPTY));
        }
        return valid;
    }
    
    private boolean enteredLegalNameConformsToKfsLength(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = true;
        if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName()) &
            StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName()))  {
            allValidationPassed = combinedLegalFirstLastNameEnteredConformsToMaxKfsLength(stgNewVendorRequestDetailToProcess, errorMessages);
        }
        else {
            if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName())) {
                allValidationPassed = companyLegalNameEnteredConformsToMaxKfsLength(stgNewVendorRequestDetailToProcess, errorMessages);
            }
            else {
                errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_LEGAL_NAME_NULL_OR_BLANK));
                allValidationPassed = false;
            }
        }
        return allValidationPassed;
    }
    
    private boolean combinedLegalFirstLastNameEnteredConformsToMaxKfsLength(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = true;
        int lastDelimiterFirstCombinedLength = stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName().length() + 
                                               VendorConstants.NAME_DELIM.length() + 
                                               stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName().length();

        if (lastDelimiterFirstCombinedLength > VendorConstants.MAX_VENDOR_NAME_LENGTH) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_COMBINED_LEGAL_FIRST_LAST_NAME_TOO_LONG_FOR_KFS));
            allValidationPassed = false;
            LOG.info("isCombinedLegalFirstLastNameEnteredWithinProperLength: PMW Legal Last Name length is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName().length() +
                     " PMW Legal First Name length is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName().length() +
                     " which when combined with delimiters must be less that the KFS legal name max length of " + (VendorConstants.MAX_VENDOR_NAME_LENGTH - VendorConstants.NAME_DELIM.length()));
        }
        return allValidationPassed;
    }
    
    private boolean companyLegalNameEnteredConformsToMaxKfsLength(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = true;
        if (stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName().length() > VendorConstants.MAX_VENDOR_NAME_LENGTH) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_LEGAL_NAME_TOO_LONG_FOR_KFS));
            allValidationPassed = false;
            LOG.info("companyLegalNameEnteredConformsToMaxKfsLength: KFS legal name max length is " + VendorConstants.MAX_VENDOR_NAME_LENGTH +
                     " PMW legal name length received is " + stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName().length());
        }
        return allValidationPassed;
    }
    
    private boolean enteredDateIsFormattedProperly(String dateToValidate, String dateDescriptionForErrorMessage, List<String> errorMessages) {
        boolean dateIsFormattedCorrectly = true;
        if (ObjectUtils.isNotNull(dateToValidate)) {
            if (!PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY.matcher(dateToValidate).matches()) {
                errorMessages.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_DATE_IS_NOT_FORMATTED_CORRECTLY), dateDescriptionForErrorMessage, dateToValidate));
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
            bankIsoCountryCodeIsNotProvidedOrTranslatesToSingleFipsCountryCode(stgNewVendorRequestDetailToProcess.getBankAddressCountry(), errorMessages)) {
            allCountryChecksPassed = true;
        }
        return allCountryChecksPassed;
    }
    
    private boolean allRequiredIsoCountriesContainData(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allRequiredAreNotBlank = true;
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxCountry())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_COUNTRY_OF_INCORPORATION_BLANK));
            allRequiredAreNotBlank = false;
        }
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getCorpAddressCountry())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_PRIMARY_ADDRESS_COUNTRY_BLANK));
            allRequiredAreNotBlank = false;
        }
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRemittanceAddressCountry())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_REMITTANCE_ADDRESS_COUNTRY_BLANK));
            allRequiredAreNotBlank = false;
        }
        return allRequiredAreNotBlank;
    }
    
    private boolean allRequiredFipsCountriesContainData(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allRequiredAreNotBlank = true;
        if (StringUtils.isBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyTaxCountry())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_FIPS_TAX_COUNTRY_BLANK));
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
                errorMessages.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_SINGLE_ISO_MAPS_TO_MULTIPLE_FIPS), isoCountryCode));
                return false;
            }
        }
        else {
            errorMessages.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_ISO_COUNTRY_NOT_FOUND), isoCountryCode));
            return false;
        }
    }
    
    private boolean bankIsoCountryCodeIsNotProvidedOrTranslatesToSingleFipsCountryCode(String isoCountryCode, List<String> errorMessages) {
        if (ObjectUtils.isNotNull(isoCountryCode)) {
            return isoCountryCodeTranslatesToSingleFipsCountryCode(isoCountryCode, errorMessages);
        } else {
            LOG.info("bankIsoCountryCodeTranslatesToSingleFipsCountryCodeWhenBankDataIsEntered: Bank Country could not be validated. No bank data provided.");
            return true;
        }
    }
    
    private boolean pmwNewVendorIdentifierDoesNotExistInKfsStagingTable(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        if (getPaymentWorksBatchUtilityService().foundExistingPaymentWorksVendorByPaymentWorksVendorId(stgNewVendorRequestDetailToProcess.getPmwVendorRequestId())) {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.DUPLICATE_NEW_VENDOR_REQUEST_ERROR_MESSAGE));
            return false;
        } 
        else {
            return true;
        }
    }
    
    private PaymentWorksVendor savePaymentWorksVendorToStagingTable(PaymentWorksVendor pmwVendorToSave) {
        pmwVendorToSave.setProcessTimestamp(getDateTimeService().getCurrentTimestamp());
        pmwVendorToSave = getBusinessObjectService().save(pmwVendorToSave);
        return pmwVendorToSave;
    }
    
    private PaymentWorksVendor pmwPendingNewVendorRequestInitialSaveToKfsStagingTable(PaymentWorksVendor pmwVendor) {
        LOG.info("pmwPendingNewVendorRequestSavesToKfsStagingTableSuccessful: entered");
        pmwVendor = setStatusValuesForPaymentWorksApprovedNewVendorRequestedInKfs(pmwVendor);
        PaymentWorksVendor savedNewVendorRequestedDetail = null;
        savedNewVendorRequestedDetail = savePaymentWorksVendorToStagingTable(pmwVendor);
        return savedNewVendorRequestedDetail;
    }
    
    private void updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing(PaymentWorksVendor pmwVendor) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing: entered");
        pmwVendor = setStatusValuesForPaymentWorksProcessedNewVendorKfsVendorCreatedAchPendingPven(pmwVendor);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(),
                                                                                  pmwVendor.getPmwRequestStatus(), 
                                                                                  pmwVendor.getKfsVendorProcessingStatus(), 
                                                                                  pmwVendor.getKfsAchProcessingStatus(), 
                                                                                  pmwVendor.getKfsVendorDocumentNumber(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private void updatePmwNewVendorStagingTableBasedOnCreateVendorFailing(PaymentWorksVendor pmwVendor) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnCreateVendorFailing: entered");
        pmwVendor = setStatusValuesForPaymentWorksRejectedNewVendorRejectedKfsVendor(pmwVendor);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(), 
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private PaymentWorksVendor setStatusValuesForPaymentWorksApprovedNewVendorRequestedInKfs(PaymentWorksVendor pmwVendor) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.getText());
        pmwVendor.setPmwTransactionType(PaymentWorksConstants.PaymentWorksTransactionType.NEW_VENDOR);
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_REQUESTED);
        return pmwVendor;
    }
    
    private PaymentWorksVendor setStatusValuesForPaymentWorksProcessedNewVendorKfsVendorCreatedAchPendingPven(PaymentWorksVendor pmwVendor) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_CREATED);
        pmwVendor.setKfsAchProcessingStatus(PaymentWorksConstants.KFSAchProcessingStatus.PENDING_PVEN);
        return pmwVendor;
    }
    
    private PaymentWorksVendor setStatusValuesForPaymentWorksRejectedNewVendorRejectedKfsVendor(PaymentWorksVendor pmwVendor) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.REJECTED.getText());
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_REJECTED);
        return pmwVendor;
    }
    
    private boolean pmwNewVendorSaveToStagingTableWasSuccessful(PaymentWorksVendor pmwNewVendorRequestModifiedByOjbSave, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (ObjectUtils.isNotNull(pmwNewVendorRequestModifiedByOjbSave)) {
            return true;
        }
        else {
            reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
            reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwNewVendorRequestModifiedByOjbSave.toString(), getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.INITIAL_SAVE_TO_PMW_STAGING_TABLE_FAILED_ERROR_MESSAGE)));
            return false;
        }
    }
    
    private void updatePmwNewVendorReportBasedOnSuccessfulCreateVendorProcessing(PaymentWorksVendor savedStgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        reportData.getRecordsProcessedSummary().incrementRecordCount();
        reportData.addRecordProcessed(getPaymentWorksNewVendorRequestsReportService().createBatchReportVendorItem(savedStgNewVendorRequestDetailToProcess));
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

    public PaymentWorksVendorDataProcessingIntoKfsService getPaymentWorksVendorDataProcessingIntoKfsService() {
        return paymentWorksVendorDataProcessingIntoKfsService;
    }

    public void setPaymentWorksVendorDataProcessingIntoKfsService(
            PaymentWorksVendorDataProcessingIntoKfsService paymentWorksVendorDataProcessingIntoKfsService) {
        this.paymentWorksVendorDataProcessingIntoKfsService = paymentWorksVendorDataProcessingIntoKfsService;
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

}
