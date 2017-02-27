package edu.cornell.kfs.concur.batch.businessobject;

import java.util.List;

public class ConcurStandardAccountingExtractFile {
    
    private String headerType; 
    private String batchDate; 
    private String recordCount; 
    private String journalAmountTotal; 
    private String batchId; 
    private List<ConcurStandardAccountingExtractDetailLine> concurStandardAccountingExtractDetailLines;
    
    public String getHeaderType() {
        return headerType;
    }
    public void setHeaderType(String headerType) {
        this.headerType = headerType;
    }
    public String getBatchDate() {
        return batchDate;
    }
    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }
    public String getRecordCount() {
        return recordCount;
    }
    public void setRecordCount(String recordCount) {
        this.recordCount = recordCount;
    }
    public String getJournalAmountTotal() {
        return journalAmountTotal;
    }
    public void setJournalAmountTotal(String journalAmountTotal) {
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
        StringBuilder sb = new StringBuilder("headerType: ").append(headerType);
        sb.append(" batchDate: ").append(batchDate).append(" recordCount: ").append(recordCount);
        sb.append(" journalAmountTotal: ").append(journalAmountTotal).append(" batchId: ").append(batchId);
        return sb.toString();
    }
}
