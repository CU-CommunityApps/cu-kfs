package edu.cornell.kfs.fp.batch;

import java.util.Optional;

import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;

public class ProcurementCardTransactionResult {

    public static final ProcurementCardTransactionResult EMPTY = new ProcurementCardTransactionResult(null, null);

    private final Optional<ProcurementCardTransaction> transaction;
    private final Optional<ProcurementCardSkippedTransaction> skippedTransaction;

    public ProcurementCardTransactionResult(ProcurementCardTransaction transaction) {
        this(transaction, null);
    }

    public ProcurementCardTransactionResult(ProcurementCardSkippedTransaction skippedTransaction) {
        this(null, skippedTransaction);
    }

    private ProcurementCardTransactionResult(
            ProcurementCardTransaction transaction, ProcurementCardSkippedTransaction skippedTransaction) {
        this.transaction = Optional.ofNullable(transaction);
        this.skippedTransaction = Optional.ofNullable(skippedTransaction);
    }

    public boolean hasTransaction() {
        return transaction.isPresent();
    }

    public ProcurementCardTransaction getTransaction() {
        return transaction.get();
    }

    public boolean hasSkippedTransaction() {
        return skippedTransaction.isPresent();
    }

    public ProcurementCardSkippedTransaction getSkippedTransaction() {
        return skippedTransaction.get();
    }

}
