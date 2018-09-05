package edu.cornell.kfs.pdp.batch.service.impl;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;
import edu.cornell.kfs.pdp.batch.service.StaleCheckExtractService;
import edu.cornell.kfs.pdp.businessobject.StaleCheckExtractDetail;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.BankService;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaleCheckExtractServiceImpl implements StaleCheckExtractService {
	private static final Logger LOG = LogManager.getLogger(StaleCheckExtractServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private List<BatchInputFileType> batchInputFileTypes;
    private CheckReconciliationDao checkReconciliationDao;
    private BankService bankService;

    private Map<String, List<String>> partialProcessingSummary;

    // Portions of this method are based on code and logic from CustomerLoadServiceImpl.
    @Transactional
    @Override
    public boolean processStaleCheckBatchDetails() {
        LOG.info("processStaleCheckBatchDetails: Beginning processing of ACH input files");
        
        int numSuccess = 0;
        int numPartial = 0;
        int numFail = 0;
        partialProcessingSummary = new HashMap<String, List<String>>();

        Map<String,BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("processStaleCheckBatchDetails: Found " + fileNamesToLoad.size() + " file(s) to process.");

        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("processStaleCheckBatchDetails: Beginning processing of filename: " + inputFileName);
            processedFiles.add(inputFileName);
            
            try {
                List<String> errorList = loadStaleCheckDetailFile(inputFileName, fileNamesToLoad.get(inputFileName));
                if (errorList.isEmpty()) {
                    LOG.info("processStaleCheckBatchDetails: Successfully loaded Stale Check CSV input file");
                    numSuccess++;
                } else {
                    LOG.warn("processStaleCheckBatchDetails:  Stale Check CSV input file contained "+ errorList.size() + " rows that could not be processed.");
                    partialProcessingSummary.put(inputFileName, errorList);
                    numPartial++;
                }
            } catch (Exception e) {
                LOG.error("processStaleCheckBatchDetails: Failed to load  Stale Check CSV input file due to this Exception:", e);
                numFail++;
            }
        }

        removeDoneFiles(processedFiles);

        LOG.info("processStaleCheckBatchDetails: ==============================================");
        LOG.info("processStaleCheckBatchDetails: ==== Summary of Payee ACH Account Extract ====");
        LOG.info("processStaleCheckBatchDetails: ==============================================");
        LOG.info("processStaleCheckBatchDetails: Files loaded successfully: " + numSuccess);
        LOG.info("processStaleCheckBatchDetails: Files loaded with one or more failed rows: " + numPartial);
        if (!partialProcessingSummary.isEmpty()) {
            for (String failingFileName : partialProcessingSummary.keySet()) {
                List<String> errorsEncountered = partialProcessingSummary.get(failingFileName);
                LOG.error("processStaleCheckBatchDetails:  Stale Check CSV input file contained "+ errorsEncountered.size() + " rows that could not be processed.");
                for (Iterator iterator = errorsEncountered.iterator(); iterator.hasNext();) {
                    String dataError = (String) iterator.next();
                    LOG.error("processStaleCheckBatchDetails: " + dataError);
                }
            }
        }
        LOG.info("processStaleCheckBatchDetails: Files with errors: " + numFail);
        LOG.info("processStaleCheckBatchDetails: =====================");
        LOG.info("processStaleCheckBatchDetails: ==== End Summary ====");
        LOG.info("processStaleCheckBatchDetails: =====================");

        // For now, return true even if files or rows did not load successfully. Functionals will address the failed rows/files accordingly.
        return true;
    }

    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     */
    protected Map<String,BatchInputFileType> getListOfFilesToProcess() {
        Map<String,BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifer()
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

    protected List<String> loadStaleCheckDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
        List<String>failedRowsErrors = new ArrayList();
        
        byte[] fileByteContent = safelyLoadFileBytes(inputFileName);
        
        LOG.info("loadStaleCheckBatchDetailFile: Attempting to parse the file.");
        
        Object parsedObject = null;
        try {
            parsedObject = batchInputFileService.parse(batchInputFileType, fileByteContent);
        } catch (ParseException e) {
            String errorMessage = "loadStaleCheckDetailFile: Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        if (!(parsedObject instanceof List)) {
            String errorMessage = "loadStaleCheckDetailFile: Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }

        List<StaleCheckExtractDetail> staleChecks = (List<StaleCheckExtractDetail>) parsedObject;
        
        for (StaleCheckExtractDetail staleCheck : staleChecks) {
            String error = processStaleCheckBatchDetail(staleCheck);
            if (StringUtils.isNotBlank(error)) {
                failedRowsErrors.add(error);
            }
        }
        
        return failedRowsErrors;
    }

    protected String processStaleCheckBatchDetail(StaleCheckExtractDetail staleCheckDetail) {
        LOG.info("processStaleCheckBatchDetail: Starting processStaleCheckBatchDetail for: " + staleCheckDetail.getLogData());
        Bank bank = bankService.getByPrimaryId(staleCheckDetail.getBankCode());
        CheckReconciliation checkReconciliation = checkReconciliationDao.findByCheckNumber(staleCheckDetail.getCheckNumber());

        String processingError = validateStaleCheckBatchDetail(staleCheckDetail, bank, checkReconciliation);
        if (StringUtils.isNotBlank(processingError)) {
            return processingError;
        }
        checkReconciliation.setStatus(CRConstants.STALE);

        //todo: need to save or begin/commit?
        return processingError;
    }

    protected String validateStaleCheckBatchDetail(StaleCheckExtractDetail staleCheckDetail, Bank bank, CheckReconciliation checkReconciliation) {
        String failureMessage = KFSConstants.EMPTY_STRING;
        int listIndex = 1;

        if (ObjectUtils.isNull(staleCheckDetail.getBankCode()) || StringUtils.isBlank(bank.getBankCode())) {
            failureMessage = appendFailureMessage(failureMessage, staleCheckDetail, listIndex++, " Bank does not exist in KFS. ");
        }

        if (ObjectUtils.isNull(staleCheckDetail.getCheckNumber()) || StringUtils.isBlank(checkReconciliation.getBankAccountNumber())) {
            failureMessage = appendFailureMessage(failureMessage, staleCheckDetail, listIndex++, " Check Reconciliation does not exist in KFS. ");
        }

        if (failureMessage.length() > 0) {
            LOG.warn("validateStaleCheckBatchDetail:" + failureMessage);
            return failureMessage.toString();
        }
        return StringUtils.EMPTY;
    }

    protected String appendFailureMessage(String failureMessage, StaleCheckExtractDetail staleCheckExtractDetail, int listIndex, String errorMessage) {
        if (failureMessage.length() == 0) {
            failureMessage += "Stale Check Detail in filename " + staleCheckExtractDetail.getFilename() + " for line " +
                    staleCheckExtractDetail.getLineNumber() + " could not be processed for the following reasons: ";
        }
        failureMessage += "[" + Integer.toString(listIndex) + "] " + errorMessage;
        return failureMessage;
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

    public void setCheckReconciliationDao(CheckReconciliationDao checkReconciliationDao) {
        this.checkReconciliationDao = checkReconciliationDao;
    }

    public void setBankService(BankService bankService) {
        this.bankService = bankService;
    }

}
