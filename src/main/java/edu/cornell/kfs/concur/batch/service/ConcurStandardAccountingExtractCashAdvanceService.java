package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public interface ConcurStandardAccountingExtractCashAdvanceService {
    
    boolean isCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine line);
    
    ConcurAccountInfo findAccountingInfoForCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line, 
            List<ConcurStandardAccountingExtractDetailLine> saeLines);
    
}
