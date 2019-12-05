package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public interface ConcurStandardAccountingExtractCashAdvanceService {
    
    boolean isAtmCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isAtmFeeDebitLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isAtmFeeCreditLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isAtmCashAdvanceLineWithUnusedAmount(ConcurStandardAccountingExtractDetailLine line);
    
    ConcurAccountInfo findAccountingInfoForCashAdvanceLine(ConcurStandardAccountingExtractDetailLine line, 
            List<ConcurStandardAccountingExtractDetailLine> saeLines);
    
    boolean isPreTripCashAdvanceRequestLine(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isPreTripCashAdvanceIssuedByCashAdmin(ConcurStandardAccountingExtractDetailLine line);
    
    boolean isCashAdvanceToBeAppliedToReimbursement(ConcurStandardAccountingExtractDetailLine line);
}
