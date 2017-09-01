package edu.cornell.kfs.concur.batch.fixture;

import static edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils.buildOverride;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.service.BalanceTypeService;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;

/**
 * Helper fixture for generating OriginEntryFull business objects.
 * For convenience, some of the enum constructors allow for using another enum constant
 * as a base, and then overrides can be specified using the other constructor args or var-args.
 * 
 * The setup of the transactionLedgerEntrySequenceNumber will be handled by ConcurCollectorBatchFixture
 * when it converts this enum's fixtures into OriginEntryFull objects.
 */
public enum ConcurOriginEntryFixture {

    DEFAULT_DEBIT(null, ParameterTestValues.COLLECTOR_CHART_CODE, ConcurTestConstants.ACCT_1234321, null,
            ConcurTestConstants.OBJ_6200, null, null, null, "CLTEABCDEFGHIJ", "Doe,John,12/24/2016",
            KFSConstants.GL_DEBIT_CODE, 50.00),
    DEFAULT_CREDIT(DEFAULT_DEBIT, null, KFSConstants.GL_CREDIT_CODE, 50.00),
    DEFAULT_CASH_OFFSET(DEFAULT_CREDIT, null,
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_PAYMENT_OFFSET_OBJECT_CODE)),
    DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT(DEFAULT_CASH_OFFSET, null, KFSConstants.GL_DEBIT_CODE, 50.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.DEFAULT_REPORT_ACCOUNT)),
    DEFAULT_PREPAID_OFFSET(DEFAULT_CREDIT, null,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_CHART_CODE),
            buildOverride(LineField.ACCOUNT_NUMBER, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER),
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_OBJECT_CODE)),
    DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT(DEFAULT_PREPAID_OFFSET, null),
    DEFAULT_CASH_ADVANCE_CREDIT(DEFAULT_CREDIT, null,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667),
            buildOverride(LineField.OBJECT_CODE, ConcurTestConstants.OBJ_1414)),
    DEFAULT_PERSONAL_OFFSET(DEFAULT_DEBIT, null,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.DEFAULT_REPORT_ACCOUNT),
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_PERSONAL_OFFSET_OBJECT_CODE)),
    DEFAULT_ATM_CASH_ADVANCE_CREDIT(DEFAULT_CASH_ADVANCE_CREDIT, null,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_CHART_CODE),
            buildOverride(LineField.ACCOUNT_NUMBER, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER),
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_OBJECT_CODE)),
    DEFAULT_ATM_FEE_DEBIT(DEFAULT_DEBIT, null,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ParameterTestValues.COLLECTOR_ATM_FEE_DEBIT_CHART_CODE),
            buildOverride(LineField.ACCOUNT_NUMBER, ParameterTestValues.COLLECTOR_ATM_FEE_DEBIT_ACCOUNT_NUMBER),
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_ATM_FEE_DEBIT_OBJECT_CODE)),
    DEFAULT_UNUSED_ATM_AMOUNT_OFFSET(DEFAULT_DEBIT, null,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_CHART_CODE),
            buildOverride(LineField.ACCOUNT_NUMBER, ParameterTestValues.COLLECTOR_PREPAID_OFFSET_ACCOUNT_NUMBER),
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.COLLECTOR_UNUSED_ATM_OFFSET_OBJECT_CODE)),

    MERGING_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MERGING_TEST, 225.00),
    MERGING_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MERGING_TEST, 225.00),

    UNIQUENESS_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ)),
    UNIQUENESS_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ)),
    UNIQUENESS_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.OBJECT_CODE, ConcurTestConstants.OBJ_7777)),
    UNIQUENESS_TEST_OFFSET3(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    UNIQUENESS_TEST_OFFSET4(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    UNIQUENESS_TEST_ENTRY5(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    UNIQUENESS_TEST_OFFSET5(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    UNIQUENESS_TEST_ENTRY6(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.SUB_OBJECT_CODE, ConcurTestConstants.SUB_OBJ_333)),
    UNIQUENESS_TEST_OFFSET6(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST),
    UNIQUENESS_TEST_ENTRY7(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.PROJECT_CODE, ConcurTestConstants.PROJ_AA_778899)),
    UNIQUENESS_TEST_OFFSET7(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.PROJECT_CODE, ConcurTestConstants.PROJ_AA_778899)),
    UNIQUENESS_TEST_ENTRY8(DEFAULT_DEBIT, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ORG_REF_ID, ConcurTestConstants.ORG_REF_123ABC)),
    UNIQUENESS_TEST_OFFSET8(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.UNIQUENESS_TEST,
            buildOverride(LineField.ORG_REF_ID, ConcurTestConstants.ORG_REF_123ABC)),

    PAYMENT_CODE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST),
    PAYMENT_CODE_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST),
    PAYMENT_CODE_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    PAYMENT_CODE_TEST_OFFSET2(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PAYMENT_CODE_TEST),

    VALIDATION_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.VALIDATION_TEST),
    VALIDATION_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.VALIDATION_TEST),

    DEBIT_CREDIT_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST),
    DEBIT_CREDIT_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST),
    DEBIT_CREDIT_TEST_ENTRY2(DEFAULT_CREDIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 115.11,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, KFSConstants.GL_DEBIT_CODE, 115.11,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DEBIT_CREDIT_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 92.33,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DEBIT_CREDIT_TEST_OFFSET3(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 92.33,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DEBIT_CREDIT_TEST_ENTRY4(DEFAULT_CREDIT, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, 22.22,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),
    DEBIT_CREDIT_TEST_OFFSET4(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DEBIT_CREDIT_TEST, KFSConstants.GL_DEBIT_CODE, 22.22,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    PENDING_CLIENT_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST, 60.00,
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.OBJECT_CODE_OVERRIDE)),
    PENDING_CLIENT_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST, 60.00),
    PENDING_CLIENT_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST,
            buildOverride(LineField.OBJECT_CODE, ParameterTestValues.OBJECT_CODE_OVERRIDE)),
    PENDING_CLIENT_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PENDING_CLIENT_TEST),

    FISCAL_YEAR_TEST1_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1),
    FISCAL_YEAR_TEST1_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1),
    FISCAL_YEAR_TEST2_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,05/19/2016")),
    FISCAL_YEAR_TEST2_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,05/19/2016")),
    FISCAL_YEAR_TEST3_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,11/09/2016")),
    FISCAL_YEAR_TEST3_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,11/09/2016")),
    FISCAL_YEAR_TEST4_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST4,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/29/2016")),
    FISCAL_YEAR_TEST4_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST4,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/29/2016")),
    FISCAL_YEAR_TEST5_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST5,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/30/2016")),
    FISCAL_YEAR_TEST5_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.FISCAL_YEAR_TEST5,
            buildOverride(LineField.ENTRY_DESCRIPTION, "Doe,John,06/30/2016")),

    DOCUMENT_NUMBER_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST),
    DOCUMENT_NUMBER_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST),
    DOCUMENT_NUMBER_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DOCUMENT_NUMBER_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655)),
    DOCUMENT_NUMBER_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEZZYYXXVVWW"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DOCUMENT_NUMBER_TEST_OFFSET3(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEZZYYXXVVWW"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888)),
    DOCUMENT_NUMBER_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEGGGG5555"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),
    DOCUMENT_NUMBER_TEST_OFFSET4(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.DOCUMENT_NUMBER_TEST,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEGGGG5555"),
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579)),

    EMPLOYEE_NAME_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST),
    EMPLOYEE_NAME_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST),
    EMPLOYEE_NAME_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655),
            buildOverride(LineField.ENTRY_DESCRIPTION, "Jones,VeryLongFirstN,12/24/2016")),
    EMPLOYEE_NAME_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_55655),
            buildOverride(LineField.ENTRY_DESCRIPTION, "Jones,VeryLongFirstN,12/24/2016")),
    EMPLOYEE_NAME_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,Jack,12/24/2016")),
    EMPLOYEE_NAME_TEST_OFFSET3(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_88888),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,Jack,12/24/2016")),
    EMPLOYEE_NAME_TEST_ENTRY4(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,VeryLongFirstN,12/24/2016")),
    EMPLOYEE_NAME_TEST_OFFSET4(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.EMPLOYEE_NAME_TEST,
            buildOverride(LineField.SUB_ACCOUNT_NUMBER, ConcurTestConstants.SUB_ACCT_13579),
            buildOverride(LineField.ENTRY_DESCRIPTION, "VeryLongLastNa,VeryLongFirstN,12/24/2016")),

    CASH_AND_CARD_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_AND_CARD_TEST, 100.00),
    CASH_AND_CARD_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.CASH_AND_CARD_TEST, 100.00),
    CASH_AND_CARD_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_AND_CARD_TEST, 5.00),
    CASH_AND_CARD_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.CASH_AND_CARD_TEST, 5.00),
    
    CANCELED_TRIP_TEST_ENTRY1(DEFAULT_CREDIT, ConcurCollectorBatchFixture.CANCELED_TRIP_TEST, 432.40),
    CANCELED_TRIP_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.CANCELED_TRIP_TEST,
            KFSConstants.GL_DEBIT_CODE, 432.40),

    FULL_USE_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.FULL_USE_CASH_ADVANCE_TEST),
    FULL_USE_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.FULL_USE_CASH_ADVANCE_TEST),

    PARTIAL_USE_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PARTIAL_USE_CASH_ADVANCE_TEST, 30.00),
    PARTIAL_USE_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.PARTIAL_USE_CASH_ADVANCE_TEST, 30.00),

    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST),
    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, 15.00),
    EXPENSE_EXCEEDS_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT,
            ConcurCollectorBatchFixture.EXPENSE_EXCEEDS_CASH_ADVANCE_TEST, 35.00),

    MULTIPLE_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST, 40.00),
    MULTIPLE_CASH_ADVANCE_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST, 40.00),
    MULTIPLE_CASH_ADVANCE_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST),
    MULTIPLE_CASH_ADVANCE_TEST_CA_ENTRY2(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST),
    MULTIPLE_CASH_ADVANCE_TEST_ENTRY3(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK")),
    MULTIPLE_CASH_ADVANCE_TEST_CA_ENTRY3(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MULTIPLE_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK"),
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ),
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_1234321)),

    ORPHANED_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.ORPHANED_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK")),
    ORPHANED_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.ORPHANED_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.DOCUMENT_NUMBER, "CLTEJJJJJKKKKK"),
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ),
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_1234321)),

    MIXED_EXPENSES_CASH_ADVANCE_TEST1_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1),
    MIXED_EXPENSES_CASH_ADVANCE_TEST1_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST1_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST1_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST1),

    MIXED_EXPENSES_CASH_ADVANCE_TEST2_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2, 40.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST2_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST2_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST2_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST2),

    MIXED_EXPENSES_CASH_ADVANCE_TEST3_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3, 55.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST3_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3, 5.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST3_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST3_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST3_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST3),

    MIXED_EXPENSES_CASH_ADVANCE_TEST4_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4),
    MIXED_EXPENSES_CASH_ADVANCE_TEST4_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4, 10.00),
    MIXED_EXPENSES_CASH_ADVANCE_TEST4_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4, 60.00,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST4_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4,
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_4455667)),
    MIXED_EXPENSES_CASH_ADVANCE_TEST4_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MIXED_EXPENSES_CASH_ADVANCE_TEST4),

    MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST, 325.00),
    MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST_OFFSET1(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST, 75.00),
    MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST),
    MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST_CA_ENTRY2(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.MANY_SAME_ENTRY_ID_CASH_ADVANCE_TEST, 200.00,
            buildOverride(LineField.CHART_OF_ACCOUNTS_CODE, ConcurTestConstants.CHART_QQ),
            buildOverride(LineField.ACCOUNT_NUMBER, ConcurTestConstants.ACCT_1234321)),

    PERSONAL_WITHOUT_CASH_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_WITHOUT_CASH_TEST, 65.00),
    PERSONAL_WITHOUT_CASH_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PERSONAL_WITHOUT_CASH_TEST, 65.00),
    PERSONAL_WITHOUT_CASH_TEST_OFFSET2(DEFAULT_PERSONAL_OFFSET, ConcurCollectorBatchFixture.PERSONAL_WITHOUT_CASH_TEST),
    PERSONAL_WITHOUT_CASH_TEST_OFFSET3(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.PERSONAL_WITHOUT_CASH_TEST),

    CASH_EXCEEDS_PERSONAL_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST, 65.00),
    CASH_EXCEEDS_PERSONAL_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST, 65.00),
    CASH_EXCEEDS_PERSONAL_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST, 77.88),
    CASH_EXCEEDS_PERSONAL_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST, 77.88),
    CASH_EXCEEDS_PERSONAL_TEST_OFFSET3(DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST),
    CASH_EXCEEDS_PERSONAL_TEST_OFFSET4(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.CASH_EXCEEDS_PERSONAL_TEST),

    CASH_EQUALS_PERSONAL_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 65.00),
    CASH_EQUALS_PERSONAL_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 65.00),
    CASH_EQUALS_PERSONAL_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 77.88),
    CASH_EQUALS_PERSONAL_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 77.88),
    CASH_EQUALS_PERSONAL_TEST_OFFSET3(DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 77.88),
    CASH_EQUALS_PERSONAL_TEST_OFFSET4(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.CASH_EQUALS_PERSONAL_TEST, 77.88),

    PERSONAL_EXCEEDS_CASH_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 65.00),
    PERSONAL_EXCEEDS_CASH_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 65.00),
    PERSONAL_EXCEEDS_CASH_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 66.88),
    PERSONAL_EXCEEDS_CASH_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 66.88),
    PERSONAL_EXCEEDS_CASH_TEST_OFFSET3(DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 66.88),
    PERSONAL_EXCEEDS_CASH_TEST_OFFSET4(DEFAULT_PERSONAL_OFFSET, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 11.00),
    PERSONAL_EXCEEDS_CASH_TEST_OFFSET5(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.PERSONAL_EXCEEDS_CASH_TEST, 77.88),

    PERSONAL_AND_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 125.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 125.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 177.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 87.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 90.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_OFFSET3(DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 13.00),
    PERSONAL_AND_CASH_ADVANCE_TEST_OFFSET4(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT, ConcurCollectorBatchFixture.PERSONAL_AND_CASH_ADVANCE_TEST, 13.00),

    PERSONAL_CHARGE_AND_RETURN_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_RETURN_TEST, 16.00),
    PERSONAL_CHARGE_AND_RETURN_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_RETURN_TEST, 16.00),
    PERSONAL_CHARGE_AND_RETURN_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_RETURN_TEST, 75.00),
    PERSONAL_CHARGE_AND_RETURN_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_RETURN_TEST, 75.00),
    
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 16.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_OFFSET1(DEFAULT_PREPAID_OFFSET, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 16.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_ENTRY2(DEFAULT_DEBIT, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 75.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_OFFSET2(DEFAULT_CASH_OFFSET, ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 75.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_OFFSET3(DEFAULT_CASH_OFFSET_PERSONAL_ADJUSTMENT,
            ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 1.00),
    PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST_OFFSET4(DEFAULT_PREPAID_OFFSET_PERSONAL_ADJUSTMENT,
            ConcurCollectorBatchFixture.PERSONAL_CHARGE_AND_PARTIAL_RETURN_TEST, 1.00),

    ATM_CASH_ADVANCE_TEST_ENTRY1(DEFAULT_DEBIT, ConcurCollectorBatchFixture.ATM_CASH_ADVANCE_TEST, 4.00),
    ATM_CASH_ADVANCE_TEST_ENTRY2(DEFAULT_ATM_FEE_DEBIT, ConcurCollectorBatchFixture.ATM_CASH_ADVANCE_TEST, 3.50),
    ATM_CASH_ADVANCE_TEST_CA_ENTRY1(DEFAULT_ATM_CASH_ADVANCE_CREDIT, ConcurCollectorBatchFixture.ATM_CASH_ADVANCE_TEST, 13.50),
    ATM_CASH_ADVANCE_TEST_OFFSET1(DEFAULT_UNUSED_ATM_AMOUNT_OFFSET, ConcurCollectorBatchFixture.ATM_CASH_ADVANCE_TEST, 6.00);

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
        
        // Default constructor sets fiscal year to zero; need to forcibly clear it to match the setup in our Concur runtime code.
        originEntry.setUniversityFiscalYear(null);
        
        originEntry.setFinancialBalanceTypeCode(BalanceTypeService.ACTUAL_BALANCE_TYPE);
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
