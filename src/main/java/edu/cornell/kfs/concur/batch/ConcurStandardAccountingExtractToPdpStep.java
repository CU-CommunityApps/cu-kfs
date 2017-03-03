package edu.cornell.kfs.concur.batch;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractToPdpStep extends AbstractStep {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractToPdpStep.class);
    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    protected FileStorageService fileStorageService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        List<String> listOfSaeFileNames = getConcurStandardAccountingExtractService().buildListOfFileNamesToBeProcessed();
        boolean success = true;
        for (String saeFileName : listOfSaeFileNames) {
            LOG.info("execute, processing: " + saeFileName);
            try {
                success = processCurrentFileAndExtractPdpFeedFromSAEFile(saeFileName) && success;
            } catch (Exception e) {
                success = false;
                LOG.error("execute, there was an error processing a file: ", e);
            } finally {
                getFileStorageService().removeDoneFiles(Collections.singletonList(saeFileName));
            }
        }
        return success;
    }

    protected boolean processCurrentFileAndExtractPdpFeedFromSAEFile(String fileName) {
        boolean success = true;
        LOG.debug("processCurrentFileAndExtractPdpFeedFromSAEFile, current File: " + fileName);
        try {
            ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
                    .parseStandardAccoutingExtractFileToStandardAccountingExtractFile(fileName);
            success = getConcurStandardAccountingExtractService()
                    .extractPdpFeedFromStandardAccounitngExtract(concurStandardAccoutingExtractFile);
        } catch (ValidationException ve) {
            success = false;
            LOG.error("processCurrentFileAndExtractPdpFeedFromSAEFile, There was a validation error processing " + fileName, ve);
        }
        return success;
    }

    public ConcurStandardAccountingExtractService getConcurStandardAccountingExtractService() {
        return concurStandardAccountingExtractService;
    }

    public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
        this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
}
