package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public interface ConcurStandardAccountingExtractCreateCollectorFileService {

    /**
     * @throws IllegalArgumentException if saeFileContents or reportData are null.
     */
    String buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents, ConcurStandardAccountingExtractBatchReportData reportData);

}
