package edu.cornell.kfs.concur.batch.service;

import java.sql.Date;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;

public interface ConcurStandardAccountingExtractValidationService {
    
    boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateDebitCreditField(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateDate(Date date);
    
    public boolean validateEmployeeId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateConcurStandardAccountingExtractDetailLineWithObjectCodeOverride(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateEmployeeGroupId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
}
