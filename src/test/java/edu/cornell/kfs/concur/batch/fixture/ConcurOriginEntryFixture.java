package edu.cornell.kfs.concur.batch.fixture;

import static edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils.buildOverride;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;

/**
 * Helper fixture for generating OriginEntryFull business objects.
 * For convenience, some of the enum constructors allow for using another enum constant
 * as a base, and then overrides can be specified using the other constructor args or var-args.
 */
public enum ConcurOriginEntryFixture {

    DEFAULT_DEBIT(null, ParameterTestValues.COLLECTOR_CHART_CODE, ConcurTestConstants.ACCT_1234321, null,
            ConcurTestConstants.OBJ_6200, null, null, null, "CLTEABCDEFGHIJ", "Doe,John,12/24/2016",
            KFSConstants.GL_DEBIT_CODE, 50.00),
    DEFAULT_CREDIT(DEFAULT_DEBIT, null, KFSConstants.GL_CREDIT_CODE, 50.00,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_00001)),

    MERGING_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MERGING_TEST, 225.00),
    MERGING_TEST_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.MERGING_TEST, 225.00),

    UNIQUENESS_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ)),
    UNIQUENESS_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.OBJECT_CODE, ConcurTestConstants.OBJ_7777)),
    UNIQUENESS_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    UNIQUENESS_TEST_ENTRY5(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    UNIQUENESS_TEST_ENTRY6(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_OBJECT_CODE, ConcurTestConstants.SUB_OBJ_333)),
    UNIQUENESS_TEST_ENTRY7(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.PROJECT_CODE, ConcurTestConstants.PROJ_AA_778899)),
    UNIQUENESS_TEST_ENTRY8(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ORG_REF_ID, ConcurTestConstants.ORG_REF_123ABC)),
    UNIQUENESS_TEST_ENTRY9(DEFAULT_CREDIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST, 400.00),

    PAYMENT_CODE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST),
    PAYMENT_CODE_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    PAYMENT_CODE_TEST_ENTRY3(DEFAULT_CREDIT, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST, 100.00),

    VALIDATION_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.VALIDATION_TEST),
    VALIDATION_TEST_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.VALIDATION_TEST),

    DEBIT_CREDIT_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST),
    DEBIT_CREDIT_TEST_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 115.11,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 87.33,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DEBIT_CREDIT_TEST_ENTRY4(DEFAULT_CREDIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 22.22,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    PENDING_CLIENT_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST, 60.00,
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.OBJECT_CODE_OVERRIDE)),
    PENDING_CLIENT_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST,
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.OBJECT_CODE_OVERRIDE)),
    PENDING_CLIENT_TEST_ENTRY3(DEFAULT_CREDIT, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST, 110.00),

    FISCAL_YEAR_TEST1_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1),
    FISCAL_YEAR_TEST1_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1),
    FISCAL_YEAR_TEST2_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,05/19/2016")),
    FISCAL_YEAR_TEST2_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,05/19/2016")),
    FISCAL_YEAR_TEST3_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,11/09/2016")),
    FISCAL_YEAR_TEST3_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,11/09/2016")),
    FISCAL_YEAR_TEST4_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST4,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/29/2016")),
    FISCAL_YEAR_TEST4_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST4,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/29/2016")),
    FISCAL_YEAR_TEST5_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST5,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/30/2016")),
    FISCAL_YEAR_TEST5_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST5,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/30/2016")),

    DOCUMENT_NUMBER_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST),
    DOCUMENT_NUMBER_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DOCUMENT_NUMBER_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEZZYYXXVVWW"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DOCUMENT_NUMBER_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEGGGG5555"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),
    DOCUMENT_NUMBER_TEST_ENTRY5(DEFAULT_CREDIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST, 200.00),

    EMPLOYEE_NAME_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST),
    EMPLOYEE_NAME_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655),
            buildOverride(LineField.ENTRY_DESCRIPTION, "Jones,VeryLongFirstN,12/24/2016")),
    EMPLOYEE_NAME_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,Jack,12/24/2016")),
    EMPLOYEE_NAME_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,VeryLongFirstN,12/24/2016")),
    EMPLOYEE_NAME_TEST_ENTRY5(DEFAULT_CREDIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST, 200.00),

    GENERAL_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.GENERAL_TEST);

    public final ConcurCollectorBatchFixture collectorBatch;
    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String financialObjectCode;
    public final String financialSubObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final String documentNumber;
    public final String transactionLedgerEntryDescription;
    public final String transactionDebitCreditCode;
    public final double transactionLedgerEntryAmount;

    @SafeVarargs
    private ConcurOriginEntryFixture(ConcurOriginEntryFixture baseFixture, ConcurCollectorBatchFixture collectorBatch,
            Map.Entry<LineField,String>... overrides) {
        this(baseFixture, collectorBatch, baseFixture.transactionLedgerEntryAmount, overrides);
    }

    @SafeVarargs
    private ConcurOriginEntryFixture(ConcurOriginEntryFixture baseFixture, ConcurCollectorBatchFixture collectorBatch,
            double transactionLedgerEntryAmount, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, collectorBatch, baseFixture.transactionDebitCreditCode, transactionLedgerEntryAmount, overrides);
    }

    @SafeVarargs
    private ConcurOriginEntryFixture(ConcurOriginEntryFixture baseFixture, ConcurCollectorBatchFixture collectorBatch,
            String transactionDebitCreditCode, double transactionLedgerEntryAmount, Map.Entry<LineField,String>... overrides) {
        this(baseFixture, collectorBatch, transactionDebitCreditCode, transactionLedgerEntryAmount,
                ConcurFixtureUtils.buildOverrideMap(LineField.class, overrides));
    }

    private ConcurOriginEntryFixture(ConcurOriginEntryFixture baseFixture, ConcurCollectorBatchFixture collectorBatch,
            String transactionDebitCreditCode, double transactionLedgerEntryAmount, EnumMap<LineField,String> overrideMap) {
        this(collectorBatch,
                overrideMap.getOrDefault(LineField.CHART_OF_ACCOUNTS_CODE, baseFixture.chartOfAccountsCode),
                overrideMap.getOrDefault(LineField.ACCOUNT_NUMBER, baseFixture.accountNumber),
                overrideMap.getOrDefault(LineField.SUB_ACCOUNT_NUMBER, baseFixture.subAccountNumber),
                overrideMap.getOrDefault(LineField.OBJECT_CODE, baseFixture.financialObjectCode),
                overrideMap.getOrDefault(LineField.SUB_OBJECT_CODE, baseFixture.financialSubObjectCode),
                overrideMap.getOrDefault(LineField.PROJECT_CODE, baseFixture.projectCode),
                overrideMap.getOrDefault(LineField.ORG_REF_ID, baseFixture.orgRefId),
                overrideMap.getOrDefault(LineField.DOCUMENT_NUMBER, baseFixture.documentNumber),
                overrideMap.getOrDefault(LineField.ENTRY_DESCRIPTION, baseFixture.transactionLedgerEntryDescription),
                transactionDebitCreditCode,
                transactionLedgerEntryAmount);
    }

    private ConcurOriginEntryFixture(ConcurCollectorBatchFixture collectorBatch, String chartOfAccountsCode, String accountNumber,
            String subAccountNumber, String financialObjectCode, String financialSubObjectCode, String projectCode,
            String orgRefId, String documentNumber, String transactionLedgerEntryDescription,
            String transactionDebitCreditCode, double transactionLedgerEntryAmount) {
        this.collectorBatch = collectorBatch;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.financialObjectCode = financialObjectCode;
        this.financialSubObjectCode = financialSubObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.documentNumber = documentNumber;
        this.transactionLedgerEntryDescription = transactionLedgerEntryDescription;
        this.transactionDebitCreditCode = transactionDebitCreditCode;
        this.transactionLedgerEntryAmount = transactionLedgerEntryAmount;
    }

    public OriginEntryFull toOriginEntryFull() {
        OriginEntryFull originEntry = new OriginEntryFull();
        
        originEntry.setChartOfAccountsCode(chartOfAccountsCode);
        originEntry.setAccountNumber(accountNumber);
        originEntry.setSubAccountNumber(
                StringUtils.defaultIfBlank(subAccountNumber, ConcurTestConstants.DASH_SUB_ACCOUNT_NUMBER));
        originEntry.setFinancialObjectCode(financialObjectCode);
        originEntry.setFinancialSubObjectCode(
                StringUtils.defaultIfBlank(financialSubObjectCode, ConcurTestConstants.DASH_SUB_OBJECT_CODE));
        originEntry.setProjectCode(
                StringUtils.defaultIfBlank(projectCode, ConcurTestConstants.DASH_PROJECT_CODE));
        originEntry.setOrganizationReferenceId(
                StringUtils.defaultIfBlank(orgRefId, StringUtils.EMPTY));
        originEntry.setFinancialDocumentTypeCode(ParameterTestValues.COLLECTOR_DOCUMENT_TYPE);
        originEntry.setFinancialSystemOriginationCode(ParameterTestValues.COLLECTOR_SYSTEM_ORIGINATION_CODE);
        originEntry.setDocumentNumber(documentNumber);
        originEntry.setTransactionLedgerEntrySequenceNumber(collectorBatch.batchSequenceNumber);
        originEntry.setTransactionLedgerEntryDescription(transactionLedgerEntryDescription);
        originEntry.setTransactionDate(ConcurFixtureUtils.toSqlDate(collectorBatch.transmissionDate));
        originEntry.setTransactionDebitCreditCode(transactionDebitCreditCode);
        originEntry.setTransactionLedgerEntryAmount(new KualiDecimal(transactionLedgerEntryAmount));
        
        return originEntry;
    }

    // This getter is primarily meant for use as a method reference.
    public ConcurCollectorBatchFixture getCollectorBatch() {
        return collectorBatch;
    }

    /**
     * Helper enum containing all of the fields of the enclosing enum that can be overridden
     * via the helper constructors.
     */
    public enum LineField {
        CHART_OF_ACCOUNTS_CODE,
        ACCOUNT_NUMBER,
        SUB_ACCOUNT_NUMBER,
        OBJECT_CODE,
        SUB_OBJECT_CODE,
        PROJECT_CODE,
        ORG_REF_ID,
        DOCUMENT_NUMBER,
        ENTRY_DESCRIPTION;
    }

}
