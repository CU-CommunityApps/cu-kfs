package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public interface ConcurStandardAccountingExtractValidationService {
    
    boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateDebitCreditField(String debitCredit);
    
    boolean validateDate(Date date);
    
    public boolean validateEmployeeId(String employeeId);
    
    boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean validateEmployeeGroupId(String employeeGroupId);
}
