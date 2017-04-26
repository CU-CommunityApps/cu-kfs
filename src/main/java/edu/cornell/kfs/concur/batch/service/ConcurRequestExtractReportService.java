package edu.cornell.kfs.concur.batch.service;

import java.io.File;

import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;

public interface ConcurRequestExtractReportService {
    
    File generateReport(ConcurRequestExtractBatchReportData reportData);

}
