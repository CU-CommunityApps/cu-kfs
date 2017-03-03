package edu.cornell.kfs.concur.batch.fixture;


import java.sql.Date;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public enum ConcurRequestExtractRequestEntryDetailFileLineFixture {
    
    BLANK_ENTRY_DETAIL_LINE(new Integer(0), "");
    
    public Integer sequenceNumber;
    public String requestId;
    
    private ConcurRequestExtractRequestEntryDetailFileLineFixture(Integer sequenceNumber, String requestId) {
        this.sequenceNumber = sequenceNumber;
        this.requestId = requestId;
    }
    
    public ConcurRequestExtractRequestEntryDetailFileLine createConcurRequestExtractRequestEntryDetailFileLine() {
        ConcurRequestExtractRequestEntryDetailFileLine testEntryDetailFileLine = new ConcurRequestExtractRequestEntryDetailFileLine();
        testEntryDetailFileLine.setSequenceNumber(this.sequenceNumber);
        testEntryDetailFileLine.setRequestId(this.requestId);
        return testEntryDetailFileLine;
    }
    
    public ConcurRequestExtractRequestEntryDetailFileLine createConcurRequestExtractRequestEntryDetailFileLine(Integer sequenceNumber, String requestId) {
        ConcurRequestExtractRequestEntryDetailFileLine testEntryDetailFileLine = new ConcurRequestExtractRequestEntryDetailFileLine();
        testEntryDetailFileLine.setSequenceNumber(sequenceNumber);
        testEntryDetailFileLine.setRequestId(requestId);
        return testEntryDetailFileLine;
    }
    
}
