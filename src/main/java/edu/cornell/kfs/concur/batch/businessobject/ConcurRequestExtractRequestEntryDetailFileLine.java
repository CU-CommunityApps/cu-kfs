package edu.cornell.kfs.concur.batch.businessobject;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.batch.ConcurRequestExtractPdpConstants;

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

    public String toString() {
        StringBuilder sb = new StringBuilder("ConcurRequestExtractRequestEntryDetailFileLine::").append(KFSConstants.NEWLINE);
        sb.append("sequenceNumber: ").append(sequenceNumber).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("requestId: ").append(requestId).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        return sb.toString();
    }

}
