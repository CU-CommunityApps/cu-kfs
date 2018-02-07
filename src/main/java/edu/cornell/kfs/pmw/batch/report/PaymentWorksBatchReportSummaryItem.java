package edu.cornell.kfs.pmw.batch.report;

import org.kuali.kfs.sys.KFSConstants;

public class PaymentWorksBatchReportSummaryItem {
    private String itemLabel;
    private int recordCount;
    
    public PaymentWorksBatchReportSummaryItem() {
        this.itemLabel = KFSConstants.EMPTY_STRING;
        this.recordCount = 0;
    }
    
    public PaymentWorksBatchReportSummaryItem (String itemLabel, int recordCount) {
        this.itemLabel = itemLabel;
        this.recordCount = recordCount;
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
}
