package edu.cornell.kfs.concur.batch.fixture;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;

public enum ConcurRequestedCashAdvanceFixture {
    CASH_ADVANCE_50(ConcurTestConstants.REQUEST_ID_1, ConcurEmployeeFixture.JOHN_DOE, 50.00, "12/01/2016",
            ConcurTestConstants.SOURCE_DOC_NUMBER_1, ConcurTestConstants.CASH_ADVANCE_KEY_1,
            ParameterTestValues.COLLECTOR_CHART_CODE, ConcurTestConstants.ACCT_4455667, null,
            ConcurTestConstants.OBJ_1414, null, null, null, ConcurTestConstants.TEST_FILE_NAME),
    CASH_ADVANCE_200(ConcurTestConstants.REQUEST_ID_2, ConcurEmployeeFixture.JOHN_DOE, 200.00, "12/02/2016",
            ConcurTestConstants.SOURCE_DOC_NUMBER_1, ConcurTestConstants.CASH_ADVANCE_KEY_2,
            ConcurTestConstants.CHART_QQ, ConcurTestConstants.ACCT_1234321, null,
            ConcurTestConstants.OBJ_1414, null, null, null, ConcurTestConstants.TEST_FILE_NAME);

    public final int concurRequestedCashAdvanceId;
    public final String requestId;
    public final ConcurEmployeeFixture employee;
    public final KualiDecimal paymentAmount;
    public final String paymentDate;
    public final String sourceDocNbr;
    public final String cashAdvanceKey;
    public final String chart;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final String fileName;

    private ConcurRequestedCashAdvanceFixture(String requestId, ConcurEmployeeFixture employee, double paymentAmount, String paymentDate,
            String sourceDocNbr, String cashAdvanceKey, String chart, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId, String fileName) {
        this.concurRequestedCashAdvanceId = ordinal() + 1;
        this.requestId = requestId;
        this.employee = employee;
        this.paymentAmount = new KualiDecimal(paymentAmount);
        this.paymentDate = paymentDate;
        this.sourceDocNbr = sourceDocNbr;
        this.cashAdvanceKey = cashAdvanceKey;
        this.chart = chart;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.fileName = fileName;
    }

    public ConcurRequestedCashAdvance toRequestedCashAdvance() {
        ConcurRequestedCashAdvance requestedCashAdvance = new ConcurRequestedCashAdvance();
        
        requestedCashAdvance.setConcurRequestedCashAdvanceId(concurRequestedCashAdvanceId);
        requestedCashAdvance.setRequestId(requestId);
        requestedCashAdvance.setEmployeeId(employee.employeeId);
        requestedCashAdvance.setPaymentAmount(paymentAmount);
        requestedCashAdvance.setPaymentDate(ConcurFixtureUtils.toSqlDate(paymentDate));
        requestedCashAdvance.setSourceDocNbr(sourceDocNbr);
        requestedCashAdvance.setCashAdvanceKey(cashAdvanceKey);
        requestedCashAdvance.setChart(chart);
        requestedCashAdvance.setAccountNumber(accountNumber);
        requestedCashAdvance.setSubAccountNumber(subAccountNumber);
        requestedCashAdvance.setObjectCode(objectCode);
        requestedCashAdvance.setSubObjectCode(subObjectCode);
        requestedCashAdvance.setProjectCode(projectCode);
        requestedCashAdvance.setOrgRefId(orgRefId);
        requestedCashAdvance.setFileName(fileName);
        
        return requestedCashAdvance;
    }

}
