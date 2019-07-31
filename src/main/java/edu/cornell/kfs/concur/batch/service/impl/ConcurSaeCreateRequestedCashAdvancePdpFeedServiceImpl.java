package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.FileStorageException;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceFileService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvancePdpFeedService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceReportService;

public class ConcurSaeCreateRequestedCashAdvancePdpFeedServiceImpl implements ConcurSaeCreateRequestedCashAdvancePdpFeedService {
    private static final Logger LOG = LogManager.getLogger(ConcurSaeCreateRequestedCashAdvancePdpFeedServiceImpl.class);
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurSaeCreateRequestedCashAdvanceFileService concurSaeCreateRequestedCashAdvanceFileService;
    protected ConcurSaeCreateRequestedCashAdvanceReportService concurSaeCreateRequestedCashAdvanceReportService;

    @Override
    public void createPdpFeedsFromSaeRequestedCashAdvances() {
        if (getConcurBatchUtilityService().shouldProcessRequestedCashAdvancesFromSaeData()) {
            List<String> filesToProcess = getConcurSaeCreateRequestedCashAdvanceFileService().getUnprocessedSaeFiles();
            if (filesToProcess.isEmpty()) {
                LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: No SAE files found for cash advance request processing.");
                getConcurSaeCreateRequestedCashAdvanceReportService().sendEmailThatNoFileWasProcesed();
            } else {
                LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: Found " + filesToProcess.size() + " file(s) to process.");
    
                for (String standardAccountingExtractFullyQualifiedFileName : filesToProcess) {
                    try {
                        LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: Begin processing for filename: " + standardAccountingExtractFullyQualifiedFileName + ".");
                        if (getConcurSaeCreateRequestedCashAdvanceFileService().processFile(standardAccountingExtractFullyQualifiedFileName)) {
                            LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: SUCCESSFUL processing of cash advances from SAE File: " + standardAccountingExtractFullyQualifiedFileName);
                        }
                        else {
                            LOG.error("createPdpFeedsFromSaeRequetedCashAdvances: Cash Advance Creation processing was UNSUCCESSFUL for SAE File: " + standardAccountingExtractFullyQualifiedFileName);
                        }
                    } catch (Exception e) {
                        LOG.error("createPdpFeedsFromSaeRequetedCashAdvances: Processing to create cash advance PDP Files from SAE File [" + standardAccountingExtractFullyQualifiedFileName + "] generated Exception: " + e);
                        e.printStackTrace();
                    } finally {
                        LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: SAE cash advance creation processing does NOT remove .done file.");
                    }
                }
            }
        } else {
            LOG.info("createPdpFeedsFromSaeRequetedCashAdvances: KFS System parameter is NOT set to process requested cash advances from the SAE data.");
        }
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public ConcurSaeCreateRequestedCashAdvanceFileService getConcurSaeCreateRequestedCashAdvanceFileService() {
        return concurSaeCreateRequestedCashAdvanceFileService;
    }

    public void setConcurSaeCreateRequestedCashAdvanceFileService(
            ConcurSaeCreateRequestedCashAdvanceFileService concurSaeCreateRequestedCashAdvanceFileService) {
        this.concurSaeCreateRequestedCashAdvanceFileService = concurSaeCreateRequestedCashAdvanceFileService;
    }

    public ConcurSaeCreateRequestedCashAdvanceReportService getConcurSaeCreateRequestedCashAdvanceReportService() {
        return concurSaeCreateRequestedCashAdvanceReportService;
    }

    public void setConcurSaeCreateRequestedCashAdvanceReportService(
            ConcurSaeCreateRequestedCashAdvanceReportService concurSaeCreateRequestedCashAdvanceReportService) {
        this.concurSaeCreateRequestedCashAdvanceReportService = concurSaeCreateRequestedCashAdvanceReportService;
    }

}
