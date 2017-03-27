package edu.cornell.kfs.concur.batch.businessobject;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;

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
        sb.append("sequenceNumber: ").append(sequenceNumber).append(ConcurConstants.SPACING_STRING_FOR_OUTPUT);
        sb.append("requestId: ").append(requestId).append(ConcurConstants.SPACING_STRING_FOR_OUTPUT);
        return sb.toString();
    }

}
