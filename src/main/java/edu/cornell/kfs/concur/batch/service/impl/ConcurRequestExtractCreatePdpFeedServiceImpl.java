package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.exception.FileStorageException;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpFeedService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractReportService;

public class ConcurRequestExtractCreatePdpFeedServiceImpl implements ConcurRequestExtractCreatePdpFeedService {
    private static final Logger LOG = LogManager.getLogger(ConcurRequestExtractCreatePdpFeedServiceImpl.class);
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurRequestExtractFileService concurRequestExtractFileService;
    protected ConcurRequestExtractReportService concurRequestExtractReportService;

    @Override
    public void createPdpFeedsFromRequestExtracts() {
        if (getConcurBatchUtilityService().shouldProcessRequestedCashAdvancesFromSaeData()) {
            LOG.info("createPdpFeedsFromRequestExtracts: KFS System parameter is SET to process requested cash advances using SAE data. BYPASSING requested cash advance creation using request extract data.");
        } else {
            List<String> filesToProcess = getConcurRequestExtractFileService().getUnprocessedRequestExtractFiles();
            if (filesToProcess.isEmpty()) {
                LOG.info("createPdpFeedsFromRequestExtracts: No Request Extract files found to process.");
                getConcurRequestExtractReportService().sendEmailThatNoFileWasProcesed();
            } else {
                LOG.info("createPdpFeedsFromRequestExtracts: Found " + filesToProcess.size() + " file(s) to process.");
    
                for (String requestExtractFullyQualifiedFileName : filesToProcess) {
                    try {
                        LOG.info("createPdpFeedsFromRequestExtracts: Begin processing for filename: " + requestExtractFullyQualifiedFileName + ".");
                        if (getConcurRequestExtractFileService().processFile(requestExtractFullyQualifiedFileName)) {
                            LOG.info("createPdpFeedsFromRequestExtracts: SUCCESSFUL processing for Request Extract File: " + requestExtractFullyQualifiedFileName);
                        }
                        else {
                            LOG.error("createPdpFeedsFromRequestExtracts: Processing was UNSUCCESSFUL for Request Extract File: " + requestExtractFullyQualifiedFileName);
                        }
                    } catch (Exception e) {
                        LOG.error("createPdpFeedsFromRequestExtracts: Processing to create PDP Files from Request Extract [" + requestExtractFullyQualifiedFileName + "] generated Exception: " + e.getMessage());
                    } finally {
                        getConcurBatchUtilityService().removeDoneFileFor(requestExtractFullyQualifiedFileName);
                    }
                }
            }
        }
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;
    }

    public ConcurRequestExtractFileService getConcurRequestExtractFileService() {
        return concurRequestExtractFileService;
    }

    public ConcurRequestExtractReportService getConcurRequestExtractReportService() {
        return concurRequestExtractReportService;
    }

    public void setConcurRequestExtractReportService(ConcurRequestExtractReportService concurRequestExtractReportService) {
        this.concurRequestExtractReportService = concurRequestExtractReportService;
    }

}
