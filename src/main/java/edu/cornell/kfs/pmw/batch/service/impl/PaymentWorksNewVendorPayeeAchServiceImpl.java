package edu.cornell.kfs.pmw.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksBankAccountType;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchReportService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksNewVendorPayeeAchService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorAchDataProcessingIntoKfsService;

public class PaymentWorksNewVendorPayeeAchServiceImpl implements PaymentWorksNewVendorPayeeAchService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksNewVendorPayeeAchServiceImpl.class);
    
    protected AchBankService achBankService;
    protected BusinessObjectService businessObjectService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected PaymentWorksNewVendorPayeeAchReportService paymentWorksNewVendorPayeeAchReportService;
    protected PaymentWorksVendorAchDataProcessingIntoKfsService paymentWorksVendorAchDataProcessingIntoKfsService;
    protected PaymentWorksVendorDao paymentWorksVendorDao;
    protected PaymentWorksFormModeService paymentWorksFormModeService;
    
    @Override
    public void processKfsPayeeAchAccountsForApprovedAndDisapprovedPmwNewVendors() {
        LOG.info("processKfsPayeeAchAccountsForApprovedAndDisapprovedPmwNewVendors : Was invoked.");
        PaymentWorksNewVendorPayeeAchBatchReportData reportData = new PaymentWorksNewVendorPayeeAchBatchReportData();
        boolean approvedPvenDataFoundToProcess = createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors(reportData);
        boolean disapprovedPvenDataFoundToProcess = manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors(reportData);
        if (approvedPvenDataFoundToProcess || disapprovedPvenDataFoundToProcess) {
            getPaymentWorksNewVendorPayeeAchReportService().generateAndEmailProcessingReport(reportData);
        } else {
            sendEmailThatNoPmwAchDataWithAppropriateKfsVendorStatusWasFoundToProcess();
        }
        LOG.info("processKfsPayeeAchAccountsForApprovedAndDisapprovedPmwNewVendors : Leaving method.");
    }
    
    private void sendEmailThatNoPmwAchDataWithAppropriateKfsVendorStatusWasFoundToProcess() {
        List<String> emailBodyItems = new ArrayList<String>();
        emailBodyItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_NO_KFS_APPROVED_VENDORS_FOUND_EMAIL_BODY));
        emailBodyItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_NO_KFS_DISAPPROVED_VENDORS_FOUND_EMAIL_BODY));
        List<String> emailSubjectItems = new ArrayList<String>();
        emailSubjectItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_NO_KFS_APPROVED_VENDORS_FOUND_EMAIL_SUBJECT));
        emailSubjectItems.add(getPaymentWorksBatchUtilityService().retrievePaymentWorksParameterValue(PaymentWorksParameterConstants.PAYMENTWORKS_PAYEE_ACH_REPORT_NO_KFS_DISAPPROVED_VENDORS_FOUND_EMAIL_SUBJECT));
        getPaymentWorksNewVendorPayeeAchReportService().sendEmailThatNoDataWasFoundToProcess(emailSubjectItems, emailBodyItems);
    }
    
    private boolean manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        LOG.info("manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors : Was invoked.");
        boolean disapprovedPvenDataFoundToProcess;
        List<PaymentWorksVendor> kfsDisapprovedPmwVendors = findAllKfsDisapprovedPmwNewVendorsWithUnprocessedPmwAchData();
        if (ObjectUtils.isNull(kfsDisapprovedPmwVendors) || kfsDisapprovedPmwVendors.isEmpty()) {
            LOG.info("manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors: No KFS Disapproved PMW New Vendors with outstanding ACH data found to process.");
            disapprovedPvenDataFoundToProcess = false;
        } 
        else {
            LOG.info("manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors: Found " + kfsDisapprovedPmwVendors.size() + " KFS Disapproved PMW New Vendors to attempt processing for.");
            for (int i=0; i < kfsDisapprovedPmwVendors.size(); i++) {
                LOG.info("manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors: PMW-Vendor-id=" + kfsDisapprovedPmwVendors.get(i).getId());
            }
            disapprovedPvenDataFoundToProcess = true;
            processEachPaymentWorksDisapprovedNewVendorWithAndWithoutAchData(kfsDisapprovedPmwVendors, reportData);
        }
        LOG.info("manageKfsPayeeAchDataForKfsDisapprovedPmwNewVendors : Leaving method.");
        return disapprovedPvenDataFoundToProcess;
    }

    private List<PaymentWorksVendor> findAllKfsDisapprovedPmwNewVendorsWithUnprocessedPmwAchData() {
        Map<String, String> searchCriteria = new HashMap<String, String>();
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.PMW_REQUEST_STATUS, PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.PMW_TRANSACTION_TYPE, PaymentWorksConstants.PaymentWorksTransactionType.NEW_VENDOR);
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_PROCESSING_STATUS, PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_DISAPPROVED);
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_ACH_PROCESSING_STATUS, PaymentWorksConstants.KFSAchProcessingStatus.PENDING_PVEN);
        List<PaymentWorksVendor> pmwVendorsList = (List<PaymentWorksVendor>)getBusinessObjectService().findMatching(PaymentWorksVendor.class, searchCriteria);
        return pmwVendorsList;
    }
    
    private void processEachPaymentWorksDisapprovedNewVendorWithAndWithoutAchData(List<PaymentWorksVendor> kfsDisapprovedPmwVendorsToProcess, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getDisapprovedVendorsSummary().setRecordCount(kfsDisapprovedPmwVendorsToProcess.size());
        for (PaymentWorksVendor pmwVendor : kfsDisapprovedPmwVendorsToProcess) {
            try {
                LOG.info("processEachPaymentWorksDisapprovedNewVendorWithAndWithoutAchData: Vendor was KFS Disapproved. ACH Processing NOT attempted for PMW-vendor-id =" 
                         + pmwVendor.getPmwVendorRequestId() + " KFS edoc# = " + pmwVendor.getKfsAchDocumentNumber());
                if (allPmwEnteredKfsPayeeAchAccountDataExists(pmwVendor)) {
                    performProcessingWhenAchAccountDataIsProvidedForDisapprovedKfsVendor(pmwVendor, reportData);
                } else {
                    performProcessingWhenAchAccountDataNotProvidedForDisapprovedKfsVendor(pmwVendor, reportData);
                }
            } catch (Exception ex) {
                LOG.error("processEachPaymentWorksDisapprovedNewVendorWithAndWithoutAchData: PMW Vendor ID " + pmwVendor.toString() + " generated Exception(s): ", ex);
                performProcessingWhenExceptionGeneratedForKfsDisapprovedVendorDuringAchCreation(pmwVendor, reportData);
            }
        }
    }
    
    private boolean createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors(PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        LOG.info("createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors : Was invoked.");
        boolean approvedDataFoundToProcess;
        List<PaymentWorksVendor> kfsApprovedPmwVendors = findAllKfsApprovedPmwNewVendorsWithUnprocessedPmwAchData();
        if (ObjectUtils.isNull(kfsApprovedPmwVendors) || kfsApprovedPmwVendors.isEmpty()) {
            LOG.info("createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors: No KFS Approved PMW New Vendors with outstanding ACH data found to process.");
            approvedDataFoundToProcess = false;
        } else {
            LOG.info("createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors: Found " + kfsApprovedPmwVendors.size() + " KFS Approved PMW New Vendors to attempt ACH processing for.");
            for (PaymentWorksVendor pmwVendor : kfsApprovedPmwVendors) {
                LOG.info("createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors: PMW-Vendor-id=" + pmwVendor.getId());
            }
            processEachPaymentWorksNewVendorPayeeAchIntoKFS(kfsApprovedPmwVendors, reportData);
            approvedDataFoundToProcess = true;
        }
        LOG.info("createKfsPayeeAchAccountsForKfsApprovedPmwNewVendors : Leaving method.");
        return approvedDataFoundToProcess;
    }
    
    private List<PaymentWorksVendor> findAllKfsApprovedPmwNewVendorsWithUnprocessedPmwAchData() {
        Map<String, String> searchCriteria = new HashMap<String, String>();
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.PMW_REQUEST_STATUS, PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.PMW_TRANSACTION_TYPE, PaymentWorksConstants.PaymentWorksTransactionType.NEW_VENDOR);
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_VENDOR_PROCESSING_STATUS, PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_APPROVED);
        searchCriteria.put(PaymentWorksPropertiesConstants.PaymentWorksVendor.KFS_ACH_PROCESSING_STATUS, PaymentWorksConstants.KFSAchProcessingStatus.PENDING_PVEN);
        List<PaymentWorksVendor> pmwVendorsWithUnprocessedAch = (List<PaymentWorksVendor>) getBusinessObjectService().findMatching(PaymentWorksVendor.class, searchCriteria);
        return pmwVendorsWithUnprocessedAch;
    }
    
    private void processEachPaymentWorksNewVendorPayeeAchIntoKFS(List<PaymentWorksVendor> pmwVendorPayeeAchsToProcess, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getRecordsFoundToProcessSummary().setRecordCount(pmwVendorPayeeAchsToProcess.size());
        for (PaymentWorksVendor pmwVendor : pmwVendorPayeeAchsToProcess) {
            try {
                LOG.info("processEachPaymentWorksNewVendorPayeeAchIntoKFS: ACH Processing being attempted for PMW-vendor-id =" + pmwVendor.getPmwVendorRequestId() 
                         + " KFS vendor number = " + pmwVendor.getKfsVendorHeaderGeneratedIdentifier() + "-" + pmwVendor.getKfsVendorDetailAssignedIdentifier());
                if (kfsVendorIdentifiersExist(pmwVendor, reportData)) {
                    if (allPmwEnteredKfsPayeeAchAccountDataExists(pmwVendor, reportData)) {
                        if (allKfsPayeeAchAccountDataIsValid(pmwVendor, reportData)) {
                            pmwVendor = setStatusValuesForPaymentWorksNewVendorAchRequestedInKfs(pmwVendor);
                            if (getPaymentWorksVendorAchDataProcessingIntoKfsService().createValidateAndRouteKfsPayeeAch(pmwVendor, reportData)) {
                                performProcessingForSuccessfulKfsPaatDocumentCreation(pmwVendor, reportData);
                            } else {
                                performProcessingForFailedKfsPaatDocumentCreation(pmwVendor, reportData);
                            }
                        } else {
                            performProcessingWhenInvalidAchAccountDataIsProvidedForApprovedKfsVendor(pmwVendor);
                        }
                    } else {
                        performProcessingWhenAchAccountDataNotProvidedForApprovedKfsVendor(pmwVendor);
                    }
                } else {
                    performProcessingWhenNoKfsVendorIdentifiersFoundForKfsApprovedVendor(pmwVendor);
                }
                
            } catch (Exception ex) {
                LOG.error("processEachPaymentWorksNewVendorPayeeAchIntoKFS: PMW Vendor ID " + pmwVendor.toString() + " generated Exception: " + ex.getStackTrace());
                performProcessingWhenExceptionGeneratedForKfsApprovedVendorDuringAchCreation(pmwVendor, reportData);
            }
        }
    }
    
    private boolean kfsVendorIdentifiersExist(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        List<String> errorMessages = new ArrayList<String>();
        if (vendorHeaderGeneratedIdentifierExists(pmwVendor, errorMessages) && vendorDetailAssignedIdentifierExists(pmwVendor, errorMessages)) {
            return true;
        } else {
            if (!errorMessages.isEmpty()){
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                reportData.addPmwVendorAchThatCouldNotBeProcessed(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, errorMessages));
            }
            return false;
        }
    }
    
    private boolean vendorHeaderGeneratedIdentifierExists(PaymentWorksVendor pmwVendor, List<String> errorMessages) {
        if (!(ObjectUtils.isNull(pmwVendor.getKfsVendorHeaderGeneratedIdentifier()))) {
            return true;
        } else {
            errorMessages.add(MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_VENDOR_HEADER_GENERATED_IDENTIFIER_MISSING), pmwVendor.getKfsVendorDocumentNumber()));
            return false;
        }
    }
    
    private boolean vendorDetailAssignedIdentifierExists(PaymentWorksVendor pmwVendor, List<String> errorMessages) {
        if (!(ObjectUtils.isNull(pmwVendor.getKfsVendorDetailAssignedIdentifier()))) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_VENDOR_HEADER_DETAILED_ASSIGNED_IDENTIFIER_MISSING));
            return false;
        }
    }
    
    private boolean allPmwEnteredKfsPayeeAchAccountDataExists(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        if (allPmwEnteredKfsPayeeAchAccountDataExists(pmwVendor)) {
            return true;
        } else {
            reportData.getNoAchDataProvidedVendorsSummary().incrementRecordCount();
            reportData.addNoAchDataProvidedVendor(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NO_ACH_DATA_PROVIDED_BY_VENDOR_MESSAGE)));
            return false;
        }
    }
    
    private boolean allPmwEnteredKfsPayeeAchAccountDataExists(PaymentWorksVendor pmwVendor) {
        if (StringUtils.isNotBlank(pmwVendor.getBankAcctRoutingNumber())
            && StringUtils.isNotBlank(pmwVendor.getBankAcctBankAccountNumber())
            && StringUtils.isNotBlank(pmwVendor.getBankAcctType())
            && StringUtils.isNotBlank(pmwVendor.getBankAcctAchEmail())) {
                return true;
        } else {
            return false;
        }
    }
    
    private boolean allKfsPayeeAchAccountDataIsValid(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        List<String> errorMessages = new ArrayList<String>();
        if (pmwBankRoutingNumberIsValid(pmwVendor, errorMessages) 
            && pmwBankAccountNumberIsValid(pmwVendor, errorMessages)
            && pmwBankAccountTypeIsValid(pmwVendor, errorMessages)
            && isUsAchBank(pmwVendor, reportData)
            && isAchPaymentMethod(pmwVendor, reportData)) {
            return true;
        } else {
            if (!errorMessages.isEmpty()){
                reportData.getRecordsThatCouldNotBeProcessedSummary().incrementRecordCount();
                reportData.addPmwVendorAchThatCouldNotBeProcessed(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, errorMessages));
            }
            return false;
        }
    }
    
    private PaymentWorksVendor setStatusValuesForPaymentWorksNewVendorAchRequestedInKfs(PaymentWorksVendor pmwVendor) {
        pmwVendor.setKfsAchProcessingStatus(PaymentWorksConstants.KFSAchProcessingStatus.ACH_REQUESTED);
        return pmwVendor;
    }
    
    private boolean pmwBankRoutingNumberIsValid(PaymentWorksVendor pmwVendor, List<String> errorMessages) {
        if (StringUtils.isNumeric(pmwVendor.getBankAcctRoutingNumber())
            && !(ObjectUtils.isNull(achBankService.getByPrimaryId(pmwVendor.getBankAcctRoutingNumber())))) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_BANK_ROUTING_NUMBER_INVALID));
            return false;
        }
    }
    
    private boolean pmwBankAccountNumberIsValid(PaymentWorksVendor pmwVendor, List<String> errorMessages) {
        if (StringUtils.isNumeric(pmwVendor.getBankAcctBankAccountNumber())) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_BANK_ACCOUNT_NUMBER_INVALID));
            return false;
        }
    }
    
    private boolean pmwBankAccountTypeIsValid(PaymentWorksVendor pmwVendor, List<String> errorMessages) {
        if (pmwBankAccountTypeIsDefinedInKfs(pmwVendor.getBankAcctType())) {
            return true;
        } else {
            errorMessages.add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_BANK_ACCOUNT_TYPE_INVALID));
            return false;
        }
    }
    
    protected boolean isUsAchBank(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        boolean usAchBank = true;
        if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
            String bankCountry = pmwVendor.getBankAddressCountry();
            if (StringUtils.equalsIgnoreCase(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString(), 
                    bankCountry)) {
                LOG.debug("isUsAchBank, found an ACH record that has a US country, it is OK to proceed.");
            } else {
                LOG.info("isUsAchBank, the bank has a country of " + bankCountry + " so can NOT create an ACH record.");
                reportData.getRecordsWithForeignAchSummary().incrementRecordCount();
                List<String> errorMessages = new ArrayList<String>();
                errorMessages.add(MessageFormat.format(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_BANK_NOT_US), 
                        bankCountry));
                reportData.addForeignAchItem(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, 
                        errorMessages));
                usAchBank = false;
            }
        } else {
            LOG.debug("isUsAchBank, not in foreign form mode, so just return true.");
        }
        return usAchBank;
    }
    
    protected boolean isAchPaymentMethod(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
            LOG.debug("isAchPaymentMethod, legacy form mode, just return true");
            return true;
        } else if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
            if (StringUtils.equalsIgnoreCase(pmwVendor.getPaymentMethod(), PaymentWorksConstants.PaymentWorksPaymentMethods.ACH)) {
                LOG.debug("isAchPaymentMethod, found an ACH payment method, return true.");
                return true;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("isAchPaymentMethod, return false, found a NON ach payment method: " + pmwVendor.getPaymentMethod());
                }
                /*
                 * @todo log in the right place on the report
                 */
                return false;
            }
        } else {
            throw new IllegalStateException("Invalid form mode");
        }
    }
    
    private boolean pmwBankAccountTypeIsDefinedInKfs(String pmwBankAccountTypeToVerify) {
        List<PaymentWorksBankAccountType> matchingValues = getPaymentWorksBatchUtilityService().findAllPmwBankAccountTypesMatching(pmwBankAccountTypeToVerify);
        return (matchingValues.size() == 1);
    }
    
    private void performProcessingWhenInvalidAchAccountDataIsProvidedForApprovedKfsVendor(PaymentWorksVendor pmwVendor) {
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwProcessedKfsApprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.ACH_REJECTED);
    }
    
    private void performProcessingWhenAchAccountDataNotProvidedForApprovedKfsVendor(PaymentWorksVendor pmwVendor) {
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwProcessedKfsApprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.NO_ACH_DATA);
    }

    private void performProcessingWhenNoKfsVendorIdentifiersFoundForKfsApprovedVendor(PaymentWorksVendor pmwVendor) {
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsApprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.NO_VENDOR_IDENTIFIERS);
    }
    
    private void performProcessingWhenExceptionGeneratedForKfsApprovedVendorDuringAchCreation(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getRecordsGeneratingExceptionSummary().incrementRecordCount();
        reportData.addRecordGeneratingException(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, 
                MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.EXCEPTION_GENERATED_DURING_PROCESSING), PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_APPROVED)));
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsApprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.EXCEPTION_GENERATED);
    }
    
    private void performProcessingWhenExceptionGeneratedForKfsDisapprovedVendorDuringAchCreation(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getRecordsGeneratingExceptionSummary().incrementRecordCount();
        reportData.addRecordGeneratingException(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor, 
                MessageFormat.format(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.EXCEPTION_GENERATED_DURING_PROCESSING), PaymentWorksConstants.KFSVendorProcessingStatus.VENDOR_DISAPPROVED)));
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsDisapprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.EXCEPTION_GENERATED);
    }
    
    private void performProcessingForSuccessfulKfsPaatDocumentCreation(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getRecordsProcessedSummary().incrementRecordCount();
        reportData.addRecordProcessed(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor));
        updatePmwNewVendorStagingTableBasedOnSuccessfulCreatePayeeAchProcessing(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.ACH_CREATED);
    }
    
    private void performProcessingForFailedKfsPaatDocumentCreation(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        updatePmwStagingTableWithKfsAchProcessingStatusForPmwProcessedKfsApprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.ACH_REJECTED);
        //Intentionally not making web service call to PMW as it is valid to have a KFS approved vendor with bad ACH data since ACH data is optional.
    }
    
    private void performProcessingWhenAchAccountDataNotProvidedForDisapprovedKfsVendor(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getDisapprovedVendorsSummary().incrementRecordCount();
        reportData.addDisapprovedVendor(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor));
        updatePmwNewVendorStagingTableKfsAchProcessingStatusBasedOnKfsDisapprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.NO_ACH_DATA);
    }
    
    private void performProcessingWhenAchAccountDataIsProvidedForDisapprovedKfsVendor(PaymentWorksVendor pmwVendor, PaymentWorksNewVendorPayeeAchBatchReportData reportData) {
        reportData.getDisapprovedVendorsSummary().incrementRecordCount();
        reportData.addDisapprovedVendor(getPaymentWorksNewVendorPayeeAchReportService().createBatchReportVendorItem(pmwVendor));
        updatePmwNewVendorStagingTableKfsAchProcessingStatusBasedOnKfsDisapprovedVendor(pmwVendor, PaymentWorksConstants.KFSAchProcessingStatus.PVEN_DISAPPROVED);
    }
    
    private void updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsApprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        LOG.info("updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsApprovedVendor: entered for pmwVendorId = " + pmwVendor.getPmwVendorRequestId() + " with kfsAchProcessingStatus = " + kfsAchProcessingStatus);
        pmwVendor = setKfsAchProcessingStatusMakingUploadIneligibleNewVendorForKfsApprovedVendor(pmwVendor, kfsAchProcessingStatus);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(),
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  pmwVendor.getKfsAchProcessingStatus(),
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private void updatePmwStagingTableWithKfsAchProcessingStatusForPmwProcessedKfsApprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        LOG.info("updatePmwStagingTableWithKfsAchProcessingStatusForPmwProcessedKfsApprovedVendor: entered for pmwVendorId = " + pmwVendor.getPmwVendorRequestId() + " with kfsAchProcessingStatus = " + kfsAchProcessingStatus);
        pmwVendor = setKfsAchProcessingStatusRetainingPaymentWorksProcessedNewVendorForKfsApprovedVendor(pmwVendor, kfsAchProcessingStatus);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(),
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  pmwVendor.getKfsAchProcessingStatus(),
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private void updatePmwNewVendorStagingTableBasedOnSuccessfulCreatePayeeAchProcessing(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        LOG.info("updatePmwNewVendorStagingTableBasedOnSuccessfulCreatePayeeAchProcessing: entered for pmwVendorId = " + pmwVendor.getPmwVendorRequestId() + " with kfsAchProcessingStatus = " + kfsAchProcessingStatus);
        pmwVendor = setKfsAchProcessingStatusRetainingPaymentWorksProcessedNewVendorForKfsApprovedVendor(pmwVendor, kfsAchProcessingStatus);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(),
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  pmwVendor.getKfsAchProcessingStatus(),
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp(),
                                                                                  pmwVendor.getKfsAchDocumentNumber());
    }
    
    private void updatePmwNewVendorStagingTableKfsAchProcessingStatusBasedOnKfsDisapprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        LOG.info("updatePmwNewVendorStagingTableKfsAchProcessingStatusBasedOnKfsDisapprovedVendor: entered for pmwVendorId = " + pmwVendor.getPmwVendorRequestId() + " with kfsAchProcessingStatus = " + kfsAchProcessingStatus);
        pmwVendor = setKfsAchProcessingStatusMakingUploadIneligibleNewVendorForKfsDisapprovedVendor(pmwVendor, kfsAchProcessingStatus);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(),
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  pmwVendor.getKfsAchProcessingStatus(),
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
        
    private void updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsDisapprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        LOG.info("updatePmwStagingTableWithKfsAchProcessingStatusForPmwUploadIneligibleKfsDisapprovedVendor: entered for pmwVendorId = " + pmwVendor.getPmwVendorRequestId() + " with kfsAchProcessingStatus = " + kfsAchProcessingStatus);
        pmwVendor = setKfsAchProcessingStatusMakingUploadIneligibleNewVendorForKfsDisapprovedVendor(pmwVendor, kfsAchProcessingStatus);
        getPaymentWorksVendorDao().updateExistingPaymentWorksVendorInStagingTable(pmwVendor.getId(), 
                                                                                  pmwVendor.getPmwRequestStatus(),
                                                                                  pmwVendor.getKfsVendorProcessingStatus(),
                                                                                  pmwVendor.getKfsAchProcessingStatus(),
                                                                                  pmwVendor.getSupplierUploadStatus(),
                                                                                  getDateTimeService().getCurrentTimestamp());
    }
    
    private PaymentWorksVendor setKfsAchProcessingStatusMakingUploadIneligibleNewVendorForKfsApprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        pmwVendor.setKfsAchProcessingStatus(kfsAchProcessingStatus);
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.INELIGIBLE_FOR_UPLOAD);
        return pmwVendor;
    }
    
    private PaymentWorksVendor setKfsAchProcessingStatusRetainingPaymentWorksProcessedNewVendorForKfsApprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        pmwVendor.setKfsAchProcessingStatus(kfsAchProcessingStatus);
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.READY_FOR_UPLOAD);
        return pmwVendor;
    }
    
    private PaymentWorksVendor setKfsAchProcessingStatusMakingUploadIneligibleNewVendorForKfsDisapprovedVendor(PaymentWorksVendor pmwVendor, String kfsAchProcessingStatus) {
        pmwVendor.setPmwRequestStatus(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getText());
        pmwVendor.setKfsAchProcessingStatus(kfsAchProcessingStatus);
        pmwVendor.setSupplierUploadStatus(PaymentWorksConstants.SupplierUploadStatus.INELIGIBLE_FOR_UPLOAD);
        return pmwVendor;
    }

    public AchBankService getAchBankService() {
        return achBankService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
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

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public PaymentWorksVendorAchDataProcessingIntoKfsService getPaymentWorksVendorAchDataProcessingIntoKfsService() {
        return paymentWorksVendorAchDataProcessingIntoKfsService;
    }

    public void setPaymentWorksVendorAchDataProcessingIntoKfsService(
            PaymentWorksVendorAchDataProcessingIntoKfsService paymentWorksVendorAchDataProcessingIntoKfsService) {
        this.paymentWorksVendorAchDataProcessingIntoKfsService = paymentWorksVendorAchDataProcessingIntoKfsService;
    }

    public PaymentWorksVendorDao getPaymentWorksVendorDao() {
        return paymentWorksVendorDao;
    }

    public void setPaymentWorksVendorDao(PaymentWorksVendorDao paymentWorksVendorDao) {
        this.paymentWorksVendorDao = paymentWorksVendorDao;
    }

    public PaymentWorksNewVendorPayeeAchReportService getPaymentWorksNewVendorPayeeAchReportService() {
        return paymentWorksNewVendorPayeeAchReportService;
    }

    public void setPaymentWorksNewVendorPayeeAchReportService(PaymentWorksNewVendorPayeeAchReportService paymentWorksNewVendorPayeeAchReportService) {
        this.paymentWorksNewVendorPayeeAchReportService = paymentWorksNewVendorPayeeAchReportService;
    }

    public void setPaymentWorksFormModeService(PaymentWorksFormModeService paymentWorksFormModeService) {
        this.paymentWorksFormModeService = paymentWorksFormModeService;
    }

}
