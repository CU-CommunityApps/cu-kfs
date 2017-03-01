package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.PhysicalFlatFileInformation;

import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;

public class ConcurRequestExtractCreatePdpFeedServiceImpl implements ConcurRequestExtractCreatePdpFeedService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpFeedServiceImpl.class);
    protected ConcurRequestExtractFileService concurRequestExtractFileService;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;

    @Override
    public void createPdpFeedsFromRequestExtracts() {
        List<String> filesToProcess = getConcurRequestExtractFileService().getUnprocessedRequestExtractFiles();
        if (filesToProcess.isEmpty()) {
            LOG.info("No Request Extract files found to process.");
        } else {
            LOG.info("Found " + filesToProcess.size() + " file(s) to process.");

            for (String requestExtractFileName : filesToProcess) {
                try {
                    LOG.info("Begin processing for filename: " + requestExtractFileName + ".");
                    createPdpFeedFromRequestExtract(requestExtractFileName);
                } finally {
                    getConcurRequestExtractFileService().performDoneFileTasks(requestExtractFileName);
                }
            }
        }
    }

    private void createPdpFeedFromRequestExtract(String requestExtractFileName) {
        if (getConcurRequestExtractFileService().processFile(requestExtractFileName)) {

            getConcurRequestExtractFileService().performAcceptedRequestExtractFileTasks(requestExtractFileName);
        } else {
            getConcurRequestExtractFileService().performRejectedRequestExtractFileTasks(requestExtractFileName);
        }
    }

    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;
    }

    public ConcurRequestExtractFileService getConcurRequestExtractFileService() {
        return concurRequestExtractFileService;
    }

    public void setConcurRequestExtractFileValidationService(ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }

    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return concurRequestExtractFileValidationService;
    }
}
