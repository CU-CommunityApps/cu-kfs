package edu.cornell.kfs.concur.batch.service;

import java.io.File;

import edu.cornell.kfs.concur.batch.report.ConcurEmailableReportData;

public interface ConcurReportEmailService {
    void sendResultsEmail(ConcurEmailableReportData reportData, File reportFile);
    void sendEmail(String subject, String body);
}
