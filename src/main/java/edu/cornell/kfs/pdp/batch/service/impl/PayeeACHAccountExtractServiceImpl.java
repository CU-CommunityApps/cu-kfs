package edu.cornell.kfs.pdp.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.ObjectUtil;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractFileResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReportData;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractRetryResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountDocumentService;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.pdp.service.CuAchService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class PayeeACHAccountExtractServiceImpl implements PayeeACHAccountExtractService {
	private static final Logger LOG = LogManager.getLogger();

    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private ParameterService parameterService;
    private PersonService personService;
    private CuAchService achService;
    private AchBankService achBankService;
    private BusinessObjectService businessObjectService; 
    private DateTimeService dateTimeService;
    private PayeeACHAccountExtractReportService payeeACHAccountExtractReportService;
    protected PayeeACHAccountDocumentService payeeACHAccountDocumentService;

    private Map<String, List<String>> partialProcessingSummary;

    // Portions of this method are based on code and logic from CustomerLoadServiceImpl.
    @Transactional
    @Override
    public boolean processACHBatchDetails() {
        PayeeACHAccountExtractReportData achAccountExtractReportData = new PayeeACHAccountExtractReportData();
        
        LOG.info("processACHBatchDetails: Beginning processing of ACH entries persisted for a retry");
        PayeeACHAccountExtractRetryResult processingRetriesResult = loadACHBatchDetailRetries();
        achAccountExtractReportData.setAchAccountExtractRetryResults(processingRetriesResult);
        
        LOG.info("processACHBatchDetails: Beginning processing of ACH input files");
        loadACHBatchDetailFiles(achAccountExtractReportData);
        
        payeeACHAccountExtractReportService.writeBatchJobReports(achAccountExtractReportData);

        // For now, return true even if files or rows did not load successfully. Functionals will address the failed rows/files accordingly.
        return true;
    }
    
    private void loadACHBatchDetailFiles(PayeeACHAccountExtractReportData achAccountExtractReportData) {
        int numSuccess = 0;
        int numPartial = 0;
        int numFail = 0;
        partialProcessingSummary = new HashMap<String, List<String>>();

        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("processACHBatchDetails: Found " + fileNamesToLoad.size() + " file(s) to process.");
        

        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("processACHBatchDetails: Beginning processing of filename: " + inputFileName);
            processedFiles.add(inputFileName);

            try {
                List<String> errorList = new ArrayList<>();
                PayeeACHAccountExtractFileResult processingFileResult = loadACHBatchDetailFile(inputFileName, fileNamesToLoad.get(inputFileName));
                errorList = processingFileResult.getErrors();
                achAccountExtractReportData.addAchAccountExtractFileResult(processingFileResult);
                if (errorList.isEmpty()) {
                    LOG.info("processACHBatchDetails: Successfully loaded ACH input file");
                    numSuccess++;
                } else {
                    LOG.warn("processACHBatchDetails: ACH input file contained "+ errorList.size() + " rows that could not be processed.");
                    partialProcessingSummary.put(inputFileName, errorList);
                    numPartial++;
                }
            } catch (Exception e) {
                LOG.error("processACHBatchDetails: Failed to load ACH input file due to this Exception:", e);
                numFail++;
            }
        }
        
        removeDoneFiles(processedFiles);        

        logSummaryOfPayeeACHExtract(numSuccess, numPartial, numFail);
    }
    
    private void logSummaryOfPayeeACHExtract(int numSuccess, int numPartial, int numFail) {
        LOG.info("processACHBatchDetails: ==============================================");
        LOG.info("processACHBatchDetails: ==== Summary of Payee ACH Account Extract ====");
        LOG.info("processACHBatchDetails: ==============================================");
        LOG.info("processACHBatchDetails: Files loaded successfully: " + numSuccess);
        LOG.info("processACHBatchDetails: Files loaded with one or more failed rows: " + numPartial);
        if (!partialProcessingSummary.isEmpty()) {
            for (String failingFileName : partialProcessingSummary.keySet()) {
                List<String> errorsEncountered = partialProcessingSummary.get(failingFileName);
                LOG.error("processACHBatchDetails: ACH input file contained "+ errorsEncountered.size() + " rows that could not be processed.");
                for (Iterator iterator = errorsEncountered.iterator(); iterator.hasNext();) {
                    String dataError = (String) iterator.next();
                    LOG.error("processACHBatchDetails: " + dataError);
                }
            }
        }
        LOG.info("processACHBatchDetails: Files with errors: " + numFail);
        
        LOG.info("processACHBatchDetails: =====================");
        LOG.info("processACHBatchDetails: ==== End Summary ====");
        LOG.info("processACHBatchDetails: =====================");
    }

    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     */
    // This is a slightly-tweaked copy of a method from CustomerLoadServiceImpl.
    protected Map<String,BatchInputFileType> getListOfFilesToProcess() {
        Map<String,BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifier()
                        + ") returned NULL which should never happen.");
            } else {
                // update the file name mapping
                for (String inputFileName : inputFileNames) {

                    // filenames returned should never be blank/empty/null
                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("One of the file names returned as ready to process [" + inputFileName
                                + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }

    /**
     * Processes a single ACH input file.
     */
    // Portions of this method are based on the code and logic from CustomerLoadServiceImpl.loadFile
    protected PayeeACHAccountExtractFileResult loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
        List<String> failedRowsErrors = new ArrayList<>();
        PayeeACHAccountExtractFileResult processingFileResult = new PayeeACHAccountExtractFileResult(inputFileName);
        
        byte[] fileByteContent = LoadFileUtils.safelyLoadFileBytes(inputFileName);
        
        LOG.info("loadACHBatchDetailFile: Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "loadACHBatchDetailFile: Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "loadACHBatchDetailFile: Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }

        List<PayeeACHAccountExtractDetail> achDetails = (List<PayeeACHAccountExtractDetail>) parsedObject;
        
        for (PayeeACHAccountExtractDetail achDetail : achDetails) {
            cleanPayeeACHAccountExtractDetail(achDetail);
            String error = processACHBatchDetail(achDetail);
            List<String> errors = new ArrayList<String>();
            if (StringUtils.isNotBlank(error)) {
                failedRowsErrors.add(error);
                processingFileResult.addFailedRow();
                errors.add(error);
                processingFileResult.getErrorEntries().put(achDetail, errors);
                storeACHAccountExtractDetailForRetry(achDetail);
            }
            else {
                achDetail.setCreateDate(dateTimeService.getCurrentSqlDate());
                processingFileResult.addSuccessRow();
                processingFileResult.getSuccessEntries().add(achDetail);
            }
        }
        
        processingFileResult.setErrors(failedRowsErrors);
        
        return processingFileResult;
    }

    protected PayeeACHAccountExtractRetryResult loadACHBatchDetailRetries() {
        PayeeACHAccountExtractRetryResult processingRetryResult = new PayeeACHAccountExtractRetryResult();
        int numRetrySuccess = 0;
        int numRetryFail = 0;

        List<PayeeACHAccountExtractDetail> achDetailsEligibleForRetry = getPayeeACHExtractDetailsEligibleForRetry();
        if (!achDetailsEligibleForRetry.isEmpty()) {
            for (PayeeACHAccountExtractDetail achDetail : achDetailsEligibleForRetry) {
                String error = processACHBatchDetail(achDetail);
                if (StringUtils.isNotBlank(error)) {
                    updateACHAccountExtractDetailRetryCount(achDetail, achDetail.getRetryCount() + 1);
                    numRetryFail++;
                    addErrorToReport(processingRetryResult, achDetail, error);
                } else {
                    markACHAccountExtractDetailAsProcessed(achDetail);
                    numRetrySuccess++;
                    addSuccessToReport(processingRetryResult, achDetail);
                }
            }
        }
        logLoadACHBatchDetailRetriesSummary(numRetrySuccess, numRetryFail);
        return processingRetryResult;
    }
    
    private void logLoadACHBatchDetailRetriesSummary(int numRetrySuccess, int numRetryFail) {
        LOG.info("loadACHBatchDetailPersisted: ======================================================");
        LOG.info("loadACHBatchDetailPersisted: ==== Summary of Payee ACH Account Extract Retries ====");
        LOG.info("loadACHBatchDetailPersisted: ======================================================");
        LOG.info("loadACHBatchDetailPersisted: Retries loaded successfully: " + numRetrySuccess);
        LOG.info("loadACHBatchDetailPersisted: Retries that failed: " + numRetryFail);      
    }
    
    private void addSuccessToReport(PayeeACHAccountExtractRetryResult processingRetryResult, PayeeACHAccountExtractDetail achDetail) {
        processingRetryResult.addSuccessRow();
        processingRetryResult.getSuccessEntries().add(achDetail);
    }
    
    private void addErrorToReport(PayeeACHAccountExtractRetryResult processingRetryResult, PayeeACHAccountExtractDetail achDetail, String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        processingRetryResult.addFailedRow();
        processingRetryResult.getErrors().add(error);
        if(achDetail.getRetryCount().equals(getMaxRetryCount())) {
            errors.add("Maximum processing retries has been reached for this entry.");
        }
        processingRetryResult.getErrorEntries().put(achDetail, errors);
    }

    private List<PayeeACHAccountExtractDetail> getPayeeACHExtractDetailsEligibleForRetry() {
        List<PayeeACHAccountExtractDetail> persistedPayeeACHAccountExtractDetails = getPersistedPayeeACHAccountExtractDetails();
        if (!persistedPayeeACHAccountExtractDetails.isEmpty()) {
            return getSortedACHDetailsEligibleForRetry(persistedPayeeACHAccountExtractDetails);
        }
        return persistedPayeeACHAccountExtractDetails;
    }

    private List<PayeeACHAccountExtractDetail> getSortedACHDetailsEligibleForRetry(List<PayeeACHAccountExtractDetail> persistedPayeeACHAccountExtractDetails) {
        List<PayeeACHAccountExtractDetail> achDetailsEligibleForRetry = new ArrayList<>();
        List<PayeeACHAccountExtractDetail> sortedAchDetailsEligibleForRetry = new ArrayList<>();
        achDetailsEligibleForRetry = persistedPayeeACHAccountExtractDetails.stream().filter(a -> a.getRetryCount() < getMaxRetryCount())
                .collect(Collectors.toList());
        sortedAchDetailsEligibleForRetry = achDetailsEligibleForRetry.stream().sorted(Comparator.comparing(PayeeACHAccountExtractDetail::getIdBigIntValue))
                .collect(Collectors.toList());
        return sortedAchDetailsEligibleForRetry;
    }

    private int getMaxRetryCount() {
        return Integer.parseInt(parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, CUPdpParameterConstants.MAX_ACH_ACCT_EXTRACT_RETRY));
    }
    
    private void storeACHAccountExtractDetailForRetry(PayeeACHAccountExtractDetail achDetail) {
        achDetail.setCreateDate(dateTimeService.getCurrentSqlDate());
        achDetail.setStatus(CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN);
        updateACHAccountExtractDetailRetryCount(achDetail, 0);
    }

    private void markACHAccountExtractDetailAsProcessed(PayeeACHAccountExtractDetail achDetail) {
        achDetail.setStatus(CUPdpConstants.PayeeAchAccountExtractStatuses.PROCESSED);
        updateACHAccountExtractDetailRetryCount(achDetail, achDetail.getRetryCount() + 1);
    }

    private void updateACHAccountExtractDetailRetryCount(PayeeACHAccountExtractDetail achDetail, int retryCount) {
        achDetail.setLastUpdatedTimestamp(dateTimeService.getCurrentTimestamp());
        achDetail.setRetryCount(retryCount);
        businessObjectService.save(achDetail);
    }

    protected List<PayeeACHAccountExtractDetail> getPersistedPayeeACHAccountExtractDetails() {
        List<PayeeACHAccountExtractDetail> persistedPayeeACHAccountExtractDetails = new ArrayList<>();
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(CUPdpPropertyConstants.PAYEE_ACH_EXTRACT_DETAIL_STATUS, CUPdpConstants.PayeeAchAccountExtractStatuses.OPEN);
        persistedPayeeACHAccountExtractDetails
                .addAll(businessObjectService.findMatchingOrderBy(PayeeACHAccountExtractDetail.class, fieldValues, KRADPropertyConstants.ID, true));
        return persistedPayeeACHAccountExtractDetails;
    }
    
    protected void cleanPayeeACHAccountExtractDetail(PayeeACHAccountExtractDetail detail) {
        if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
            String logMessageStarter = "cleanPayeeACHAccountExtractDetail, the bank account for " + detail.getNetID();
            String bankAccountNumber = detail.getBankAccountNumber();
            if (StringUtils.contains(bankAccountNumber, KFSConstants.DASH)) {
                LOG.info(logMessageStarter + " contains dashes, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber, KFSConstants.DASH);
            }
            if (StringUtils.contains(bankAccountNumber, StringUtils.SPACE)) {
                LOG.info(logMessageStarter + " contains spaces, so removing them");
                bankAccountNumber = StringUtils.remove(bankAccountNumber,  StringUtils.SPACE);
            }
            
            detail.setBankAccountNumber(bankAccountNumber);
            
            if (!StringUtils.isNumeric(detail.getBankAccountNumber())) {
                LOG.error(logMessageStarter + " is not numeric after cleaning");
            }

            detail.setBankName(extractBankName(detail));
        }
    }

    private String extractBankName(PayeeACHAccountExtractDetail detail) {
        String bankName = ObjectUtils.isNull(detail) ? StringUtils.EMPTY : detail.getBankName();
        if (StringUtils.length(bankName) > 40) {
            LOG.info("cleanPayeeACHAccountExtractDetail truncating bank name to 40 characters");
            bankName = bankName.substring(0, 39);
        }
        return bankName;
    }

    /**
     * Processes a single ACH batch detail, and routes a Payee ACH Account maintenance document accordingly.
     */
    protected String processACHBatchDetail(PayeeACHAccountExtractDetail achDetail) {
        LOG.info("processACHBatchDetail: Starting processACHBatchDetail for: " + achDetail.getLogData());
        Person payee = personService.getPersonByPrincipalName(achDetail.getNetID());

        String processingError = validateACHBatchDetail(achDetail, payee);
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        
        processingError = addOrUpdateACHAccountIfNecessary(payee, achDetail, PayeeIdTypeCodes.ENTITY, payee.getEntityId());
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        
        processingError = addOrUpdateACHAccountIfNecessary(payee, achDetail, PayeeIdTypeCodes.EMPLOYEE, payee.getEmployeeId());
        
        return processingError;
    }

    protected String addOrUpdateACHAccountIfNecessary(
            Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType, String payeeIdNumber) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        PayeeACHAccount achAccount = achService.getAchInformationIncludingInactive(
                payeeType, payeeIdNumber, payeeACHAccountDocumentService.getDirectDepositTransactionType());
        
        if (ObjectUtils.isNull(achAccount)) {
            return payeeACHAccountDocumentService.addACHAccount(payee, achDetail, payeeType);
        } else {
            return payeeACHAccountDocumentService.updateACHAccountIfNecessary(payee, achDetail, achAccount);
        }
    }

    /**
     * Validates whether a single file-loaded ACH batch detail is valid for processing.
     * A warning message will be added to the logs if the row should be skipped.
     * 
     * @param achDetail The batch detail to check; cannot be null.
     * @param payee The object representing the payee matching the batch detail's netID; may be null.
     * @return True if the ACH batch detail passed validation, false otherwise.
     */
    protected String validateACHBatchDetail(PayeeACHAccountExtractDetail achDetail, Person payee) {
        final int BUILDER_START_SIZE = 200;
        StringBuilder failureMessage = new StringBuilder(BUILDER_START_SIZE);
        int listIndex = 1;
        
        if (ObjectUtils.isNull(payee) || StringUtils.isBlank(payee.getEntityId())) {
            // Person doesn't exist in KFS.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Payee does not exist in KFS. ");
        } else {
            // Some validations can only occur if payee exists.
            if (!StringUtils.equals(achDetail.getEmployeeID(), payee.getEmployeeId())) {
                // Employee ID mismatch between input file and KFS.
                appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
                failureMessage.append(" Payee has an employee ID of \"").append(achDetail.getEmployeeID()).append(
                        "\" in input file, but has an employee ID of \"").append(payee.getEmployeeId()).append("\" in KFS. ");
            }
            if (StringUtils.isBlank(payee.getEmailAddressUnmasked())) {
                appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
                failureMessage.append(" Payee has no email address defined in KFS. No notification emails will be sent for this user.");
            }
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE.equalsIgnoreCase(achDetail.getPaymentType())) {
            // Input file payment type is not "Direct Deposit".
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Payment type is \"").append(achDetail.getPaymentType()).append("\" instead of \"").append(
                    CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE).append("\". ");
        }
        
        if (StringUtils.isBlank(achDetail.getBankName())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank Name was not supplied. ");
        }

        if (StringUtils.isBlank(achDetail.getBankRoutingNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank routing number was not supplied. ");
        } else if (!StringUtils.isNumeric(achDetail.getBankRoutingNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank routing number must only contain digits. ");
        } else if (ObjectUtils.isNull(achBankService.getByPrimaryId(achDetail.getBankRoutingNumber()))) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Could not find bank \"").append(achDetail.getBankName()).append("\" under the given routing number. ");
        }

        if (StringUtils.isBlank(achDetail.getBankAccountNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank account number was not supplied. ");
        } else if (!StringUtils.isNumeric(achDetail.getBankAccountNumber())) {
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Bank account number must only contain digits. ");
        }

        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR.equals(achDetail.getBalanceAccount())) {
            // ACH detail is not for a balance account.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Account is not a balance account. ");
        }
        
        if (!CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())
                && !CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE.equalsIgnoreCase(achDetail.getBankAccountType())) {
            // ACH detail is not for a checking or savings account.
            appendFailurePrefix(failureMessage, achDetail.getNetID(), listIndex++);
            failureMessage.append(" Account is not a checking or savings account. ");
        }

        // Log the message as a warning if non-blank, and also treat non-blank messages as failures; otherwise return success.
        if (failureMessage.length() > 0) {
            LOG.warn("validateACHBatchDetail:" + failureMessage.toString());
            return failureMessage.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Helper method invoked by validateACHBatchDetail() when an ACH batch detail is not
     * valid for processing and the failure message needs to have a prefix added for the
     * first or subsequent failures detected.
     * 
     * @param failureMessage The StringBuilder to append the prefix to.
     * @param netID The payee's netID.
     * @param listIndex The index of the next failure reason to be appended by the calling code.
     */
    protected void appendFailurePrefix(StringBuilder failureMessage, String netID, int listIndex) {
        if (failureMessage.length() == 0) {
            failureMessage.append("ACH Detail for payee \"").append(netID).append("\" could not be processed for the following reasons: ");
        }
        failureMessage.append('[').append(listIndex).append(']');
    }

    protected String getPayeeACHAccountExtractParameter(String parameterName) {
        return parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, parameterName);
    }

    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    // Copied this method from CustomerLoadServiceImpl.
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
     * LOG error and throw RuntimeException
     * 
     * @param errorMessage
     */
    private void criticalError(String errorMessage) {
        LOG.error("criticalError: " + errorMessage);
        throw new RuntimeException(errorMessage);
    }    

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileTypes(List<BatchInputFileType> batchInputFileTypes) {
        this.batchInputFileTypes = batchInputFileTypes;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAchService(CuAchService achService) {
        this.achService = achService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public PayeeACHAccountExtractReportService getPayeeACHAccountExtractReportService() {
        return payeeACHAccountExtractReportService;
    }

    public void setPayeeACHAccountExtractReportService(PayeeACHAccountExtractReportService payeeACHAccountExtractReportService) {
        this.payeeACHAccountExtractReportService = payeeACHAccountExtractReportService;
    }

    public void setPayeeACHAccountDocumentService(PayeeACHAccountDocumentService payeeACHAccountDocumentService) {
        this.payeeACHAccountDocumentService = payeeACHAccountDocumentService;
    }

}
