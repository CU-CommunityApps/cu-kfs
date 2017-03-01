package edu.cornell.kfs.concur.batch.businessobject;

import java.util.ArrayList;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class ConcurRequestExtractRequestEntryDetailFileLine extends TransientBusinessObjectBase {
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
