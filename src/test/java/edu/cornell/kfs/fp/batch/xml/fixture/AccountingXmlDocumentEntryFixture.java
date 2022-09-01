package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import java.sql.Date;
import java.util.List;
import java.util.function.LongFunction;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeExpense;
import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.util.MockDocumentUtils;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@SuppressWarnings("deprecation")
public enum AccountingXmlDocumentEntryFixture {
    BASE_DOCUMENT(1, KFSConstants.ROOT_DOCUMENT_TYPE, "Test Document", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(), targetAccountingLines(), items(), notes(), adHocRecipients(), backupLinks(), CuDisbursementVoucherDocumentFixture.EMPTY),
    BAD_CONVERSION_DOCUMENT_PLACEHOLDER(1, KFSConstants.ROOT_DOCUMENT_TYPE, "Fail Conversion from XML",
            "Placeholder for documents that are expected to fail XML-to-doc conversion", "ABCD1234",
            sourceAccountingLines(), targetAccountingLines(), notes(), adHocRecipients(), backupLinks()),
    BAD_RULES_DOCUMENT_PLACEHOLDER(1, KFSConstants.ROOT_DOCUMENT_TYPE,
            CuFPTestConstants.BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR,
            "Placeholder for documents that are expected to fail business rule validation", "ABCD1234",
            sourceAccountingLines(), targetAccountingLines(), notes(), adHocRecipients(), backupLinks()),

    MULTI_DI_DOCUMENT_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_50),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE)),
    MULTI_DI_DOCUMENT_TEST_DOC2(
            2, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "Test Document 2", "This is another test document.", "GGGG4444",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(
                    "A fun testing note on the second DI",
                    "Another note on the second DI"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks()),
    MULTI_DI_DOCUMENT_TEST_DOC3(
            3, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "Test Document 3", "A third document for testing!!!", "ZYXW9876",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(),
            adHocRecipients(),
            backupLinks()),
    MULTI_DI_DOCUMENT_TEST_DOC4(
            4, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "auth backup Document", "Document Explanation", "OrgDoc4",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(),
            adHocRecipients(),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.AWS_BILLING_INVOICE,
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),

    SINGLE_DI_DOCUMENT_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_50),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE)),

    DI_FULL_ACCOUNT_LINE_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504701_OBJ_2641_AMOUNT_100_04),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504707_OBJ_2643_AMOUNT_100_04),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE)),

    DI_SINGLE_ELEMENT_LISTS_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100),
            notes(
                    "A fun testing note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),

    DI_EMPTY_ELEMENT_LISTS_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(),
            targetAccountingLines(),
            notes(),
            adHocRecipients(),
            backupLinks()),

    DI_WITHOUT_ELEMENT_LISTS_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(),
            targetAccountingLines(),
            notes(),
            adHocRecipients(),
            backupLinks()),

    BASE_IB_WITH_ITEMS(1, KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING,
            "First IB Test Document", "This is an example IB document with multiple items", "IntBill01",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254700_OBJ_4020_AMOUNT_100_INCOME1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263700_OBJ_1280_AMOUNT_50_INCOME2),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254710_OBJ_4020_AMOUNT_100_EXPENSE1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263600_OBJ_1280_AMOUNT_50_EXPENSE2),
            items(
                    AccountingXmlDocumentItemFixture.STAPLERS_QTY_5_COST_20_00,
                    AccountingXmlDocumentItemFixture.HEADPHONES_QTY_1_COST_50_00),
            notes(
                    "This is a sample note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE),
            CuDisbursementVoucherDocumentFixture.EMPTY),
    BASE_IB_NO_ITEMS(2, KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING,
            "Another IB Test Document", "This is a sample IB document without any item lines.", "IntBill02",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254700_OBJ_4020_AMOUNT_1000_INCOME1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263700_OBJ_1280_AMOUNT_500_INCOME2),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254710_OBJ_4020_AMOUNT_1000_EXPENSE1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263600_OBJ_1280_AMOUNT_500_EXPENSE2),
            items(),
            notes(),
            adHocRecipients(),
            backupLinks(),
            CuDisbursementVoucherDocumentFixture.EMPTY),

    SINGLE_IB_DOCUMENT_TEST_DOC1(BASE_IB_WITH_ITEMS, 1),

    MULTI_IB_DOCUMENT_TEST_DOC1(BASE_IB_WITH_ITEMS, 1),
    MULTI_IB_DOCUMENT_TEST_DOC2(BASE_IB_NO_ITEMS, 2),

    SINGLE_IB_NO_ITEMS_DOCUMENT_TEST_DOC1(BASE_IB_NO_ITEMS, 1),

    BASE_BA_WITH_ZERO_AND_SINGLE_MONTHS(1, CuFPTestConstants.BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test BA Document", "This is a BA document for testing purposes", "WXYZ5678", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_0_MONTH03_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40_NO_BASE_MONTH03_40),
            notes(
                    "Sample BA Note",
                    "Another BA Note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS(1, CuFPTestConstants.YEAR_END_BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test YEBA Document", "This is a YEBA document for testing purposes", "WXYZ5678", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_0_MONTH03_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40_NO_BASE_MONTH03_40),
            notes(
                    "Sample YEBA Note",
                    "Another YEBA Note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS_NO_ADHOC_NO_BACKUP(1, CuFPTestConstants.YEAR_END_BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test YEBA Document 2", "This is a YEBA 2 document for testing purposes", "WXYZ567C", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_0_MONTH03_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40_NO_BASE_MONTH03_40),
            notes(
                    "Sample YEBA Note for the second document"),
            adHocRecipients(),
            backupLinks()),
    BASE_BA_NO_BASEAMOUNT_OR_MONTHS(1, CuFPTestConstants.BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test BA Document", "This BA document should have base and month amounts of zero by default",
            "VVVV5555", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40),
            notes(),
            adHocRecipients(),
            backupLinks()),
    BASE_BA_NONZERO_BASEAMOUNT_AND_SINGLE_MONTHS(1, CuFPTestConstants.BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test BA Document", "This BA document should have non-zero base amounts on its accounting lines",
            "ZWZW9595", CuFPTestConstants.FY_2016,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_10_MONTH03_40,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_10_MONTH03_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_10_MONTH03_40,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40_BASE_10_MONTH03_40),
            notes(),
            adHocRecipients(),
            backupLinks()),
    BASE_BA_MULTI_MONTHS(1, CuFPTestConstants.BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test BA Document", "This BA document should have multiple month amounts", "BBBB2222", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_52_BASE_0_MONTH01_37_MONTH08_15,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_52_BASE_0_ALL_MONTHS_4_OR_5),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_52_BASE_0_MONTH01_37_MONTH08_15,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_52_BASE_0_ALL_MONTHS_4_OR_5),
            notes(),
            adHocRecipients(),
            backupLinks()),

    SINGLE_BA_DOCUMENT_TEST_DOC1(BASE_BA_WITH_ZERO_AND_SINGLE_MONTHS, 1),
    SINGLE_YEBA_DOCUMENT_TEST_DOC1(BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS, 1),
    
    MULTI_YEBA_DOCUMENT_TEST_DOC1(BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS, 1),
    MULTI_YEBA_DOCUMENT_TEST_DOC2(BASE_YEBA_WITH_ZERO_AND_SINGLE_MONTHS_NO_ADHOC_NO_BACKUP, 1),

    SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST_DOC1(BASE_BA_NO_BASEAMOUNT_OR_MONTHS, 1),

    SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST_DOC1(BASE_BA_NONZERO_BASEAMOUNT_AND_SINGLE_MONTHS, 1),

    SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST_DOC1(BASE_BA_MULTI_MONTHS, 1),

    MULTI_BA_DOCUMENT_TEST_DOC1(BASE_BA_WITH_ZERO_AND_SINGLE_MONTHS, 1),
    MULTI_BA_DOCUMENT_TEST_DOC2(BASE_BA_NONZERO_BASEAMOUNT_AND_SINGLE_MONTHS, 2),
    MULTI_BA_DOCUMENT_TEST_DOC3(BASE_BA_MULTI_MONTHS, 3),

    BASE_SB_WITH_ITEMS(1, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "First SB Test Document", "This is an example SB document with multiple items", "ServBill01",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254700_OBJ_4020_AMOUNT_100_INCOME1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263700_OBJ_1280_AMOUNT_50_INCOME2),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254710_OBJ_4020_AMOUNT_100_EXPENSE1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263600_OBJ_1280_AMOUNT_50_EXPENSE2),
            items(
                    AccountingXmlDocumentItemFixture.STAPLERS_QTY_5_COST_20_00,
                    AccountingXmlDocumentItemFixture.HEADPHONES_QTY_1_COST_50_00),
            notes(
                    "This is a sample note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE),
            CuDisbursementVoucherDocumentFixture.EMPTY),
    BASE_SB_NO_ITEMS(2, KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            "Another SB Test Document", "This is a sample SB document without any item lines.", "ServBill02",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254700_OBJ_4020_AMOUNT_1000_INCOME1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263700_OBJ_1280_AMOUNT_500_INCOME2),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G254710_OBJ_4020_AMOUNT_1000_EXPENSE1,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_G263600_OBJ_1280_AMOUNT_500_EXPENSE2),
            items(),
            notes(),
            adHocRecipients(),
            backupLinks(),
            CuDisbursementVoucherDocumentFixture.EMPTY),

    SINGLE_SB_DOCUMENT_TEST_DOC1(BASE_SB_WITH_ITEMS, 1),

    MULTI_SB_DOCUMENT_TEST_DOC1(BASE_SB_WITH_ITEMS, 1),
    MULTI_SB_DOCUMENT_TEST_DOC2(BASE_SB_NO_ITEMS, 2),

    MULTI_SB_DOCUMENT_DATE_VALUES_TEST_DOC3(BASE_SB_NO_ITEMS, 3),

    SINGLE_SB_NO_ITEMS_DOCUMENT_TEST_DOC1(BASE_SB_NO_ITEMS, 1),

    BASE_AV_ADJUSTMENT(
            1, CuFPTestConstants.AUXILIARY_VOUCHER_DOC_TYPE,
            "Test AV Document", "This is an AV document for testing purposes", "WXYZ5678",
            AccountingPeriodFixture.FEB_2019, KFSConstants.AuxiliaryVoucher.ADJUSTMENT_DOC_TYPE, null,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_DEBIT_55,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_CREDIT_55),
            notes(
                    "Sample AV Note",
                    "Another AV Note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    BASE_AV_ACCRUAL_WITH_DEFAULT_REVERSAL(
            2, CuFPTestConstants.AUXILIARY_VOUCHER_DOC_TYPE,
            "Test AV Document 2", "This is another test AV document", "WXYZ5679",
            AccountingPeriodFixture.FEB_2019, KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE, null,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_DEBIT_55,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_CREDIT_55),
            notes(
                    "Sample AV Note 2",
                    "Another AV Note 2"),
            adHocRecipients(),
            backupLinks()),
    BASE_AV_ACCRUAL_WITH_SPECIFIC_REVERSAL(
            3, CuFPTestConstants.AUXILIARY_VOUCHER_DOC_TYPE,
            "Test AV Document 3", "This is yet another test AV document", "WXYZ5680",
            AccountingPeriodFixture.FEB_2019, KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE, "03/31/2019",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_DEBIT_55,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_CREDIT_55),
            notes(
                    "Sample AV Note 3",
                    "Another AV Note 3"),
            adHocRecipients(),
            backupLinks()),

    SINGLE_AV_DOCUMENT_TEST_DOC1(BASE_AV_ADJUSTMENT, 1),

    MULTI_AV_DOCUMENT_TEST_DOC1(BASE_AV_ADJUSTMENT, 1),
    MULTI_AV_DOCUMENT_TEST_DOC2(BASE_AV_ACCRUAL_WITH_DEFAULT_REVERSAL, 2),
    MULTI_AV_DOCUMENT_TEST_DOC3(BASE_AV_ACCRUAL_WITH_SPECIFIC_REVERSAL, 3),

    MULTI_DOC_TYPE_TEST_DI(MULTI_DI_DOCUMENT_TEST_DOC1, 1),
    MULTI_DOC_TYPE_TEST_IB(BASE_IB_WITH_ITEMS, 2),
    MULTI_DOC_TYPE_TEST_TF1(3, KFSConstants.TRANSFER_FUNDS,
            "Test TF Document", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_100_FROM),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S343717_OBJ_7070_AMT_100_TO),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    MULTI_DOC_TYPE_TEST_TF2(4, KFSConstants.TRANSFER_FUNDS,
            "Test TF Document2", "This is only a test document2!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_1000_FROM),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S343717_OBJ_7070_AMT_1000_TO),
            notes(),
            adHocRecipients(),
            backupLinks()),
    MULTI_DOC_TYPE_TEST_BA(BASE_BA_WITH_ZERO_AND_SINGLE_MONTHS, 5),
    MULTI_DOC_TYPE_TEST_SB(BASE_SB_WITH_ITEMS, 6),
    MULTI_DOC_TYPE_TEST_YEDI(BASE_DOCUMENT, 7, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_50),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE)),
    MULTI_DOC_TYPE_TEST_DV(8, CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            "Test DV Document", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(),
            items(),
            notes(
                    "This is a sample note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE),
            CuDisbursementVoucherDocumentFixture.JANE_DOE_DV_DETAIL),
    MULTI_DOC_TYPE_TEST_YEBA(9, CuFPTestConstants.YEAR_END_BUDGET_ADJUSTMENT_DOC_TYPE,
            "Test YEBA Document in multi doc file", "This is a YEBA doc for testing", "WXYZ567D", CuFPTestConstants.FY_2018,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_0_MONTH03_40),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_C200222_OBJ_5390_AMOUNT_40_NO_BASE_MONTH03_40),
            notes(
                    "Sample YEBA Note for the multi doc testing file"),
            adHocRecipients(),
            backupLinks()),
    MULTI_DOC_TYPE_TEST_YETF(10, CuFPTestConstants.YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE,
            "Test YETF Document", "This is only a test YETF document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_100_FROM),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S343717_OBJ_7070_AMT_100_TO),
            notes(
                    "A fun testing note for YETF",
                    "Another note for YETF"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    MULTI_DOC_TYPE_TEST_AV(BASE_AV_ADJUSTMENT, 11),

    SINGLE_DOC_TYPE_TEST_YETF(1, CuFPTestConstants.YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE,
            "Test YETF Document", "This is only a test YETF document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_100_FROM),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S343717_OBJ_7070_AMT_100_TO),
            notes(
                    "A fun testing note for YETF",
                    "Another note for YETF"),
            adHocRecipients(),
            backupLinks()),
    DI_WITH_IB_ITEMS_TEST_DOC1(MULTI_DI_DOCUMENT_TEST_DOC1, 1),
    MULTI_YEDI_DOCUMENT_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_50),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE)),
    MULTI_YEDI_DOCUMENT_TEST_DOC2(
            2, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "Test Document 2", "This is another test document.", "GGGG4444",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(
                    "A fun testing note on the second YEDI",
                    "Another note on the second YEDI"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks()),
    MULTI_YEDI_DOCUMENT_TEST_DOC3(
            3, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "Test Document 3", "A third document for testing!!!", "ZYXW9876",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(),
            adHocRecipients(),
            backupLinks()),
    MULTI_YEDI_DOCUMENT_TEST_DOC4(
            4, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            "auth backup Document", "Document Explanation", "OrgDoc4",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(),
            adHocRecipients(),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.AWS_BILLING_INVOICE,
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    
    DV_DOC_TEST1(1, CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            "Test DV Document", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(),
            items(),
            notes(
                    "This is a sample note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE),
            CuDisbursementVoucherDocumentFixture.JANE_DOE_DV_DETAIL),
    DV_DOC_TEST2(2, CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            "Test DV Document - No Due Date", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(),
            items(),
            notes(
                    "This is a sample note"),
            adHocRecipients(),
            backupLinks(),
            CuDisbursementVoucherDocumentFixture.JOHN_DOE_DV_DETAIL),
    DV_DOC_VENDOR_TEST1(1, CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            "Test Vendor", "This is only a test document!", "ABYZ1290",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(),
            items(),
            notes(
                    "This is a sample note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE),
            CuDisbursementVoucherDocumentFixture.XYZ_INDUSTRIES_DV_DETAIL),
    DV_DOC_VENDOR_TEST2(2, CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            "Test Vendor Person", "This is another test document!", "ABYZ1291",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(),
            items(),
            notes("This is a sample note"),
            adHocRecipients(),
            backupLinks(),
            CuDisbursementVoucherDocumentFixture.REE_PHUND_DV_DETAIL),
    SINGLE_DOC_TYPE_TEST_TF1(1, KFSConstants.TRANSFER_FUNDS,
            "Test TF Document", "This is only a test document!", "ABCD1234",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_100_FROM),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_S343717_OBJ_7070_AMT_100_TO),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE)),
    SINGLE_YEDI_DOCUMENT_TEST_DOC1(
            BASE_DOCUMENT, 1, KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_50),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_100,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_50),
            notes(
                    "A fun testing note",
                    "Another note"),
            adHocRecipients(
                    AccountingXmlDocumentAdHocRecipientFixture.JDH34_APPROVE,
                    AccountingXmlDocumentAdHocRecipientFixture.SE12_FYI,
                    AccountingXmlDocumentAdHocRecipientFixture.CCS1_COMPLETE,
                    AccountingXmlDocumentAdHocRecipientFixture.NKK4_ACKNOWLEDGE),
            backupLinks(
                    AccountingXmlDocumentBackupLinkFixture.CORNELL_INDEX_PAGE,
                    AccountingXmlDocumentBackupLinkFixture.DFA_INDEX_PAGE));

    public final Long index;
    public final String documentTypeCode;
    public final String description;
    public final String explanation;
    public final String organizationDocumentNumber;
    public final AccountingPeriodFixture accountingPeriod;
    public final String auxiliaryVoucherType;
    public final String reversalDate;
    public final Integer postingFiscalYear;
    public final List<AccountingXmlDocumentAccountingLineFixture> sourceAccountingLines;
    public final List<AccountingXmlDocumentAccountingLineFixture> targetAccountingLines;
    public final List<AccountingXmlDocumentItemFixture> items;
    public final CuDisbursementVoucherDocumentFixture dvDetails;
    public final List<String> notes;
    public final List<AccountingXmlDocumentAdHocRecipientFixture> adHocRecipients;
    public final List<AccountingXmlDocumentBackupLinkFixture> backupLinks;

    private AccountingXmlDocumentEntryFixture(AccountingXmlDocumentEntryFixture baseFixture, long index,
            String documentTypeCode, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this(index, documentTypeCode, baseFixture.description, baseFixture.explanation, baseFixture.organizationDocumentNumber,
                sourceAccountingLines, targetAccountingLines, notes, adHocRecipients, backupLinks);
    }
    
    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingPeriodFixture accountingPeriod, String auxiliaryVoucherType, String reversalDate,
            AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this(index, documentTypeCode, description, explanation, organizationDocumentNumber, accountingPeriod, auxiliaryVoucherType, reversalDate, 0,
                sourceAccountingLines, targetAccountingLines(), items(), notes, adHocRecipients, backupLinks, CuDisbursementVoucherDocumentFixture.EMPTY);
    }
    
    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this(index, documentTypeCode, description, explanation, organizationDocumentNumber,
                sourceAccountingLines, targetAccountingLines, items(), notes, adHocRecipients, backupLinks, CuDisbursementVoucherDocumentFixture.EMPTY);
    }

    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, AccountingXmlDocumentItemFixture[] items, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks,
            CuDisbursementVoucherDocumentFixture dvDetails) {
        this(index, documentTypeCode, description, explanation, organizationDocumentNumber, null, null, null, 0,
                sourceAccountingLines, targetAccountingLines, items, notes, adHocRecipients, backupLinks, dvDetails);
    }

    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber,
            int postingFiscalYear, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this(index, documentTypeCode, description, explanation, organizationDocumentNumber, null, null, null, postingFiscalYear,
                sourceAccountingLines, targetAccountingLines, items(), notes, adHocRecipients, backupLinks, CuDisbursementVoucherDocumentFixture.EMPTY);
    }

    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingPeriodFixture accountingPeriod, String auxiliaryVoucherType, String reversalDate,
            int postingFiscalYear, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, AccountingXmlDocumentItemFixture[] items, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks,
            CuDisbursementVoucherDocumentFixture dvDetails) {
        this.index = Long.valueOf(index);
        this.documentTypeCode = documentTypeCode;
        this.description = description;
        this.explanation = defaultToEmptyStringIfBlank(explanation);
        this.organizationDocumentNumber = defaultToEmptyStringIfBlank(organizationDocumentNumber);
        this.accountingPeriod = accountingPeriod;
        this.auxiliaryVoucherType = defaultToEmptyStringIfBlank(auxiliaryVoucherType);
        this.reversalDate = reversalDate;
        this.postingFiscalYear = (postingFiscalYear != 0) ? Integer.valueOf(postingFiscalYear) : null;
        this.sourceAccountingLines = XmlDocumentFixtureUtils.toImmutableList(sourceAccountingLines);
        this.targetAccountingLines = XmlDocumentFixtureUtils.toImmutableList(targetAccountingLines);
        this.items = XmlDocumentFixtureUtils.toImmutableList(items);
        this.dvDetails = dvDetails;
        this.notes = XmlDocumentFixtureUtils.toImmutableList(notes);
        this.adHocRecipients = XmlDocumentFixtureUtils.toImmutableList(adHocRecipients);
        this.backupLinks = XmlDocumentFixtureUtils.toImmutableList(backupLinks);
    }

    private AccountingXmlDocumentEntryFixture(AccountingXmlDocumentEntryFixture baseFixture, long index) {
        this.index = Long.valueOf(index);
        this.postingFiscalYear = baseFixture.postingFiscalYear;
        this.documentTypeCode = baseFixture.documentTypeCode;
        this.description = baseFixture.description;
        this.explanation = baseFixture.explanation;
        this.organizationDocumentNumber = baseFixture.organizationDocumentNumber;
        this.accountingPeriod = baseFixture.accountingPeriod;
        this.auxiliaryVoucherType = baseFixture.auxiliaryVoucherType;
        this.reversalDate = baseFixture.reversalDate;
        this.sourceAccountingLines = baseFixture.sourceAccountingLines;
        this.targetAccountingLines = baseFixture.targetAccountingLines;
        this.items = baseFixture.items;
        this.notes = baseFixture.notes;
        this.dvDetails = baseFixture.dvDetails;
        this.adHocRecipients = baseFixture.adHocRecipients;
        this.backupLinks = baseFixture.backupLinks;
    }

    public AccountingXmlDocumentEntry toDocumentEntryPojo() {
        AccountingXmlDocumentEntry documentEntry = new AccountingXmlDocumentEntry();
        documentEntry.setIndex(index);
        documentEntry.setDocumentTypeCode(documentTypeCode);
        documentEntry.setDescription(description);
        documentEntry.setExplanation(explanation);
        documentEntry.setOrganizationDocumentNumber(organizationDocumentNumber);
        if (accountingPeriod != null) {
            documentEntry.setAccountingPeriod(accountingPeriod.getUniversityFiscalPeriodName());
        }
        documentEntry.setAuxiliaryVoucherType(auxiliaryVoucherType);
        if (StringUtils.isNotBlank(reversalDate)) {
            documentEntry.setReversalDate(getParsedReversalDate(java.util.Date::new));
        }
        
        documentEntry.setPostingFiscalYear(postingFiscalYear);
        documentEntry.setSourceAccountingLines(
                XmlDocumentFixtureUtils.convertToPojoList(sourceAccountingLines, AccountingXmlDocumentAccountingLineFixture::toAccountingLinePojo));
        documentEntry.setTargetAccountingLines(
                XmlDocumentFixtureUtils.convertToPojoList(targetAccountingLines, AccountingXmlDocumentAccountingLineFixture::toAccountingLinePojo));
        documentEntry.setItems(
                XmlDocumentFixtureUtils.convertToPojoList(items, AccountingXmlDocumentItemFixture::toItemPojo));
        documentEntry.setNotes(
                XmlDocumentFixtureUtils.convertToPojoList(notes, AccountingXmlDocumentNote::new));
        documentEntry.setAdHocRecipients(
                XmlDocumentFixtureUtils.convertToPojoList(adHocRecipients, AccountingXmlDocumentAdHocRecipientFixture::toAdHocRecipientPojo));
        documentEntry.setBackupLinks(
                XmlDocumentFixtureUtils.convertToPojoList(backupLinks, AccountingXmlDocumentBackupLinkFixture::toBackupLinkPojo));
        return documentEntry;
    }

    public AccountingDocument toAccountingDocument(String documentNumber) {
        Class<? extends AccountingDocument> accountingDocumentClass = AccountingDocumentClassMappingUtils
                .getDocumentClassByDocumentType(documentTypeCode);
        
        AccountingDocument accountingDocument = MockDocumentUtils.buildMockAccountingDocument(accountingDocumentClass);
        
        populateNumberAndHeaderOnDocument(accountingDocument, documentNumber);
        addAccountingLinesToDocument(accountingDocument);
        addItemsToDocumentIfNecessary(accountingDocument);
        addDvDetailsToDocumentIfNecessary(accountingDocument);
        addAuxiliaryVoucherSettingsToDocumentIfNecessary(accountingDocument);
        addNotesToDocument(accountingDocument);
        addAdHocRecipientsToDocument(accountingDocument);
        
        if (postingFiscalYear != null) {
            accountingDocument.setPostingYear(postingFiscalYear);
        }
        
        return accountingDocument;
    }

    private void populateNumberAndHeaderOnDocument(AccountingDocument accountingDocument, String documentNumber) {
        DocumentHeader documentHeader = (DocumentHeader) accountingDocument.getDocumentHeader();
        accountingDocument.setDocumentNumber(documentNumber);
        documentHeader.setDocumentNumber(documentNumber);
        documentHeader.setDocumentDescription(description);
        documentHeader.setExplanation(explanation);
        documentHeader.setOrganizationDocumentNumber(organizationDocumentNumber);
    }

    @SuppressWarnings("unchecked")
    private void addAccountingLinesToDocument(AccountingDocument accountingDocument) {
        Class<? extends SourceAccountingLine> sourceAccountingLineClass = accountingDocument.getSourceAccountingLineClass();
        Class<? extends TargetAccountingLine> targetAccountingLineClass = accountingDocument.getTargetAccountingLineClass();
        sourceAccountingLines.stream()
                .map((fixture) -> fixture.toAccountingLineBo(sourceAccountingLineClass, accountingDocument.getDocumentNumber()))
                .forEach(accountingDocument::addSourceAccountingLine);
        targetAccountingLines.stream()
                .map((fixture) -> fixture.toAccountingLineBo(targetAccountingLineClass, accountingDocument.getDocumentNumber()))
                .forEach(accountingDocument::addTargetAccountingLine);
    }

    private void addNotesToDocument(AccountingDocument accountingDocument) {
        notes.stream()
                .map(MockDocumentUtils::buildMockNote)
                .forEach(accountingDocument::addNote);
        backupLinks.stream()
            .map(AccountingXmlDocumentBackupLinkFixture::toNoteWithAttachment)
            .forEach(accountingDocument::addNote);
    }

    private void addAdHocRecipientsToDocument(AccountingDocument accountingDocument) {
        List<AdHocRoutePerson> adHocPersons = accountingDocument.getAdHocRoutePersons();
        adHocRecipients.stream()
                .map((fixture) -> fixture.toAdHocRoutePerson(accountingDocument.getDocumentNumber()))
                .forEach(adHocPersons::add);
    }

    private void addItemsToDocumentIfNecessary(AccountingDocument accountingDocument) {
        if (accountingDocument instanceof InternalBillingDocument) {
            InternalBillingDocument internalBillingDocument = (InternalBillingDocument) accountingDocument;
            String documentNumber = internalBillingDocument.getDocumentNumber();
            items.stream()
                    .map((fixture) -> fixture.toInternalBillingItem(documentNumber))
                    .forEach(internalBillingDocument::addItem);
        }
    }
    
    private void addDvDetailsToDocumentIfNecessary(AccountingDocument accountingDocument) {
        if (accountingDocument instanceof CuDisbursementVoucherDocument) {
            CuDisbursementVoucherDocument dvDoc = (CuDisbursementVoucherDocument) accountingDocument;
            dvDoc.setDisbVchrBankCode(dvDetails.bankCode);
            dvDoc.setDisbVchrContactPersonName(dvDetails.contactName);
            dvDoc.getDvPayeeDetail().setDisbVchrPaymentReasonCode(dvDetails.paymentReasonCode);
            dvDoc.getDvPayeeDetail().setDisbursementVoucherPayeeTypeCode(dvDetails.getMappedPayeeTypeCode());
            dvDoc.getDvNonEmployeeTravel().setDisbVchrPerdiemRate(dvDetails.perdiemRate);
            dvDoc.getDvNonEmployeeTravel().setDisbVchrNonEmpTravelerName(dvDetails.nonEmployeeTravelerName);
            dvDoc.getDvNonEmployeeTravel().setDvPersonalCarMileageAmount(dvDetails.nonEmployeeCarMileage);
            dvDoc.getDvPreConferenceDetail().setDvConferenceDestinationName(dvDetails.conferenceDestination);
            dvDoc.setDisbursementVoucherDueDate(new Date(dvDetails.dueDate.getMillis()));
            if (dvDetails.invoiceDate != null) {
                dvDoc.setInvoiceDate(new Date(dvDetails.invoiceDate.getMillis()));
            }
            dvDoc.setInvoiceNumber(dvDetails.invoiceNumber);
        }
    }

    private void addAuxiliaryVoucherSettingsToDocumentIfNecessary(AccountingDocument accountingDocument) {
        if (accountingDocument instanceof AuxiliaryVoucherDocument) {
            AuxiliaryVoucherDocument auxiliaryVoucherDocument = (AuxiliaryVoucherDocument) accountingDocument;
            auxiliaryVoucherDocument.setTypeCode(auxiliaryVoucherType);
            auxiliaryVoucherDocument.setAccountingPeriod(accountingPeriod.toAccountingPeriod());
            if (StringUtils.isNotBlank(reversalDate)) {
                auxiliaryVoucherDocument.setReversalDate(getParsedReversalDate(java.sql.Date::new));
            } else if (StringUtils.equals(KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_TYPE, auxiliaryVoucherType)) {
                auxiliaryVoucherDocument.setReversalDate(accountingPeriod.getReversalSqlDate());
            }
        }
    }

    private <T extends java.util.Date> T getParsedReversalDate(LongFunction<T> dateConstructor) {
        DateTime parsedReversalDate = StringToJavaDateAdapter.parseToDateTime(reversalDate);
        return dateConstructor.apply(parsedReversalDate.getMillis());
    }

    // The following methods are only meant to improve the setup and readability of this enum's constants.

    private static AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines(AccountingXmlDocumentAccountingLineFixture... fixtures) {
        return fixtures;
    }

    private static AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines(AccountingXmlDocumentAccountingLineFixture... fixtures) {
        return fixtures;
    }

    private static AccountingXmlDocumentItemFixture[] items(AccountingXmlDocumentItemFixture... items) {
        return items;
    }

    private static String[] notes(String... values) {
        return values;
    }

    private static AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients(AccountingXmlDocumentAdHocRecipientFixture... fixtures) {
        return fixtures;
    }

    private static AccountingXmlDocumentBackupLinkFixture[] backupLinks(AccountingXmlDocumentBackupLinkFixture... fixtures) {
        return fixtures;
    }
    
    private DisbursementVoucherNonEmployeeExpense getTestAbleDisbursementVoucherNonEmployeeExpense() {
        DisbursementVoucherNonEmployeeExpense expense = Mockito.spy(new DisbursementVoucherNonEmployeeExpense());
        Mockito.doNothing().when(expense).refreshNonUpdateableReferences();
        return expense;
    }

}
