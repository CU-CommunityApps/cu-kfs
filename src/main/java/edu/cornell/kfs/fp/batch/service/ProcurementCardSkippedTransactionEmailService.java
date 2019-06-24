package edu.cornell.kfs.fp.batch.service;

import java.util.List;

import edu.cornell.kfs.fp.batch.ProcurementCardSkippedTransaction;

public interface ProcurementCardSkippedTransactionEmailService {

    void sendSkippedTransactionEmail(List<ProcurementCardSkippedTransaction> skippedTransactions);

}
