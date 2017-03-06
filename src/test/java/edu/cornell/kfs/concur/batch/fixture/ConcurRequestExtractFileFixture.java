package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public enum ConcurRequestExtractFileFixture {
    
    GOOD_FILE("GOOD_FILE_COUNT_FILE", (new Date(Calendar.getInstance().getTimeInMillis())), new Integer(13), new KualiDecimal(5253.33)),
    BAD_FILE_COUNT_FILE("BAD_FILE_COUNT_FILE", (new Date(Calendar.getInstance().getTimeInMillis())), new Integer(4), new KualiDecimal(5253.33)),
    BAD_REQUEST_AMOUNT_FILE("BAD_REQUEST_AMOUNT_FILE", (new Date(Calendar.getInstance().getTimeInMillis())), new Integer(13), new KualiDecimal(9.87));
    
    public final String fileName;
    public final Date batchDate;
    public final Integer recordCount;
    public final KualiDecimal totalApprovedAmount;
    public final List<ConcurRequestExtractRequestDetailFileLine> requestDetails;
    
    private ConcurRequestExtractFileFixture (String fileName, Date batchDate, Integer recordCount, KualiDecimal totalApprovedAmount) {
        this.fileName = fileName;
        this.batchDate = batchDate;
        this.recordCount = recordCount;
        this.totalApprovedAmount = totalApprovedAmount;
        this.requestDetails = new ArrayList<ConcurRequestExtractRequestDetailFileLine>();
        this.requestDetails.add(ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_LINE_1.createConcurRequestExtractRequestDetailFileLine());
        this.requestDetails.add(ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_LINE_2.createConcurRequestExtractRequestDetailFileLine());
        this.requestDetails.add(ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_LINE_3.createConcurRequestExtractRequestDetailFileLine());
        this.requestDetails.add(ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_LINE_4.createConcurRequestExtractRequestDetailFileLine());
        this.requestDetails.add(ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_LINE_5.createConcurRequestExtractRequestDetailFileLine());
    }   
    
    public ConcurRequestExtractFile createConcurRequestExtractFile() {
        ConcurRequestExtractFile testFile = new ConcurRequestExtractFile();
        testFile.setFileName(this.fileName);
        testFile.setBatchDate(this.batchDate);
        testFile.setRecordCount(this.recordCount);
        testFile.setTotalApprovedAmount(this.totalApprovedAmount);
        testFile.setRequestDetails(this.requestDetails);
        return testFile;
    }
    
    public List<ConcurRequestExtractFile> createConcurRequestExtractFiles() {
        List<ConcurRequestExtractFile> requestExtractFiles = new ArrayList<ConcurRequestExtractFile>();
        requestExtractFiles.add(createConcurRequestExtractFile());
        return requestExtractFiles;
    }
}
