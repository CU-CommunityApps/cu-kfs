package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileValidationService;

public class ConcurRequestExtractCreatePdpFeedServiceImpl implements ConcurRequestExtractCreatePdpFeedService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpFeedServiceImpl.class);
    protected ConcurRequestExtractFileService concurRequestExtractFileService;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;

    @Override
    public void createPdpFeedsFromRequestExtracts() {
        List<String> filesToProcess = getUnprocessedRequestExtractFiles();
        if (filesToProcess.isEmpty()) {
            LOG.error("No Request Extract files found to process.");
        } else {
            for (String requestExtractFileName : filesToProcess) {
                createPdpFeedFromRequestExtract(requestExtractFileName);
                LOG.info("Request Extract File " + requestExtractFileName + " was processed.");
            }
        }
    }

    private void createPdpFeedFromRequestExtract(String requestExtractFileName) {
        if (getConcurRequestExtractFileService().requestExtractHeaderRowValidatesToFileContents(requestExtractFileName)) {
            LOG.debug("Request Extract file " + requestExtractFileName + "passed header row validation.");
            getConcurRequestExtractFileService().processRequestExtractFile(requestExtractFileName);
            getConcurRequestExtractFileService().performAcceptedRequestExtractFileTasks(requestExtractFileName);
        } else {
            LOG.error("Request Extract file " + requestExtractFileName + "header row does not match file contents.");
            getConcurRequestExtractFileService().performRejectedRequestExtractFileTasks(requestExtractFileName);
        }
    }

    private List<String> getUnprocessedRequestExtractFiles() {
        return new ArrayList<String>();
    }

    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;
    }

    public ConcurRequestExtractFileService getConcurRequestExtractFileService() {
        return concurRequestExtractFileService;
    }

    public void setConcurRequestExtractFileValidationService(
            ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }

    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return concurRequestExtractFileValidationService;
    }
}
