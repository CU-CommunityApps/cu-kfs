package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractOverrideAccountingInfo;

public interface ConcurStandardAccountingExtractCashAdvanceService {
    
    boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line);
    
    ConcurStandardAccountingExtractOverrideAccountingInfo findAccountingInfoForCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line, 
            List<ConcurStandardAccountingExtractDetailLine> saeLines);
    
}
