package edu.cornell.kfs.concur.batch.report;

import org.kuali.rice.core.api.util.type.KualiDecimal;

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
        int count = this.getRecordCount();
        count++;
        this.setRecordCount(count);
    }

    public KualiDecimal getDollarAmount() {
        return dollarAmount;
    }

    public void setDollarAmount(KualiDecimal dollarAmount) {
        this.dollarAmount = dollarAmount;
    }

    public void addDollarAmount(KualiDecimal amountToAdd) {
        this.setDollarAmount(this.getDollarAmount().add(amountToAdd));
    }

}
