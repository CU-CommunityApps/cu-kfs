package edu.cornell.kfs.concur.batch.report;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import org.kuali.kfs.sys.KFSConstants;

public class ConcurBatchReportSummaryItem {
    private String itemLabel;
    private int recordCount;
    private KualiDecimal dollarAmount;

    public ConcurBatchReportSummaryItem() {
        this.itemLabel = KFSConstants.EMPTY_STRING;
        this.recordCount = 0;
        this.dollarAmount = KualiDecimal.ZERO;
    }

    public ConcurBatchReportSummaryItem(String itemLabel, int recordCount, KualiDecimal dollarAmount) {
        this.itemLabel = itemLabel;
        this.recordCount = recordCount;
        this.dollarAmount = dollarAmount;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public void incrementRecordCount() {
        recordCount++;
    }

    public KualiDecimal getDollarAmount() {
        return dollarAmount;
    }

    public void setDollarAmount(KualiDecimal dollarAmount) {
        this.dollarAmount = dollarAmount;
    }

    public void addDollarAmount(KualiDecimal amountToAdd) {
        dollarAmount = dollarAmount.add(amountToAdd);
    }

}
