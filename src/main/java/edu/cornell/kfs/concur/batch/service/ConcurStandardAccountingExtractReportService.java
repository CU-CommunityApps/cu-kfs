package edu.cornell.kfs.concur.batch.service;

import java.io.File;

import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public interface ConcurStandardAccountingExtractReportService {
    
    File generateReport(ConcurStandardAccountingExtractBatchReportData reportData);
    
    void sendResultsEmail(ConcurStandardAccountingExtractBatchReportData reportData, File reportFile);
    
    void sendEmailThatNoFileWasProcesed();

}
