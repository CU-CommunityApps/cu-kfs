package edu.cornell.kfs.fp.batch;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ProcurementCardSkippedTransaction {

    private int fileLineNumber;
    private String cardHolderName;
    private KualiDecimal transactionAmount;

    public int getFileLineNumber() {
        return fileLineNumber;
    }

    public void setFileLineNumber(int fileLineNumber) {
        this.fileLineNumber = fileLineNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public KualiDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(KualiDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

}
