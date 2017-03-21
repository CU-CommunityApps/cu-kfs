package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractValidationService {
    
    boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);
    
    boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);
    
    boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);
    
    boolean validateDebitCreditField(String debitCredit);
    
    boolean validateDate(Date date);
    
    public boolean validateEmployeeId(String employeeId);
    
    boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean validateEmployeeGroupId(String employeeGroupId);
}
