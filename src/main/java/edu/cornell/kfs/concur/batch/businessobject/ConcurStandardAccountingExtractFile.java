package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class ConcurStandardAccountingExtractFile extends ConcurStandardAccountingExtractLineBase {

    private Date batchDate;
    private Integer recordCount;
    private KualiDecimal journalAmountTotal;
    private String batchId;
    private List<ConcurStandardAccountingExtractDetailLine> concurStandardAccountingExtractDetailLines;
    private String originalFileName;
    private String fullyQualifiedRequestedCashAdvancesPdpFileName;

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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFullyQualifiedRequestedCashAdvancesPdpFileName() {
        return fullyQualifiedRequestedCashAdvancesPdpFileName;
    }

    public void setFullyQualifiedRequestedCashAdvancesPdpFileName(String fullyQualifiedRequestedCashAdvancesPdpFileName) {
        this.fullyQualifiedRequestedCashAdvancesPdpFileName = fullyQualifiedRequestedCashAdvancesPdpFileName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(" batchDate: ").append(batchDate).append(" recordCount: ").append(recordCount);
        sb.append(" journalAmountTotal: ").append(journalAmountTotal).append(" batchId: ").append(batchId);
        sb.append(" originalFileName: ").append(originalFileName);
        sb.append(" fullyQualifiedRequestedCashAdvancesPdpFileName: ").append(fullyQualifiedRequestedCashAdvancesPdpFileName);
        return sb.toString();
    }
}
