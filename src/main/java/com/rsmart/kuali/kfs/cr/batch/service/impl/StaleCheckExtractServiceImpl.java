package com.rsmart.kuali.kfs.cr.batch.service.impl;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;
import com.rsmart.kuali.kfs.cr.batch.service.StaleCheckExtractService;
import com.rsmart.kuali.kfs.cr.businessobject.StaleCheckBatchRow;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractServiceImpl implements StaleCheckExtractService {
	private static final Logger LOG = LogManager.getLogger(StaleCheckExtractServiceImpl.class);

    private BusinessObjectService businessObjectService;
    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private CheckReconciliationDao checkReconciliationDao;
    private DateTimeService dateTimeService;

    @Transactional
    @Override
    public boolean processStaleCheckBatchFiles() {
        LOG.info("processStaleCheckBatchFiles: Beginning processing of Stale Check Batch input files");

        StaleCheckBatchProcessResults staleCheckBatchProcessResults = new StaleCheckBatchProcessResults();


        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("processStaleCheckBatchFiles: Found " + fileNamesToLoad.size() + " file(s) to process.");

        List<String> processedFiles = new ArrayList<>();
        for (String inputFileName : fileNamesToLoad.keySet()) {

            String filename = Paths.get(inputFileName).getFileName().toString();
            LOG.info("processStaleCheckBatchFiles: Beginning processing of file: " + filename);
            processedFiles.add(inputFileName);
            
            try {
                List<String> errorList = loadStaleCheckBatchFile(inputFileName, fileNamesToLoad.get(inputFileName));
                if (errorList.isEmpty()) {
                    LOG.info("processStaleCheckBatchFiles: Successfully loaded Stale Check Batch input file");
                    staleCheckBatchProcessResults.incrementNumSuccessFiles();
                } else {
                    LOG.warn("processStaleCheckBatchFiles: Stale Check Batch input file contained "+ errorList.size() + " rows that could not be processed.");
                    staleCheckBatchProcessResults.addPartialProcessResult(inputFileName, errorList);
                    staleCheckBatchProcessResults.incrementNumPartialFiles();
                }
            } catch (Exception e) {
                LOG.error("processStaleCheckBatchFiles: Failed to load  Stale Check Batch input file due to this Exception:", e);
                staleCheckBatchProcessResults.incrementNumErrorFiles();
            }
        }
        removeDoneFiles(processedFiles);
        logStaleCheckBatchResults(staleCheckBatchProcessResults);
        return true;
    }

    private void logStaleCheckBatchResults(StaleCheckBatchProcessResults staleCheckBatchProcessResults) {
        LOG.info("logStaleCheckBatchResults: ==============================================");
        LOG.info("logStaleCheckBatchResults: ==== Summary of Stale Check Batch ====");
        LOG.info("logStaleCheckBatchResults: ==============================================");
        LOG.info("logStaleCheckBatchResults: Files loaded successfully: " + staleCheckBatchProcessResults.getNumSuccessFiles());
        LOG.info("logStaleCheckBatchResults: Files loaded with one or more failed rows: " + staleCheckBatchProcessResults.getNumPartialFiles());
        if (!staleCheckBatchProcessResults.getPartialProcessingSummary().isEmpty()) {
            for (String failingFileName : staleCheckBatchProcessResults.getPartialProcessingSummary().keySet()) {
                List<String> errorsEncountered = staleCheckBatchProcessResults.getPartialProcessingSummary().get(failingFileName);
                LOG.error("logStaleCheckBatchResults: Stale Check Batch input file contained "+ errorsEncountered.size() +
                        " rows that could not be processed. (" + failingFileName + ")");
                for (String dataError : errorsEncountered) {
                    LOG.error("logStaleCheckBatchResults: " + dataError);
                }
            }
        }
        LOG.info("logStaleCheckBatchResults: Files with errors: " + staleCheckBatchProcessResults.getNumErrorFiles());
        LOG.info("logStaleCheckBatchResults: =====================");
        LOG.info("logStaleCheckBatchResults: ==== End Summary ====");
    }

    protected Map<String,BatchInputFileType> getListOfFilesToProcess() {
        Map<String,BatchInputFileType> inputFileTypeMap = new LinkedHashMap<>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("getListOfFilesToProcess: BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifer()
                        + ") returned NULL which should never happen.");
            } else {
                for (String inputFileName : inputFileNames) {

                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("getListOfFilesToProcess: One of the file names returned as ready to process [" + inputFileName
                                + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }

    protected List<String> loadStaleCheckBatchFile(String inputFileName, BatchInputFileType batchInputFileType) {
        List<String> failedRowsErrors = new ArrayList<>();
        byte[] fileByteContent = safelyLoadFileBytes(inputFileName);
        LOG.info("loadStaleCheckBatchFile: Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "loadStaleCheckBatchFile: Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "loadStaleCheckBatchFile: Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }

        List<StaleCheckBatchRow> staleChecks = (List<StaleCheckBatchRow>) parsedObject;
        for (StaleCheckBatchRow staleCheck : staleChecks) {
            String error = processStaleCheckBatchRow(staleCheck);
            if (StringUtils.isNotBlank(error)) {
                failedRowsErrors.add(error);
            }
        }
        
        return failedRowsErrors;
    }

    protected String processStaleCheckBatchRow(StaleCheckBatchRow staleCheckRow) {
        LOG.info("processStaleCheckBatchRow: Starting processStaleCheckBatchRow for: " + staleCheckRow.getLogData());
        CheckReconciliation checkReconciliation = checkReconciliationDao.findByCheckNumber(staleCheckRow.getCheckNumber());

        String processingError = validateStaleCheckBatchRow(staleCheckRow, checkReconciliation);
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        checkReconciliation.setStatus(CRConstants.STALE);
        checkReconciliation.setStatusChangeDate(dateTimeService.getCurrentSqlDate());
        businessObjectService.save(checkReconciliation);

        return processingError;
    }

    protected String validateStaleCheckBatchRow(StaleCheckBatchRow staleCheckRow, CheckReconciliation checkReconciliation) {
        List<String> validationErrors = new ArrayList<>();

        if (StringUtils.isBlank(staleCheckRow.getBankCode())) {
            validationErrors.add("Bank is blank.");
        }

        if (StringUtils.isBlank(staleCheckRow.getCheckNumber())) {
            validationErrors.add("Check Number is blank.");
        }

        if (StringUtils.isBlank(staleCheckRow.getCheckTotalAmount())) {
            validationErrors.add("Check Total Amount is blank.");
        }

        if (ObjectUtils.isNull(checkReconciliation) || ObjectUtils.isNull(checkReconciliation.getCheckNumber())) {
            validationErrors.add("Check Reconciliation does not exist in KFS.");
        } else {
            validateCheckReconciliation(staleCheckRow, checkReconciliation, validationErrors);
        }

        if (validationErrors.size() > 0) {
            String validationErrorMessagesForRow = getValidationErrorsForRow(validationErrors);
            LOG.warn("validateStaleCheckBatchRow:" + validationErrorMessagesForRow);
            return validationErrorMessagesForRow;
        }
        return StringUtils.EMPTY;
    }

    protected String getValidationErrorsForRow(List<String> validationErrors) {
        String failureMessage = "Stale Check Row could not be processed for the following reasons:";
        for (int i=0; i<validationErrors.size(); ++i) {
            failureMessage += " [" + Integer.toString(i + 1) + "] " + validationErrors.get(i);
        }
        return failureMessage;
    }

    protected void validateCheckReconciliation(StaleCheckBatchRow staleCheckRow, CheckReconciliation checkReconciliation, List<String> validationErrors) {
        if (!StringUtils.equalsIgnoreCase(staleCheckRow.getBankCode(), checkReconciliation.getBankCode())) {
            validationErrors.add("The Bank Code in the file (" + staleCheckRow.getBankCode() + ") row does not match the bank on the Check (" +
                    checkReconciliation.getBankCode() + ").");
        }

        try {
            Double checkTotalAmountParsed = NumberFormat.getNumberInstance(java.util.Locale.US).parse(staleCheckRow.getCheckTotalAmount()).doubleValue();
            KualiDecimal checkTotalAmount = new KualiDecimal(checkTotalAmountParsed);
            if (!checkTotalAmount.equals(checkReconciliation.getAmount())) {
                validationErrors.add("The Total Check Amount in the file row (" + checkTotalAmount.toString() + ") does not match the amount on the Check (" +
                        checkReconciliation.getAmount().toString() + ").");
            }
        } catch (java.text.ParseException ex) {
            validationErrors.add("Error converting total amount to decimal [" + staleCheckRow.getCheckTotalAmount() + "] " + ex.toString());
        }

        if (StringUtils.equalsIgnoreCase(checkReconciliation.getStatus(), CRConstants.STALE)) {
            validationErrors.add("Check Status is already STALE.");
        }
    }

    protected byte[] safelyLoadFileBytes(String fileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        
        try {
            fileContents = new FileInputStream(fileName);
        } catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        
        return fileByteContent;
    }

    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

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

    public void setCheckReconciliationDao(CheckReconciliationDao checkReconciliationDao) {
        this.checkReconciliationDao = checkReconciliationDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    private class StaleCheckBatchProcessResults {
        private int numSuccessFiles = 0;
        private int numPartialFiles = 0;
        private int numErrorFiles = 0;
        private Map<String, List<String>> partialProcessingSummary = new HashMap<>();

        public StaleCheckBatchProcessResults() {}

        public Map<String, List<String>> getPartialProcessingSummary() {
            return partialProcessingSummary;
        }

        public void addPartialProcessResult(String inputFileName, List<String> errorList) {
            partialProcessingSummary.put(inputFileName, errorList);
        }

        public int getNumSuccessFiles() {
            return numSuccessFiles;
        }

        public void incrementNumSuccessFiles() {
            ++numSuccessFiles;
        }

        public int getNumPartialFiles() {
            return numPartialFiles;
        }

        public void incrementNumPartialFiles() {
            ++numPartialFiles;
        }

        public int getNumErrorFiles() {
            return numErrorFiles;
        }

        public void incrementNumErrorFiles() {
            ++numErrorFiles;
        }
    }

}
