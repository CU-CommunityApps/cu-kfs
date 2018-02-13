package edu.cornell.kfs.fp.batch.service;

import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;

public interface CreateAccountingDocumentReportService {
    
    void generateReport(CreateAccountingDocumentReportItem reportItem);
    
    void sendReportEmail(String toAddress, String fromAddress);

}
