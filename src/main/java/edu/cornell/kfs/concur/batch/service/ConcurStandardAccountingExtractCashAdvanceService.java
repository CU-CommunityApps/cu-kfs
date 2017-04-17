package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

public interface ConcurStandardAccountingExtractCashAdvanceService {
    
    boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line);
    
}
