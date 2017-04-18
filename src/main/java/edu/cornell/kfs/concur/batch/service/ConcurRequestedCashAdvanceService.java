package edu.cornell.kfs.concur.batch.service;

import java.util.Collection;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;

public interface ConcurRequestedCashAdvanceService {
    
    void saveConcurRequestedCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance);
    
    boolean isDuplicateConcurRequestCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance);

    Collection<ConcurRequestedCashAdvance> findConcurRequestedCashAdvanceBy(String cashAdvanceKey);

}
