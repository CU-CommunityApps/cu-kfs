package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;

public interface ConcurRequestedCashAdvanceService {
    
    void saveConcurRequestedCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance);
    
    boolean isDuplicateConcurRequestCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance);
    
}
