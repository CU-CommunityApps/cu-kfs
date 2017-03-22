package edu.cornell.kfs.concur.batch.fixture;

import static edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils.buildOverride;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ConcurCollectorTestConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Helper fixture for generating ConcurStandardAccountingExtractDetailLine POJOs.
 * For convenience, some of the enum constructors allow for using another enum constant
 * as a base, and then overrides can be specified using the other constructor args or var-args.
 */
public enum ConcurSAEDetailLineFixture {

    DEFAULT_LINE(null, ConcurEmployeeFixture.JOHN_DOE, 1, ConcurTestConstants.REPORT_ID_1,
            ConcurConstants.SAE_OUT_OF_POCKET_PAYMENT_CODE, ConcurCollectorTestConstants.CHART_CODE,
            ConcurTestConstants.OBJ_6200, ConcurTestConstants.ACCT_1234321, null, null, null, null,
            ConcurConstants.SAE_DEBIT_CODE, 50.00, "12/24/2016"),

    MERGING_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.MERGING_TEST, 1),
    MERGING_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.MERGING_TEST, 2, ConcurConstants.SAE_DEBIT_CODE, 75.00),
    MERGING_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.MERGING_TEST, 3, ConcurConstants.SAE_DEBIT_CODE, 100.00),

    UNIQUENESS_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 1),
    UNIQUENESS_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 2,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ)),
    UNIQUENESS_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 3,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ConcurTestConstants.OBJ_7777)),
    UNIQUENESS_TEST_LINE4(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 4,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    UNIQUENESS_TEST_LINE5(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 5,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    UNIQUENESS_TEST_LINE6(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 6,
            buildOverride(LineField.SUB_OBJECT_CODE, ConcurTestConstants.SUB_OBJ_333)),
    UNIQUENESS_TEST_LINE7(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 7,
            buildOverride(LineField.PROJECT_CODE, ConcurTestConstants.PROJ_AA_778899)),
    UNIQUENESS_TEST_LINE8(DEFAULT_LINE, ConcurSAEFileFixture.UNIQUENESS_TEST, 8,
            buildOverride(LineField.ORG_REF_ID, ConcurTestConstants.ORG_REF_123ABC)),

    PAYMENT_CODE_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.PAYMENT_CODE_TEST, 1),
    PAYMENT_CODE_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.PAYMENT_CODE_TEST, 2,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655),
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.SAE_PRE_PAID_OR_OTHER_PAYMENT_CODE)),
    PAYMENT_CODE_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.PAYMENT_CODE_TEST, 3,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888),
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.SAE_UNIVERSITY_BILLED_OR_PAID_PAYMENT_CODE)),
    PAYMENT_CODE_TEST_LINE4(DEFAULT_LINE, ConcurSAEFileFixture.PAYMENT_CODE_TEST, 4,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579),
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.SAE_PSEUDO_PAYMENT_CODE)),
    PAYMENT_CODE_TEST_LINE5(DEFAULT_LINE, ConcurSAEFileFixture.PAYMENT_CODE_TEST, 5,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_24680),
            buildOverride(LineField.PAYMENT_CODE, ConcurTestConstants.UNRECOGNIZED_PAYMENT_CODE)),

    VALIDATION_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.VALIDATION_TEST, 1),
    VALIDATION_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.VALIDATION_TEST, 2,
            buildOverride(LineField.REPORT_ID, StringUtils.EMPTY)),
    VALIDATION_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.VALIDATION_TEST, 3,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, StringUtils.EMPTY)),

    DEBIT_CREDIT_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 1),
    DEBIT_CREDIT_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 2, ConcurConstants.SAE_CREDIT_CODE, -100.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 3, ConcurConstants.SAE_CREDIT_CODE, -25.55,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_LINE4(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 4, ConcurConstants.SAE_DEBIT_CODE, 12.33,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DEBIT_CREDIT_TEST_LINE5(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 5, ConcurConstants.SAE_CREDIT_CODE, -44.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),
    DEBIT_CREDIT_TEST_LINE6(DEFAULT_LINE, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 6, ConcurConstants.SAE_DEBIT_CODE, 75.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),

    PENDING_CLIENT_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.PENDING_CLIENT_TEST, 1, ConcurConstants.SAE_DEBIT_CODE, 60.00,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ConcurCollectorTestConstants.UNDEFINED_OBJECT_CODE)),
    PENDING_CLIENT_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.PENDING_CLIENT_TEST, 2,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ConcurCollectorTestConstants.DEFAULT_OBJECT_CODE)),

    FISCAL_YEAR_TEST1_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.FISCAL_YEAR_TEST1, 1),
    FISCAL_YEAR_TEST2_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.FISCAL_YEAR_TEST2, 1,
            buildOverride(LineField.REPORT_END_DATE, "05/19/2016")),
    FISCAL_YEAR_TEST3_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.FISCAL_YEAR_TEST3, 1,
            buildOverride(LineField.REPORT_END_DATE, "11/09/2016")),
    FISCAL_YEAR_TEST4_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.FISCAL_YEAR_TEST4, 1,
            buildOverride(LineField.REPORT_END_DATE, "06/29/2016")),
    FISCAL_YEAR_TEST5_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.FISCAL_YEAR_TEST5, 1,
            buildOverride(LineField.REPORT_END_DATE, "06/30/2016")),

    DOCUMENT_NUMBER_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST, 1),
    DOCUMENT_NUMBER_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST, 2,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DOCUMENT_NUMBER_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST, 3,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_3),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DOCUMENT_NUMBER_TEST_LINE4(DEFAULT_LINE, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST, 4,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_SHORT),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    SOME_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.GENERAL_TEST, 1),

    EMPLOYEE_NAME_TEST_LINE1(DEFAULT_LINE, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, 1),
    EMPLOYEE_NAME_TEST_LINE2(DEFAULT_LINE, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_FIRSTNAME, 2,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    EMPLOYEE_NAME_TEST_LINE3(DEFAULT_LINE, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_LASTNAME, 3,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    EMPLOYEE_NAME_TEST_LINE4(DEFAULT_LINE, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_FULLNAME, 4,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    FILE9_LINE1(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.JOHN_DOE, 1, "ABCDEFGHIJ1234567890",
            ConcurConstants.SAE_OUT_OF_POCKET_PAYMENT_CODE, ConcurCollectorTestConstants.CHART_CODE,
            ConcurTestConstants.OBJ_6200, ConcurTestConstants.ACCT_1234321, null, null,
            null, null, ConcurConstants.SAE_DEBIT_CODE, 50.00, "12/24/2016"),
    FILE9_LINE2(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.JOHN_DOE, 2, "11", "CASH",
            "0100", "IT", "1234321", null, null, null, null, "DR", 50.00, "2016-12-24"),
    FILE9_LINE3(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.JANE_DOE, 5, "11", "CASH",
            "0100", "IT", "1234321", null, null, null, null, "DR", 50.00, "2016-12-24"),
    FILE9_LINE4(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.DAN_SMITH, 6, "11", "CASH",
            "0100", "IT", "1234321", null, null, null, null, "DR", 50.00, "2016-12-24"),
    FILE9_LINE5(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.JANE_DOE, 7, "11", "CASH",
            "0100", "IT", "1234321", null, null, null, null, "DR", 50.00, "2016-12-24"),
    FILE9_LINE6(ConcurSAEFileFixture.GENERAL_TEST, ConcurEmployeeFixture.JANE_DOE, 19, "11", "CASH",
            "0100", "IT", "1234321", null, null, null, null, "DR", 50.00, "2016-12-24");

    public final ConcurSAEFileFixture extractFile;
    public final ConcurEmployeeFixture employee;
    public final String sequenceNumber;
    public final String reportId;
    public final String paymentCode;
    public final String chartOfAccountsCode;
    public final String journalAccountCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String subObjectCode;
    public final String orgRefId;
    public final String projectCode;
    public final String journalDebitCredit;
    public final double journalAmount;
    public final String reportEndDate;

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            int sequenceNumber, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, baseFixture.employee, sequenceNumber,
                baseFixture.journalDebitCredit, baseFixture.journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            int sequenceNumber, String journalDebitCredit, double journalAmount,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, baseFixture.employee, sequenceNumber, journalDebitCredit, journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, int sequenceNumber, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, employee, sequenceNumber,
                baseFixture.journalDebitCredit, baseFixture.journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, int sequenceNumber, String journalDebitCredit, double journalAmount,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, employee, sequenceNumber, journalDebitCredit, journalAmount,
                ConcurFixtureUtils.buildOverrideMap(LineField.class, overrides));
    }

    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, int sequenceNumber, String journalDebitCredit, double journalAmount,
            EnumMap<LineField,String> overrideMap) {
        this(extractFile,
                employee,
                sequenceNumber,
                overrideMap.getOrDefault(LineField.REPORT_ID, baseFixture.reportId),
                overrideMap.getOrDefault(LineField.PAYMENT_CODE, baseFixture.paymentCode),
                overrideMap.getOrDefault(LineField.CHART_OF_ACCOUNTS_CODE, baseFixture.chartOfAccountsCode),
                overrideMap.getOrDefault(LineField.JOURNAL_ACCOUNT_CODE, baseFixture.journalAccountCode),
                overrideMap.getOrDefault(LineField.ACCOUNT_NUMBER, baseFixture.accountNumber),
                overrideMap.getOrDefault(LineField.SUB_ACCOUNT_NUMBER, baseFixture.subAccountNumber),
                overrideMap.getOrDefault(LineField.SUB_OBJECT_CODE, baseFixture.subObjectCode),
                overrideMap.getOrDefault(LineField.PROJECT_CODE, baseFixture.projectCode),
                overrideMap.getOrDefault(LineField.ORG_REF_ID, baseFixture.orgRefId),
                journalDebitCredit,
                journalAmount,
                overrideMap.getOrDefault(LineField.REPORT_END_DATE, baseFixture.reportEndDate));
    }

    private ConcurSAEDetailLineFixture(ConcurSAEFileFixture extractFile, ConcurEmployeeFixture employee,
            int sequenceNumber, String reportId, String paymentCode, String chartOfAccountsCode,
            String journalAccountCode, String accountNumber, String subAccountNumber, String subObjectCode,
            String projectCode, String orgRefId, String journalDebitCredit, double journalAmount, String reportEndDate) {
        this.extractFile = extractFile;
        this.employee = employee;
        this.sequenceNumber = Integer.toString(sequenceNumber);
        this.reportId = reportId;
        this.paymentCode = paymentCode;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.journalAccountCode = journalAccountCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.journalDebitCredit = journalDebitCredit;
        this.journalAmount = journalAmount;
        this.reportEndDate = reportEndDate;
    }

    public ConcurStandardAccountingExtractDetailLine toDetailLine() {
        ConcurStandardAccountingExtractDetailLine detailLine = new ConcurStandardAccountingExtractDetailLine();
        
        detailLine.setBatchID(extractFile.batchId);
        detailLine.setBatchDate(ConcurFixtureUtils.toSqlDate(extractFile.batchDate));
        detailLine.setSequenceNumber(sequenceNumber);
        detailLine.setEmployeeId(employee.employeeId);
        detailLine.setEmployeeLastName(employee.lastName);
        detailLine.setEmployeeFirstName(employee.firstName);
        detailLine.setEmployeeMiddleInitital(employee.middleInitial);
        detailLine.setEmployeeGroupId(employee.groupId);
        detailLine.setEmployeeStatus(employee.status);
        detailLine.setReportId(reportId);
        detailLine.setPaymentCode(paymentCode);
        detailLine.setJournalAccountCode(journalAccountCode);
        detailLine.setChartOfAccountsCode(chartOfAccountsCode);
        detailLine.setAccountNumber(accountNumber);
        detailLine.setSubAccountNumber(subAccountNumber);
        detailLine.setSubObjectCode(subObjectCode);
        detailLine.setProjectCode(projectCode);
        detailLine.setOrgRefId(orgRefId);
        detailLine.setJounalDebitCredit(journalDebitCredit);
        detailLine.setJournalAmount(new KualiDecimal(journalAmount));
        detailLine.setReportEndDate(ConcurFixtureUtils.toSqlDate(reportEndDate));
        
        return detailLine;
    }

    // This getter is primarily meant for use as a method reference.
    public ConcurSAEFileFixture getExtractFile() {
        return extractFile;
    }

    /**
     * Helper enum containing all of the fields of the enclosing enum that can be overridden
     * via the helper constructors.
     */
    public enum LineField {
        REPORT_ID,
        PAYMENT_CODE,
        CHART_OF_ACCOUNTS_CODE,
        JOURNAL_ACCOUNT_CODE,
        ACCOUNT_NUMBER,
        SUB_ACCOUNT_NUMBER,
        SUB_OBJECT_CODE,
        ORG_REF_ID,
        PROJECT_CODE,
        REPORT_END_DATE;
    }

}
