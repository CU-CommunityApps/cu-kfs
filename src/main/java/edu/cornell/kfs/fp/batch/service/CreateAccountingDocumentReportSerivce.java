package edu.cornell.kfs.fp.batch.service;

import edu.cornell.kfs.fp.batch.CreateAccounntingDocumentReportItem;

public interface CreateAccountingDocumentReportSerivce {
    
    void generateReport(CreateAccounntingDocumentReportItem reportItem);
    
    void sendReportEmail(String toAddress, String fromAddress);

}
