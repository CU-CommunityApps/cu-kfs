package edu.cornell.kfs.concur.batch.fixture;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;

public enum ConcurRequestExtractRequestDetailFileLineFixture {

    REQUEST_DETAIL_GOOD_LINE_1("04/14/2017", "34YA", new KualiDecimal(10.00), new KualiDecimal(10.00), "CORNELL", 1),
    REQUEST_DETAIL_GOOD_LINE_2("04/14/2017", "34YE", new KualiDecimal(40.00), new KualiDecimal(40.00), "CORNELL", 2),
    REQUEST_DETAIL_GOOD_LINE_3("04/14/2017", "34YH", new KualiDecimal(200.00), new KualiDecimal(200.00), "Athletics", 1),
    REQUEST_DETAIL_GOOD_LINE_4("04/14/2017", "34YK", new KualiDecimal(3.33), new KualiDecimal(3.33), "CORNELL", 3),
    REQUEST_DETAIL_GOOD_LINE_5("04/14/2017", "34YN", new KualiDecimal(5000.00), new KualiDecimal(5000.00), "Executive", 1),
    
    REQUEST_DETAIL_BAD_EMPLOYEE_GROUP_ID_LINE_2("04/14/2017", "34YE", new KualiDecimal(40.00), new KualiDecimal(40.00), "HARVARD", 2);

    public final Date batchDate;
    public final String requestId;
    public final KualiDecimal totalApprovedAmount;
    public final KualiDecimal requestAmount;
    public final String employeeGroupId;
    public final List<ConcurRequestExtractRequestEntryDetailFileLine> requestEntryDetails;    

    private ConcurRequestExtractRequestDetailFileLineFixture(String batchDate, String requestId, KualiDecimal totalApprovedAmount, KualiDecimal requestAmount, String employeeGroupId, int numberRequestEntryDetailLines) {
        this.batchDate = ConcurFixtureUtils.toSqlDate(batchDate);
        this.requestId = requestId;
        this.totalApprovedAmount = totalApprovedAmount;
        this.requestAmount = requestAmount;
        this.employeeGroupId = employeeGroupId;
        this.requestEntryDetails = new ArrayList<ConcurRequestExtractRequestEntryDetailFileLine>();
        for (int i=1; i<=numberRequestEntryDetailLines; i++) {
            this.requestEntryDetails.add(ConcurRequestExtractRequestEntryDetailFileLineFixture.BLANK_ENTRY_DETAIL_LINE.createConcurRequestExtractRequestEntryDetailFileLine(i, this.requestId));
        }
    }

    public ConcurRequestExtractRequestDetailFileLine createConcurRequestExtractRequestDetailFileLine() {
        ConcurRequestExtractRequestDetailFileLine testDetailFileLine = new ConcurRequestExtractRequestDetailFileLine();
        testDetailFileLine.setBatchDate(this.batchDate);
        testDetailFileLine.setRequestId(this.requestId);
        testDetailFileLine.setTotalApprovedAmount(this.totalApprovedAmount);
        testDetailFileLine.setRequestAmount(this.requestAmount);
        testDetailFileLine.setEmployeeGroupId(this.employeeGroupId);
        testDetailFileLine.setRequestEntryDetails(this.requestEntryDetails);
        return testDetailFileLine;
    }

}
