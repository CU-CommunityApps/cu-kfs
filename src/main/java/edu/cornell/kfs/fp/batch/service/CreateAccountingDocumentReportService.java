package edu.cornell.kfs.fp.batch.service;

import java.util.List;

import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;

public interface CreateAccountingDocumentReportService {
    
    void generateReport(CreateAccountingDocumentReportItem reportItem);
    
    void sendReportEmail(String fromAddress, List<String> toAddresses);

}
