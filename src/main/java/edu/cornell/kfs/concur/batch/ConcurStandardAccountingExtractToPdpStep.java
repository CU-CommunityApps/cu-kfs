package edu.cornell.kfs.concur.batch;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractToPdpStep extends AbstractStep {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractToPdpStep.class);
    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        List<String> listOfFileNames = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());

        if (LOG.isDebugEnabled()) {
            String numberOfFiles = listOfFileNames != null ? String.valueOf(listOfFileNames.size()) : "NULL";
            LOG.debug("execute started, directoryPath: number of files found to process: " + numberOfFiles);
        }

        boolean success = true;
        for (String fileName : listOfFileNames) {
            LOG.info("execute, processing: " + fileName);
            
            File currentFile = new File(fileName);
            success = processCurrentFileAndExtractPdpFeedFromSAEFile(currentFile) && success;
            
        }

        return success;
    }

    protected boolean processCurrentFileAndExtractPdpFeedFromSAEFile(File currentFile) {
        boolean success = true;
        LOG.debug("processCurrentFileAndExtractPdpFeedFromSAEFile, current File: " + currentFile.getName());
        try {
            ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
                    .parseStandardAccoutingExtractFileToStandardAccountingExtractFile(currentFile);
            logDetailedInfoForConcurStandardAccountingExtractFile(concurStandardAccoutingExtractFile);
            success = getConcurStandardAccountingExtractService()
                    .extractPdpFeedFromStandardAccounitngExtract(concurStandardAccoutingExtractFile);
        } catch (ValidationException ve) {
            success = false;
            LOG.error("processCurrentFileAndExtractPdpFeedFromSAEFile, There was a validation error processing "
                    + currentFile.getName(), ve);
        }
        return success;
    }
    
    protected void logDetailedInfoForConcurStandardAccountingExtractFile(ConcurStandardAccountingExtractFile saeFile) {
        if (LOG.isDebugEnabled()) {
            if (saeFile != null) {
                LOG.debug("debugConcurStandardAccountingExtractFile, " + saeFile.getDebugInformation());
                if (saeFile.getConcurStandardAccountingExtractDetailLines() != null) {
                    LOG.debug("debugConcurStandardAccountingExtractFile, Number of line items: " + saeFile.getConcurStandardAccountingExtractDetailLines().size());
                    for (ConcurStandardAccountingExtractDetailLine line : saeFile.getConcurStandardAccountingExtractDetailLines()) {
                        LOG.debug("debugConcurStandardAccountingExtractFile, " + line.getDebugInformation());
                    }
                } else {
                    LOG.debug("debugConcurStandardAccountingExtractFile, The getConcurStandardAccountingExtractDetailLines is null");
                }
                
            } else {
                LOG.debug("debugConcurStandardAccountingExtractFile, The SAE file is null");
            }
        }
    }

    public ConcurStandardAccountingExtractService getConcurStandardAccountingExtractService() {
        return concurStandardAccountingExtractService;
    }

    public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
        this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }
}
