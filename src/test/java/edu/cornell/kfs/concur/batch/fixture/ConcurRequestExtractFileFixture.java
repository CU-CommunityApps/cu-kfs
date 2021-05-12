package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public enum ConcurRequestExtractFileFixture {
    
    GOOD_FILE("GOOD_FILE", "04/14/2017", 13, new KualiDecimal(5253.33),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()}),

    GOOD_FILE_MULTIPLE_DETAILS("GOOD_FILE_MULTIPLE_DETAILS", "04/14/2017", 21, new KualiDecimal(5253.33),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()}),

    BAD_FILE_COUNT_FILE("BAD_FILE_COUNT_FILE", "04/14/2017", 4, new KualiDecimal(5253.33),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()}),

    BAD_REQUEST_AMOUNT_FILE("BAD_REQUEST_AMOUNT_FILE", "04/14/2017", 13, new KualiDecimal(9.87),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()}),

    BAD_REQUEST_AMOUNT_MULTIPLE_DETAILS_FILE("BAD_REQUEST_AMOUNT_MULTIPLE_DETAILS_FILE", "04/14/2017", 15, new KualiDecimal(9.87),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()}),
    
    BAD_EMPLOYEE_GROUP_ID_FILE("BAD_EMPLOYEE_GROUP_ID_FILE", "04/14/2017", 13, new KualiDecimal(5253.33),
        new ConcurRequestExtractRequestDetailFileLine[]
            {ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_1.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_BAD_EMPLOYEE_GROUP_ID_LINE_2.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_3.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_4.createConcurRequestExtractRequestDetailFileLine(),
             ConcurRequestExtractRequestDetailFileLineFixture.REQUEST_DETAIL_GOOD_LINE_5.createConcurRequestExtractRequestDetailFileLine()});

    public final String fileName;
    public final Date batchDate;
    public final Integer recordCount;
    public final KualiDecimal totalApprovedAmount;
    public final List<ConcurRequestExtractRequestDetailFileLine> requestDetails;
    
    private ConcurRequestExtractFileFixture (String fileName, String batchDate, int recordCount,
                                             KualiDecimal totalApprovedAmount, ConcurRequestExtractRequestDetailFileLine[] detailLines) {
        this.fileName = fileName;
        this.batchDate = ConcurFixtureUtils.toSqlDate(batchDate);
        this.recordCount = new Integer(recordCount);
        this.totalApprovedAmount = totalApprovedAmount;
        this.requestDetails = new ArrayList<ConcurRequestExtractRequestDetailFileLine>();
        for (ConcurRequestExtractRequestDetailFileLine detailLine : detailLines) {
            this.requestDetails.add(detailLine);
        }
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
