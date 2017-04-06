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

    /**
     * If this method is used, any information placed in the ConcurStandardAccountingExtractBatchReportData
     * object WILL NOT be placed in the physical SAE PDP/Collector KFS batch processing report that
     * is generated when the Concur data is convert to the PDP and Collector files.
     * It will be the callers responsibility to maintain that batch reporting information if this method is used.
     */
    @Deprecated
    boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line);

    boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
    
    boolean validateEmployeeGroupId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData);
}
