package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import java.util.List;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;

public enum AccountingXmlDocumentEntryFixture {
    MULTI_DI_DOCUMENT_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
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
    MULTI_DI_DOCUMENT_TEST_DOC2(2, "DI", "Document Description 2", "Document Explanation 2", "Org doc number 2",
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
    MULTI_DI_DOCUMENT_TEST_DOC3(3, "DI", "Document Description 3", "Document Explanation 3", "Org doc number 3",
            sourceAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504700_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000718_OBJ_4000_AMOUNT_500),
            targetAccountingLines(
                    AccountingXmlDocumentAccountingLineFixture.ACCT_R504706_OBJ_2640_AMOUNT_1000,
                    AccountingXmlDocumentAccountingLineFixture.ACCT_1000710_OBJ_4000_AMOUNT_500),
            notes(),
            adHocRecipients(),
            backupLinks()),

    SINGLE_DI_DOCUMENT_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
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

    DI_FULL_ACCOUNT_LINE_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
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

    DI_SINGLE_ELEMENT_LISTS_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
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

    DI_EMPTY_ELEMENT_LISTS_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
            sourceAccountingLines(),
            targetAccountingLines(),
            notes(),
            adHocRecipients(),
            backupLinks()),

    DI_WITHOUT_ELEMENT_LISTS_TEST_DOC1(1, "DI", "Document Description", "Document Explanation", "Org doc number",
            sourceAccountingLines(),
            targetAccountingLines(),
            notes(),
            adHocRecipients(),
            backupLinks());

    public final Long index;
    public final String documentTypeCode;
    public final String description;
    public final String explanation;
    public final String organizationDocumentNumber;
    public final List<AccountingXmlDocumentAccountingLineFixture> sourceAccountingLines;
    public final List<AccountingXmlDocumentAccountingLineFixture> targetAccountingLines;
    public final List<String> notes;
    public final List<AccountingXmlDocumentAdHocRecipientFixture> adHocRecipients;
    public final List<AccountingXmlDocumentBackupLinkFixture> backupLinks;

    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this.index = Long.valueOf(index);
        this.documentTypeCode = documentTypeCode;
        this.description = description;
        this.explanation = defaultToEmptyStringIfBlank(explanation);
        this.organizationDocumentNumber = defaultToEmptyStringIfBlank(organizationDocumentNumber);
        this.sourceAccountingLines = AccountingXmlDocumentFixtureUtils.toImmutableList(sourceAccountingLines);
        this.targetAccountingLines = AccountingXmlDocumentFixtureUtils.toImmutableList(targetAccountingLines);
        this.notes = AccountingXmlDocumentFixtureUtils.toImmutableList(notes);
        this.adHocRecipients = AccountingXmlDocumentFixtureUtils.toImmutableList(adHocRecipients);
        this.backupLinks = AccountingXmlDocumentFixtureUtils.toImmutableList(backupLinks);
    }

    public AccountingXmlDocumentEntry toDocumentEntryPojo() {
        AccountingXmlDocumentEntry documentEntry = new AccountingXmlDocumentEntry();
        documentEntry.setIndex(index);
        documentEntry.setDocumentTypeCode(documentTypeCode);
        documentEntry.setDescription(description);
        documentEntry.setExplanation(explanation);
        documentEntry.setOrganizationDocumentNumber(organizationDocumentNumber);
        documentEntry.setSourceAccountingLines(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(sourceAccountingLines, AccountingXmlDocumentAccountingLineFixture::toAccountingLinePojo));
        documentEntry.setTargetAccountingLines(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(targetAccountingLines, AccountingXmlDocumentAccountingLineFixture::toAccountingLinePojo));
        documentEntry.setNotes(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(notes, AccountingXmlDocumentNote::new));
        documentEntry.setAdHocRecipients(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(adHocRecipients, AccountingXmlDocumentAdHocRecipientFixture::toAdHocRecipientPojo));
        documentEntry.setBackupLinks(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(backupLinks, AccountingXmlDocumentBackupLinkFixture::toBackupLinkPojo));
        return documentEntry;
    }

    // The following methods are only meant to improve the setup and readability of this enum's constants.

    private static AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines(AccountingXmlDocumentAccountingLineFixture... fixtures) {
        return fixtures;
    }

    private static AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines(AccountingXmlDocumentAccountingLineFixture... fixtures) {
        return fixtures;
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

}
