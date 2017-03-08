package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.ConcurStandardAccountingExtractFileSummary;

/**
 * Helper service that sends notifications when an SAE file processing attempt succeeds or fails.
 */
public interface ConcurStandardAccountingExtractReportService {

    void reportSuccessfullyProcessedFile(String saeFileName, ConcurStandardAccountingExtractFileSummary summary);

    void reportFileProcessingFailure(String saeFileName, String failureReason);
}
