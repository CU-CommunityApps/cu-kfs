package edu.cornell.kfs.concur.batch.service;

import java.io.File;

import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;

public interface ConcurSaeCreateRequestedCashAdvanceReportService {
    
    File generateReport(ConcurSaeRequestedCashAdvanceBatchReportData reportData);
    
    void sendResultsEmail(ConcurSaeRequestedCashAdvanceBatchReportData reportData, File reportFile);
    
    void sendEmailThatNoFileWasProcesed();
}
