package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public enum ConcurRequestExtractRequestDetailFileLineFixture {
    
    REQUEST_DETAIL_LINE_1((new Date(Calendar.getInstance().getTimeInMillis())), new String("34YA"), new KualiDecimal(10.00), 1),
    REQUEST_DETAIL_LINE_2((new Date(Calendar.getInstance().getTimeInMillis())), new String("34YE"), new KualiDecimal(40.00), 2),
    REQUEST_DETAIL_LINE_3((new Date(Calendar.getInstance().getTimeInMillis())), new String("34YH"), new KualiDecimal(200.00), 1),
    REQUEST_DETAIL_LINE_4((new Date(Calendar.getInstance().getTimeInMillis())), new String("34YK"), new KualiDecimal(3.33), 3),
    REQUEST_DETAIL_LINE_5((new Date(Calendar.getInstance().getTimeInMillis())), new String("34YN"), new KualiDecimal(5000.00), 1);
    
    
    public final Date batchDate;
    public final String requestId;
    public final KualiDecimal requestAmount;
    public final List<ConcurRequestExtractRequestEntryDetailFileLine> requestEntryDetails;    
    
    private ConcurRequestExtractRequestDetailFileLineFixture(Date batchDate, String requestId, KualiDecimal requestAmount, int numberRequestEntryDetailLines) {
        this.batchDate = batchDate;
        this.requestId = requestId;
        this.requestAmount = requestAmount;
        this.requestEntryDetails = new ArrayList<ConcurRequestExtractRequestEntryDetailFileLine>();
        for (int i=1; i<=numberRequestEntryDetailLines; i++) {
            this.requestEntryDetails.add(ConcurRequestExtractRequestEntryDetailFileLineFixture.BLANK_ENTRY_DETAIL_LINE.createConcurRequestExtractRequestEntryDetailFileLine(i, this.requestId));
        }
    }

    public ConcurRequestExtractRequestDetailFileLine createConcurRequestExtractRequestDetailFileLine() {
        ConcurRequestExtractRequestDetailFileLine testDetailFileLine = new ConcurRequestExtractRequestDetailFileLine();
        testDetailFileLine.setBatchDate(this.batchDate);
        testDetailFileLine.setRequestId(this.requestId);
        testDetailFileLine.setRequestAmount(this.requestAmount);
        testDetailFileLine.setRequestEntryDetails(this.requestEntryDetails);
        return testDetailFileLine;
    }
    
}
