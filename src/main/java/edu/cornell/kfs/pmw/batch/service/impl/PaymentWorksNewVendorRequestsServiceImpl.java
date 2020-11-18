package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.KfsSupplierDiversityDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksIsoFipsCountryDao;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportRawDataItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorRequestsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorDataProcessingIntoKfsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;

public class PaymentWorksNewVendorRequestsServiceImpl implements PaymentWorksNewVendorRequestsService {
    
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorRequestsServiceImpl.class);

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
    protected PaymentWorksFormModeService paymentWorksFormModeService;
    
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
        emailBodyItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_APPROVED_VENDORS_FOUND_EMAIL_BODY));
        List<String> emailSubjectItems = new ArrayList<String>();
        emailSubjectItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_NEW_VENDOR_REPORT_NO_APPROVED_VENDORS_FOUND_EMAIL_SUBJECT));
        getPaymentWorksNewVendorRequestsReportService().sendEmailThatNoDataWasFoundToProcess(emailSubjectItems, emailBodyItems);
    }
    
    private void processEachPaymentWorksNewVendorRequestIntoKFS(List<String> identifiersForPendingNewVendorsToProcess) {
        PaymentWorksNewVendorRequestsBatchReportData reportData = new PaymentWorksNewVendorRequestsBatchReportData();
        reportData.getRecordsFoundToProcessSummary().setRecordCount(identifiersForPendingNewVendorsToProcess.size());

        for (String pmwNewVendorRequestId : identifiersForPendingNewVendorsToProcess) {
            LOG.info("processEachPaymentWorksNewVendorRequestIntoKFS: Processing started for PMW-vendor-id=" + pmwNewVendorRequestId);
            PaymentWorksVendor stgNewVendorRequestDetailToProcess = getPaymentWorksWebServiceCallsService().obtainPmwNewVendorRequestDetailForPmwIdentifier(pmwNewVendorRequestId, reportData);
            if (canPaymentWorksNewVendorRequestProcessingContinueForVendor(stgNewVendorRequestDetailToProcess, reportData, pmwNewVendorRequestId)) {
                PaymentWorksVendor savedStgNewVendorRequestDetailToProcess = null;
                String stagingTableSaveExceptionMessageForUser = KFSConstants.BLANK_SPACE;
                try {
                    savedStgNewVendorRequestDetailToProcess = pmwPendingNewVendorRequestInitialSaveToKfsStagingTable(stgNewVendorRequestDetailToProcess);
                } catch (Exception e) {
                    LOG.error("processEachPaymentWorksNewVendorRequestIntoKFS: Caught Exception attempting PMW vendor save to KFS staging table, unable to save PaymentWorks vendor for pmwNewVendorRequestId: " + pmwNewVendorRequestId, e);
                    stagingTableSaveExceptionMessageForUser = parseSqlExceptionStringForOracleError(e.toString());
                }
                if (pmwNewVendorSaveToStagingTableWasSuccessful(savedStgNewVendorRequestDetailToProcess, stgNewVendorRequestDetailToProcess, stagingTableSaveExceptionMessageForUser, reportData)) {
                    if (pmwNewVendorRequestProcessingIntoKfsWasSuccessful(savedStgNewVendorRequestDetailToProcess, reportData)) {
                        LOG.info("processEachPaymentWorksNewVendorRequestIntoKFS, successfully processed vendor for pmwNewVendorRequestId: " + pmwNewVendorRequestId);
                    } else {
                        LOG.error("processEachPaymentWorksNewVendorRequestIntoKFS, failed to create and route PVEN for pmwNewVendorRequestId: " + pmwNewVendorRequestId);
                    }
                } else {
                    LOG.error("processEachPaymentWorksNewVendorRequestIntoKFS, could not save vendor staging data for pmwNewVendorRequestId: " + pmwNewVendorRequestId);
                }
            } else {
                LOG.error("processEachPaymentWorksNewVendorRequestIntoKFS, vendor data cannot be processed for pmwNewVendorRequestId: " + pmwNewVendorRequestId);
            }
            /*
             * @todo re enable this before merging
             */
            //getPaymentWorksWebServiceCallsService().sendProcessedStatusToPaymentWorksForNewVendor(pmwNewVendorRequestId);
        }
        getPaymentWorksNewVendorRequestsReportService().generateAndEmailProcessingReport(reportData);
    }
    
    private boolean pmwNewVendorRequestProcessingIntoKfsWasSuccessful(PaymentWorksVendor savedStgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (getPaymentWorksVendorDataProcessingIntoKfsService().createValidateAndRouteOrSaveKFSVendor(savedStgNewVendorRequestDetailToProcess, getPaymentWorksIsoToFipsCountryMap(), getPaymentWorksToKfsDiversityMap(), reportData)) { 
            updatePmwNewVendorStagingTableBasedOnSuccessfulCreateVendorProcessing(savedStgNewVendorRequestDetailToProcess);
            updatePmwNewVendorReportBasedOnSuccessfulCreateVendorProcessing(savedStgNewVendorRequestDetailToProcess, reportData);
            return true;
        }
        else {
            updatePmwNewVendorStagingTableBasedOnCreateVendorFailing(savedStgNewVendorRequestDetailToProcess);
            return false;
        }
    }
    
    private boolean canPaymentWorksNewVendorRequestProcessingContinueForVendor(PaymentWorksVendor stgNewVendorRequestDetailToProcess, PaymentWorksNewVendorRequestsBatchReportData reportData, 
            String pmwNewVendorRequestId) {
        List<String> errorMessages = new ArrayList<String>();
        if (pmwNewVendorNotNull(stgNewVendorRequestDetailToProcess, errorMessages)
                && pmwDtosCouldConvertCustomAttributesToPmwJavaClassAttributes(stgNewVendorRequestDetailToProcess)
                && pmwNewVendorAttributesConformToKfsLengthsOrFormats(stgNewVendorRequestDetailToProcess, errorMessages)
                && pmwNewVendorIdentifierDoesNotExistInKfsStagingTable(stgNewVendorRequestDetailToProcess, errorMessages)) {
            return true;
        } else {
            if (!errorMessages.isEmpty()) {
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                String paymentWorksVendorString = ObjectUtils.isNotNull(stgNewVendorRequestDetailToProcess) ? stgNewVendorRequestDetailToProcess.toString() : 
                    "Could not retreive details from PaymentWorks for PaymentWorks vendor request ID " + pmwNewVendorRequestId;
                reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(paymentWorksVendorString, errorMessages));
            }
            return false;
        }
    }
    
    private boolean pmwNewVendorNotNull(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        if (ObjectUtils.isNotNull(stgNewVendorRequestDetailToProcess)) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.INITIAL_PAYMENT_WORKS_VENDOR_RETRIEVAL_ERROR));
            return false;
        }
    }
    
    private boolean pmwDtosCouldConvertCustomAttributesToPmwJavaClassAttributes(PaymentWorksVendor stgNewVendorRequestDetailToProcess) {
        return !stgNewVendorRequestDetailToProcess.isCustomFieldConversionErrors();
    }
    
    private boolean pmwNewVendorAttributesConformToKfsLengthsOrFormats(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        boolean allValidationPassed = legalNameWasEntered(stgNewVendorRequestDetailToProcess, errorMessages);
        
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
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_MINORTY_BUSINESS_DESCRIPTION), 
                PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY, errorMessages) 
                && allValidationPassed;
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getWbeCertificationExpirationDate(), 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_WOMAN_OWNED_BUSINESS_DESCRIPTION), 
                PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY, errorMessages) 
                && allValidationPassed;
        
        allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getVeteranCertificationExpirationDate(), 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_NYS_CERTIFIED_DISABLED_VETERAN_BUSINESS_DESCRIPTION), 
                PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY, errorMessages) 
                && allValidationPassed;
        
        if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
            if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getDateOfBirth())) {
                allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getDateOfBirth(), 
                        getConfigurationService().getPropertyValueAsString(
                                PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_DATE_OF_BIRTH_DESCRIPTION), 
                        PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_YYYY_SLASH_MM_SLASH_DD, errorMessages) 
                        && allValidationPassed;
            }
            if (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getW8SignedDate())) {
                allValidationPassed = enteredDateIsFormattedProperly(stgNewVendorRequestDetailToProcess.getW8SignedDate(), 
                        getConfigurationService().getPropertyValueAsString(
                                PaymentWorksKeyConstants.ERROR_W8_SIGNED_DATE_DESCRIPTION),
                        PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_YYYY_SLASH_MM_SLASH_DD, errorMessages) 
                        && allValidationPassed;
            }
        }
        
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
    
    private boolean legalNameWasEntered(PaymentWorksVendor stgNewVendorRequestDetailToProcess, List<String> errorMessages) {
        if ( (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalLastName()) &
              StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalFirstName()))
              || (StringUtils.isNotBlank(stgNewVendorRequestDetailToProcess.getRequestingCompanyLegalName())) ) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_LEGAL_NAME_NULL_OR_BLANK));
            return false;
        }
    }
    
    protected boolean enteredDateIsFormattedProperly(String dateToValidate, String dateDescriptionForErrorMessage, Pattern datePattern, List<String> errorMessages) {
        boolean dateIsFormattedCorrectly = true;
        if (ObjectUtils.isNotNull(dateToValidate)) {
            if (!datePattern.matcher(dateToValidate).matches()) {
                errorMessages.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_DATE_IS_NOT_FORMATTED_CORRECTLY), dateDescriptionForErrorMessage, dateToValidate));
                dateIsFormattedCorrectly = false;
            }
        }
        return dateIsFormattedCorrectly;
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
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private void updatePmwNewVendorStagingTableBasedOnCreateVendorFailing(PaymentWorksVendor pmwVendor) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnCreateVendorFailing: entered");
        pmwVendor = setStatusValuesForPaymentWorksProcessedNewVendorRejectedKfsVendor(pmwVendor);
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
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.PENDING_PAAT);
        return pmwVendor;
    }
    
    private PaymentWorksVendor setStatusValuesForPaymentWorksProcessedNewVendorRejectedKfsVendor(PaymentWorksVendor pmwVendor) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        pmwVendor.setKfsVendorProcessingStatus(PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_REJECTED);
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.INELIGIBLE_FOR_UPLOAD);
        return pmwVendor;
    }
    
    private boolean pmwNewVendorSaveToStagingTableWasSuccessful(PaymentWorksVendor pmwNewVendorRequestModifiedByOjbSave, 
            PaymentWorksVendor pmwNewVendorRequestReceivedFromWebServiceCall, String processingErrorMessage, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        if (ObjectUtils.isNotNull(pmwNewVendorRequestModifiedByOjbSave)) {
            return true;
        } else {
            reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
            reportData.addPmwVendorThatCouldNotBeProcessed(new PaymentWorksBatchReportRawDataItem(pmwNewVendorRequestReceivedFromWebServiceCall.toString(), 
                    MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.INITIAL_SAVE_TO_PMW_STAGING_TABLE_FAILED_ERROR_MESSAGE), processingErrorMessage)));
            return false;
        }
    }
    
    private String parseSqlExceptionStringForOracleError(String exceptionString) {
        int startPosition = exceptionString.indexOf(PaymentWorksConstants.PaymentWorksVendorStagingTableParseExceptionConstants.EXCEPTION_REASON_START_STRING);
        int endPosition = exceptionString.indexOf(PaymentWorksConstants.PaymentWorksVendorStagingTableParseExceptionConstants.EXCEPTION_REASON_END_CHAR, startPosition);
        String reasonForSqlException = PaymentWorksConstants.PaymentWorksVendorStagingTableParseExceptionConstants.DEFAULT_EXCEPTION_MESSAGE;
        
        if (requestedIndexWasFound(startPosition) && requestedIndexWasFound(endPosition)) {
            reasonForSqlException = exceptionString.substring(startPosition, (endPosition + 1));
        }
        return reasonForSqlException;
    }
    
    private boolean requestedIndexWasFound (int indexFound) {
        if (indexFound == -1) {
            return false;
        } else {
            return true;
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

    public void setPaymentWorksFormModeService(PaymentWorksFormModeService paymentWorksFormModeService) {
        this.paymentWorksFormModeService = paymentWorksFormModeService;
    }

}
