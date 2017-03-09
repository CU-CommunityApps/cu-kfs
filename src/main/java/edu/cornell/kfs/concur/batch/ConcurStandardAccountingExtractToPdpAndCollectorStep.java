package edu.cornell.kfs.concur.batch;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractToPdpAndCollectorStep extends AbstractStep {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractToPdpAndCollectorStep.class);
    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
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
                LOG.error("execute, there was an unexpected error processing a file: ", e);
            } finally {
                getFileStorageService().removeDoneFiles(Collections.singletonList(saeFileName));
            }
        }
        return success;
    }

    protected boolean processCurrentFileAndExtractPdpFeedFromSAEFile(String saeFileName) {
        boolean success = true;
        LOG.debug("processCurrentFileAndExtractPdpFeedFromSAEFile, current File: " + saeFileName);
        ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
                .parseStandardAccoutingExtractFile(saeFileName);
        if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountExtractFile(concurStandardAccoutingExtractFile)) {
            success = getConcurStandardAccountingExtractService().extractPdpFeedFromStandardAccounitngExtract(concurStandardAccoutingExtractFile);
            if (success) {
                success &= getConcurStandardAccountingExtractService()
                        .extractCollectorFeedFromStandardAccountingExtract(concurStandardAccoutingExtractFile);
            }
        } else {
            success = false;
            LOG.error("processCurrentFileAndExtractPdpFeedFromSAEFile, the SAE file failed high level validation.");
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

    public ConcurStandardAccountingExtractValidationService getConcurStandardAccountingExtractValidationService() {
        return concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractValidationService(
            ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
}
