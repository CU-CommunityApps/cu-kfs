package edu.cornell.kfs.concur.batch.fixture;

import static edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils.buildOverride;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;

/**
 * Helper fixture for generating ConcurStandardAccountingExtractDetailLine POJOs.
 * For convenience, some of the enum constructors allow for using another enum constant
 * as a base, and then overrides can be specified using the other constructor args or var-args.
 * 
 * The setup of the line sequence number will be handled by ConcurSAEFileFixture
 * when it converts this enum's fixtures into ConcurStandardAccountingExtractDetailLine POJOs.
 * Also, the reportEntryIsPersonalFlag is being stored as a "Y"/"N" String, to allow
 * for simplified overriding via the related utility classes and methods.
 */
public enum ConcurSAEDetailLineFixture {

    DEFAULT_DEBIT(null, ConcurEmployeeFixture.JOHN_DOE, ConcurTestConstants.REPORT_ID_1,
            ConcurConstants.PAYMENT_CODE_CASH, ParameterTestValues.COLLECTOR_CHART_CODE,
            ConcurTestConstants.OBJ_6200, ConcurTestConstants.ACCT_1234321, null, null, null, null,
            ConcurConstants.DEBIT, 50.00, "12/24/2016", null, ConcurTestConstants.REPORT_ENTRY_ID_1,
            KRADConstants.NO_INDICATOR_VALUE, ParameterTestValues.COLLECTOR_CHART_CODE,
            ConcurTestConstants.DEFAULT_REPORT_ACCOUNT, null, null, null, null),
    DEFAULT_CREDIT(DEFAULT_DEBIT, null, ConcurConstants.CREDIT, -50.00),
    DEFAULT_CASH_ADVANCE(DEFAULT_CREDIT, null,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, StringUtils.EMPTY),
            buildOverride(LineField.ACCOUNT_NUMBER, StringUtils.EMPTY),
            buildOverride(LineField.CASH_ADVANCE_KEY, ConcurTestConstants.CASH_ADVANCE_KEY_1)),
    DEFAULT_CORP_CARD_DEBIT(DEFAULT_DEBIT, null,
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID)),
    DEFAULT_PERSONAL_DEBIT(DEFAULT_CORP_CARD_DEBIT, null,
            buildOverride(LineField.REPORT_ENTRY_IS_PERSONAL_FLAG, KRADConstants.YES_INDICATOR_VALUE),
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, StringUtils.EMPTY),
            buildOverride(LineField.ACCOUNT_NUMBER, StringUtils.EMPTY),
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_OBJECT_CODE)),
    DEFAULT_PERSONAL_CREDIT(DEFAULT_PERSONAL_DEBIT, null, ConcurConstants.CREDIT, -50.00),

    MERGING_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.MERGING_TEST),
    MERGING_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.MERGING_TEST, 75.00),
    MERGING_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.MERGING_TEST, 100.00),

    UNIQUENESS_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ)),
    UNIQUENESS_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ConcurTestConstants.OBJ_7777)),
    UNIQUENESS_TEST_LINE4(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    UNIQUENESS_TEST_LINE5(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    UNIQUENESS_TEST_LINE6(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_OBJECT_CODE, ConcurTestConstants.SUB_OBJ_333)),
    UNIQUENESS_TEST_LINE7(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.PROJECT_CODE, ConcurTestConstants.PROJ_AA_778899)),
    UNIQUENESS_TEST_LINE8(DEFAULT_DEBIT, ConcurSAEFileFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ORG_REF_ID, ConcurTestConstants.ORG_REF_123ABC)),

    PAYMENT_CODE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.PAYMENT_CODE_TEST),
    PAYMENT_CODE_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655),
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_PRE_PAID_OR_OTHER)),
    PAYMENT_CODE_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888),
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID)),
    PAYMENT_CODE_TEST_LINE4(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_PSEUDO)),
    PAYMENT_CODE_TEST_LINE5(DEFAULT_DEBIT, ConcurSAEFileFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_24680),
            buildOverride(LineField.PAYMENT_CODE, ConcurTestConstants.UNRECOGNIZED_PAYMENT_CODE)),

    VALIDATION_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.VALIDATION_TEST),
    VALIDATION_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.VALIDATION_TEST,
            buildOverride(LineField.REPORT_ID, StringUtils.EMPTY)),
    VALIDATION_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.VALIDATION_TEST,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, StringUtils.EMPTY)),

    DEBIT_CREDIT_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST),
    DEBIT_CREDIT_TEST_LINE2(DEFAULT_CREDIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, -100.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_LINE3(DEFAULT_CREDIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, -15.11,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_LINE4(DEFAULT_DEBIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 12.33,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DEBIT_CREDIT_TEST_LINE5(DEFAULT_CREDIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, -22.22,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),
    DEBIT_CREDIT_TEST_LINE6(DEFAULT_DEBIT, ConcurSAEFileFixture.DEBIT_CREDIT_TEST, 80.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),

    PENDING_CLIENT_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.PENDING_CLIENT_TEST, 60.00,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ConcurConstants.PENDING_CLIENT)),
    PENDING_CLIENT_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.PENDING_CLIENT_TEST,
            buildOverride(LineField.JOURNAL_ACCOUNT_CODE, ParameterTestValues.OBJECT_CODE_OVERRIDE)),

    FISCAL_YEAR_TEST1_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FISCAL_YEAR_TEST1),
    FISCAL_YEAR_TEST2_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FISCAL_YEAR_TEST2,
            buildOverride(LineField.REPORT_END_DATE, "05/19/2016")),
    FISCAL_YEAR_TEST3_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FISCAL_YEAR_TEST3,
            buildOverride(LineField.REPORT_END_DATE, "11/09/2016")),
    FISCAL_YEAR_TEST4_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FISCAL_YEAR_TEST4,
            buildOverride(LineField.REPORT_END_DATE, "06/29/2016")),
    FISCAL_YEAR_TEST5_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FISCAL_YEAR_TEST5,
            buildOverride(LineField.REPORT_END_DATE, "06/30/2016")),

    DOCUMENT_NUMBER_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST),
    DOCUMENT_NUMBER_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DOCUMENT_NUMBER_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_3),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DOCUMENT_NUMBER_TEST_LINE4(DEFAULT_DEBIT, ConcurSAEFileFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_SHORT),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    EMPLOYEE_NAME_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST),
    EMPLOYEE_NAME_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_FIRSTNAME,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    EMPLOYEE_NAME_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_LASTNAME,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    EMPLOYEE_NAME_TEST_LINE4(DEFAULT_DEBIT, ConcurSAEFileFixture.EMPLOYEE_NAME_TEST, ConcurEmployeeFixture.LONG_FULLNAME,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    CASH_AND_CARD_TEST_LINE1(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.CASH_AND_CARD_TEST, 100.00),
    CASH_AND_CARD_TEST_LINE2(DEFAULT_DEBIT, ConcurSAEFileFixture.CASH_AND_CARD_TEST, 5.00),

    CANCELED_TRIP_TEST_LINE1(DEFAULT_CREDIT, ConcurSAEFileFixture.CANCELED_TRIP_TEST, -442.40,
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_UNIVERSITY_BILLED_OR_PAID)),
    CANCELED_TRIP_TEST_LINE2(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.CANCELED_TRIP_TEST, 10.00),

    FULL_USE_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.FULL_USE_CASH_ADVANCE_TEST),
    FULL_USE_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.FULL_USE_CASH_ADVANCE_TEST),

    PARTIAL_USE_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.PARTIAL_USE_CASH_ADVANCE_TEST, 30.00),
    PARTIAL_USE_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.PARTIAL_USE_CASH_ADVANCE_TEST, -30.00),
    PARTIAL_USE_CASH_ADVANCE_TEST_LINE3(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.PARTIAL_USE_CASH_ADVANCE_TEST, -20.00,
            buildOverride(LineField.PAYMENT_CODE, ConcurConstants.PAYMENT_CODE_PSEUDO)),

    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, 35.00),
    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, -35.00),
    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, 15.00),

    MULTIPLE_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, 40.00),
    MULTIPLE_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, -40.00),
    MULTIPLE_CASH_ADVANCE_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, 10.00,
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_2)),
    MULTIPLE_CASH_ADVANCE_TEST_LINE4(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, -10.00,
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_2)),
    MULTIPLE_CASH_ADVANCE_TEST_LINE5(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, 40.00),
    MULTIPLE_CASH_ADVANCE_TEST_LINE6(DEFAULT_DEBIT, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_3)),
    MULTIPLE_CASH_ADVANCE_TEST_LINE7(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.MULTIPLE_CASH_ADVANCE_TEST, -200.00,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_3),
            buildOverride(LineField.CASH_ADVANCE_KEY, ConcurTestConstants.CASH_ADVANCE_KEY_2)),

    ORPHANED_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST),
    ORPHANED_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST),
    ORPHANED_CASH_ADVANCE_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST, 10.00,
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_2)),
    ORPHANED_CASH_ADVANCE_TEST_LINE4(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST, -10.00,
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_2),
            buildOverride(LineField.CASH_ADVANCE_KEY, ConcurTestConstants.CASH_ADVANCE_KEY_NONEXISTENT)),
    ORPHANED_CASH_ADVANCE_TEST_LINE5(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST, 40.00),
    ORPHANED_CASH_ADVANCE_TEST_LINE6(DEFAULT_DEBIT, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_3)),
    ORPHANED_CASH_ADVANCE_TEST_LINE7(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.ORPHANED_CASH_ADVANCE_TEST, -200.00,
            buildOverride(LineField.REPORT_ID, ConcurTestConstants.REPORT_ID_2),
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.REPORT_ENTRY_ID_3),
            buildOverride(LineField.CASH_ADVANCE_KEY, ConcurTestConstants.CASH_ADVANCE_KEY_2)),

    PERSONAL_WITHOUT_CASH_TEST_LINE1(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.PERSONAL_WITHOUT_CASH_TEST, 65.00),
    PERSONAL_WITHOUT_CASH_TEST_LINE2(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.PERSONAL_WITHOUT_CASH_TEST),
    PERSONAL_WITHOUT_CASH_TEST_LINE3(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.PERSONAL_WITHOUT_CASH_TEST),

    CASH_EXCEEDS_PERSONAL_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.CASH_EXCEEDS_PERSONAL_TEST, 77.88),
    CASH_EXCEEDS_PERSONAL_TEST_LINE2(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.CASH_EXCEEDS_PERSONAL_TEST, 65.00),
    CASH_EXCEEDS_PERSONAL_TEST_LINE3(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.CASH_EXCEEDS_PERSONAL_TEST),
    CASH_EXCEEDS_PERSONAL_TEST_LINE4(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.CASH_EXCEEDS_PERSONAL_TEST),

    CASH_EQUALS_PERSONAL_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST, 77.88),
    CASH_EQUALS_PERSONAL_TEST_LINE2(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST, 65.00),
    CASH_EQUALS_PERSONAL_TEST_LINE3(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST),
    CASH_EQUALS_PERSONAL_TEST_LINE4(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST),
    CASH_EQUALS_PERSONAL_TEST_LINE5(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST, 27.88),
    CASH_EQUALS_PERSONAL_TEST_LINE6(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.CASH_EQUALS_PERSONAL_TEST, -27.88),

    PERSONAL_EXCEEDS_CASH_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST, 66.88),
    PERSONAL_EXCEEDS_CASH_TEST_LINE2(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST, 65.00),
    PERSONAL_EXCEEDS_CASH_TEST_LINE3(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST),
    PERSONAL_EXCEEDS_CASH_TEST_LINE4(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST),
    PERSONAL_EXCEEDS_CASH_TEST_LINE5(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST, 27.88),
    PERSONAL_EXCEEDS_CASH_TEST_LINE6(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.PERSONAL_EXCEEDS_CASH_TEST, -27.88),

    PERSONAL_AND_CASH_ADVANCE_TEST_LINE1(DEFAULT_DEBIT, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 90.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_LINE2(DEFAULT_CASH_ADVANCE, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, -90.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_LINE3(DEFAULT_DEBIT, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 87.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_LINE4(DEFAULT_CORP_CARD_DEBIT, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 125.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_LINE5(DEFAULT_PERSONAL_DEBIT, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 13.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_LINE6(DEFAULT_PERSONAL_CREDIT, ConcurSAEFileFixture.PERSONAL_AND_CASH_ADVANCE_TEST, -13.00),

    PDP_TEST_CASH_ADVANCE_500(DEFAULT_CREDIT, ConcurSAEFileFixture.PDP_TEST, -500, buildOverride(LineField.CASH_ADVANCE_KEY, ConcurTestConstants.PDP_LINE_FIXTURE_CASH_ADVANCE_KEY),
            buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.PDP_LINE_FIXTURE_REPORT_ENTRY_ID), 
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, StringUtils.EMPTY), buildOverride(LineField.ACCOUNT_NUMBER, StringUtils.EMPTY)),
    PDP_TEST_DEBIT_1_50(DEFAULT_DEBIT, ConcurSAEFileFixture.PDP_TEST, 50, buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.PDP_LINE_FIXTURE_REPORT_ENTRY_ID)),
    PDP_TEST_DEBIT_2_500(DEFAULT_DEBIT, ConcurSAEFileFixture.PDP_TEST, 500, buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.PDP_LINE_FIXTURE_REPORT_ENTRY_ID)),
    PDP_TEST_DEBIT_3_500_NO_ACCOUNT(DEFAULT_DEBIT, ConcurSAEFileFixture.PDP_TEST, 500, buildOverride(LineField.REPORT_ENTRY_ID, ConcurTestConstants.PDP_LINE_FIXTURE_REPORT_ENTRY_ID + "-2"),
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, StringUtils.EMPTY), buildOverride(LineField.ACCOUNT_NUMBER, StringUtils.EMPTY));

    public final ConcurSAEFileFixture extractFile;
    public final ConcurEmployeeFixture employee;
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
    public final String cashAdvanceKey;
    public final String reportEntryId;
    public final String reportEntryIsPersonalFlag;
    public final String reportChartOfAccountsCode;
    public final String reportAccountNumber;
    public final String reportSubAccountNumber;
    public final String reportSubObjectCode;
    public final String reportProjectCode;
    public final String reportOrgRefId;

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, baseFixture.journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            double journalAmount, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, baseFixture.employee, baseFixture.journalDebitCredit, journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            String journalDebitCredit, double journalAmount,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, baseFixture.employee, journalDebitCredit, journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, employee,
                baseFixture.journalDebitCredit, baseFixture.journalAmount, overrides);
    }

    @SafeVarargs
    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, String journalDebitCredit, double journalAmount,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, extractFile, employee, journalDebitCredit, journalAmount,
                ConcurFixtureUtils.buildOverrideMap(LineField.class, overrides));
    }

    private ConcurSAEDetailLineFixture(ConcurSAEDetailLineFixture baseFixture, ConcurSAEFileFixture extractFile,
            ConcurEmployeeFixture employee, String journalDebitCredit, double journalAmount,
            EnumMap<LineField,String> overrideMap) {
        this(extractFile,
                employee,
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
                overrideMap.getOrDefault(LineField.REPORT_END_DATE, baseFixture.reportEndDate), 
                overrideMap.getOrDefault(LineField.CASH_ADVANCE_KEY, baseFixture.cashAdvanceKey),
                overrideMap.getOrDefault(LineField.REPORT_ENTRY_ID, baseFixture.reportEntryId),
                overrideMap.getOrDefault(LineField.REPORT_ENTRY_IS_PERSONAL_FLAG, baseFixture.reportEntryIsPersonalFlag),
                overrideMap.getOrDefault(LineField.REPORT_CHART_OF_ACCOUNTS_CODE, baseFixture.reportChartOfAccountsCode),
                overrideMap.getOrDefault(LineField.REPORT_ACCOUNT_NUMBER, baseFixture.reportAccountNumber),
                overrideMap.getOrDefault(LineField.REPORT_SUB_ACCOUNT_NUMBER, baseFixture.reportSubAccountNumber),
                overrideMap.getOrDefault(LineField.REPORT_SUB_OBJECT_CODE, baseFixture.reportSubObjectCode),
                overrideMap.getOrDefault(LineField.REPORT_PROJECT_CODE, baseFixture.reportProjectCode),
                overrideMap.getOrDefault(LineField.REPORT_ORG_REF_ID, baseFixture.reportOrgRefId));
    }

    private ConcurSAEDetailLineFixture(ConcurSAEFileFixture extractFile, ConcurEmployeeFixture employee,
            String reportId, String paymentCode, String chartOfAccountsCode,
            String journalAccountCode, String accountNumber, String subAccountNumber, String subObjectCode,
            String projectCode, String orgRefId, String journalDebitCredit, double journalAmount, String reportEndDate,
            String cashAdvanceKey, String reportEntryId, String reportEntryIsPersonalFlag,
            String reportChartOfAccountsCode, String reportAccountNumber, String reportSubAccountNumber,
            String reportSubObjectCode, String reportProjectCode, String reportOrgRefId) {
        this.extractFile = extractFile;
        this.employee = employee;
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
        this.cashAdvanceKey = cashAdvanceKey;
        this.reportEntryId = reportEntryId;
        this.reportEntryIsPersonalFlag = reportEntryIsPersonalFlag;
        this.reportChartOfAccountsCode = reportChartOfAccountsCode;
        this.reportAccountNumber = reportAccountNumber;
        this.reportSubAccountNumber = reportSubAccountNumber;
        this.reportSubObjectCode = reportSubObjectCode;
        this.reportProjectCode = reportProjectCode;
        this.reportOrgRefId = reportOrgRefId;
    }

    public ConcurStandardAccountingExtractDetailLine toDetailLine() {
        ConcurStandardAccountingExtractDetailLine detailLine = new ConcurStandardAccountingExtractDetailLine();
        
        boolean journalAccountCodeOverridden = ConcurConstants.PENDING_CLIENT.equals(journalAccountCode);
        String actualJournalAccountCode = journalAccountCodeOverridden ? ParameterTestValues.OBJECT_CODE_OVERRIDE : journalAccountCode;
        
        detailLine.setBatchID(extractFile.batchId);
        detailLine.setBatchDate(ConcurFixtureUtils.toSqlDate(extractFile.batchDate));
        detailLine.setEmployeeId(employee.employeeId);
        detailLine.setEmployeeLastName(employee.lastName);
        detailLine.setEmployeeFirstName(employee.firstName);
        detailLine.setEmployeeMiddleInitital(employee.middleInitial);
        detailLine.setEmployeeGroupId(employee.groupId);
        detailLine.setEmployeeStatus(employee.status);
        detailLine.setReportId(reportId);
        detailLine.setPaymentCode(paymentCode);
        detailLine.setJournalAccountCode(actualJournalAccountCode);
        detailLine.setJournalAccountCodeOverridden(Boolean.valueOf(journalAccountCodeOverridden));
        detailLine.setChartOfAccountsCode(chartOfAccountsCode);
        detailLine.setAccountNumber(accountNumber);
        detailLine.setSubAccountNumber(subAccountNumber);
        detailLine.setSubObjectCode(subObjectCode);
        detailLine.setProjectCode(projectCode);
        detailLine.setOrgRefId(orgRefId);
        detailLine.setJounalDebitCredit(journalDebitCredit);
        detailLine.setJournalAmount(new KualiDecimal(journalAmount));
        detailLine.setJournalAmountString(String.valueOf(journalAmount));
        detailLine.setReportEndDate(ConcurFixtureUtils.toSqlDate(reportEndDate));
        detailLine.setPolicy(ConcurTestConstants.DEFAULT_POLICY_NAME);
        detailLine.setExpenseType(ConcurTestConstants.DEFAULT_EXPENSE_TYPE_NAME);
        detailLine.setCashAdvanceKey(cashAdvanceKey);
        detailLine.setReportEntryId(reportEntryId);
        detailLine.setReportEntryIsPersonalFlag(getReportEntryIsPersonalFlagAsBoolean());
        detailLine.setReportChartOfAccountsCode(reportChartOfAccountsCode);
        detailLine.setReportAccountNumber(reportAccountNumber);
        detailLine.setReportSubAccountNumber(reportSubAccountNumber);
        detailLine.setReportSubObjectCode(reportSubObjectCode);
        detailLine.setReportProjectCode(reportProjectCode);
        detailLine.setReportOrgRefId(reportOrgRefId);
        return detailLine;
    }

    public Boolean getReportEntryIsPersonalFlagAsBoolean() {
        return Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(reportEntryIsPersonalFlag));
    }

    // This getter is primarily meant for use as a method reference.
    public String getReportId() {
        return reportId;
    }

    // This getter is primarily meant for use as a method reference.
    public ConcurSAEFileFixture getExtractFile() {
        return extractFile;
    }

    // This getter is primarily meant for use as a method reference.
    public double getJournalAmount() {
        return journalAmount;
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
        REPORT_END_DATE,
        CASH_ADVANCE_KEY,
        REPORT_ENTRY_ID,
        REPORT_ENTRY_IS_PERSONAL_FLAG,
        REPORT_CHART_OF_ACCOUNTS_CODE,
        REPORT_ACCOUNT_NUMBER,
        REPORT_SUB_ACCOUNT_NUMBER,
        REPORT_SUB_OBJECT_CODE,
        REPORT_PROJECT_CODE,
        REPORT_ORG_REF_ID;
    }

}
