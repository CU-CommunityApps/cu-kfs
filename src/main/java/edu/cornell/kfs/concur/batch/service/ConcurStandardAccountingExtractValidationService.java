package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractValidationService {
    
    void validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException;
    
    void validateAmounts(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException;
    
    void validateDebitCreditField(String debitCredit) throws ValidationException;
    
    void validateDate(Date date) throws ValidationException;

}
