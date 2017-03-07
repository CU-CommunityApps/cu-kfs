package edu.cornell.kfs.concur.batch.businessobject;

public class ConcurRequestExtractRequestEntryDetailFileLine {
    private Integer sequenceNumber;
    private String requestId;
    
    public ConcurRequestExtractRequestEntryDetailFileLine() {
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
