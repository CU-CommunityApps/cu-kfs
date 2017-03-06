package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurStandardAccountingExtractFile {

    private Date batchDate;
    private Integer recordCount;
    private KualiDecimal journalAmountTotal;
    private String batchId;
    private List<ConcurStandardAccountingExtractDetailLine> concurStandardAccountingExtractDetailLines;

    public ConcurStandardAccountingExtractFile() {
        concurStandardAccountingExtractDetailLines = new ArrayList<ConcurStandardAccountingExtractDetailLine>();
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public KualiDecimal getJournalAmountTotal() {
        return journalAmountTotal;
    }

    public void setJournalAmountTotal(KualiDecimal journalAmountTotal) {
        this.journalAmountTotal = journalAmountTotal;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public List<ConcurStandardAccountingExtractDetailLine> getConcurStandardAccountingExtractDetailLines() {
        return concurStandardAccountingExtractDetailLines;
    }

    public void setConcurStandardAccountingExtractDetailLines(
            List<ConcurStandardAccountingExtractDetailLine> concurStandardAccountingExtractDetailLines) {
        this.concurStandardAccountingExtractDetailLines = concurStandardAccountingExtractDetailLines;
    }

    public String getDebugInformation() {
        StringBuilder sb = new StringBuilder(" batchDate: ").append(batchDate).append(" recordCount: ")
                .append(recordCount);
        sb.append(" journalAmountTotal: ").append(journalAmountTotal).append(" batchId: ").append(batchId);
        return sb.toString();
    }
}
