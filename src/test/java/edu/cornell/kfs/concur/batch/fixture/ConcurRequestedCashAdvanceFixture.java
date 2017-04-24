package edu.cornell.kfs.concur.batch.fixture;

import java.util.EnumMap;
import java.util.Map;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;

public enum ConcurRequestedCashAdvanceFixture {
    TEST_CASH_ADVANCE_01("REQ01", ConcurEmployeeFixture.JOHN_DOE, 100.00, "12/01/2016", "DOC01", "CA01",
            "IT", "G222333", null, "1414", null, null, null, "test01.txt");

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

    @SafeVarargs
    private ConcurRequestedCashAdvanceFixture(
            ConcurRequestedCashAdvanceFixture baseFixture, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, baseFixture.paymentAmount.doubleValue(), overrides);
    }

    @SafeVarargs
    private ConcurRequestedCashAdvanceFixture(ConcurRequestedCashAdvanceFixture baseFixture, double paymentAmount,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, baseFixture.employee, paymentAmount, overrides);
    }

    @SafeVarargs
    private ConcurRequestedCashAdvanceFixture(ConcurRequestedCashAdvanceFixture baseFixture, ConcurEmployeeFixture employee,
            double paymentAmount, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, employee, paymentAmount, ConcurFixtureUtils.buildOverrideMap(LineField.class, overrides));
    }

    private ConcurRequestedCashAdvanceFixture(ConcurRequestedCashAdvanceFixture baseFixture, ConcurEmployeeFixture employee,
            double paymentAmount, EnumMap<LineField,String> overrideMap) {
        this(overrideMap.getOrDefault(LineField.REQUEST_ID, baseFixture.requestId),
                employee,
                paymentAmount,
                overrideMap.getOrDefault(LineField.PAYMENT_DATE, baseFixture.paymentDate),
                overrideMap.getOrDefault(LineField.SOURCE_DOC_NBR, baseFixture.sourceDocNbr),
                overrideMap.getOrDefault(LineField.CASH_ADVANCE_KEY, baseFixture.cashAdvanceKey),
                overrideMap.getOrDefault(LineField.CHART, baseFixture.chart),
                overrideMap.getOrDefault(LineField.ACCOUNT_NUMBER, baseFixture.accountNumber),
                overrideMap.getOrDefault(LineField.SUB_ACCOUNT_NUMBER, baseFixture.subAccountNumber),
                overrideMap.getOrDefault(LineField.OBJECT_CODE, baseFixture.objectCode),
                overrideMap.getOrDefault(LineField.SUB_OBJECT_CODE, baseFixture.subObjectCode),
                overrideMap.getOrDefault(LineField.PROJECT_CODE, baseFixture.projectCode),
                overrideMap.getOrDefault(LineField.ORG_REF_ID, baseFixture.orgRefId),
                overrideMap.getOrDefault(LineField.FILE_NAME, baseFixture.fileName));
    }

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

    /**
     * Helper enum containing all of the fields of the enclosing enum that can be overridden
     * via the helper constructors.
     */
    public enum LineField {
       REQUEST_ID,
       PAYMENT_DATE,
       SOURCE_DOC_NBR,
       CASH_ADVANCE_KEY,
       CHART,
       ACCOUNT_NUMBER,
       SUB_ACCOUNT_NUMBER,
       OBJECT_CODE,
       SUB_OBJECT_CODE,
       PROJECT_CODE,
       ORG_REF_ID,
       FILE_NAME;
    }

}
