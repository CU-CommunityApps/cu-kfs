package edu.cornell.kfs.pdp.service;

import org.kuali.kfs.pdp.businessobject.PaymentGroup;

public interface CuPendingTransactionService {
	
    /**
     * Creates GLPE entries for a check cancel and stores to PDP pending entry table. Debit/Credit codes are reversed backing
     * out the original GLPEs for the payment and reversing the entries for the source documents for DVCA, PREQ, CM.
     * 
     * @param paymentGroup payment group record to create GLPE for
     */
    public void generateCRCancellationGeneralLedgerPendingEntry(PaymentGroup paymentGroup);
    
    /**
     * Generates GLPE entries for a stale payment.
     * 
     * @param paymentGroup
     */
    public void generateStaleGeneralLedgerPendingEntry(PaymentGroup paymentGroup);

}
