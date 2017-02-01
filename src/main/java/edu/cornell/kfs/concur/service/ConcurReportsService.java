package edu.cornell.kfs.concur.service;

import edu.cornell.kfs.concur.businessobjects.ConcurReport;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public interface ConcurReportsService {
    
    ConcurReport extractConcurReport(String reportURI);

    void updateExpenseReportStatusInConcur(String workflowURI, ValidationResult validationResult);

}
