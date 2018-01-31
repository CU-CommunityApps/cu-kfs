package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;
import edu.cornell.kfs.sys.util.MockDocumentUtils;

@SuppressWarnings("deprecation")
public enum AccountingXmlDocumentEntryFixture {
    BASE_DOCUMENT(1, KFSConstants.ROOT_DOCUMENT_TYPE, "Test Document", "This is only a test document!", "ABCD1234",
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
            3, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
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

    MULTI_DI_DOCUMENT_TEST_DOC1_BAD(
            1, KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            CuFPTestConstants.BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR, "This document should not route!", "ABCD1234",
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

    private AccountingXmlDocumentEntryFixture(AccountingXmlDocumentEntryFixture baseFixture, long index,
            String documentTypeCode, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, String[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this(index, documentTypeCode, baseFixture.description, baseFixture.explanation, baseFixture.organizationDocumentNumber,
                sourceAccountingLines, targetAccountingLines, notes, adHocRecipients, backupLinks);
    }

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

    public AccountingDocument toAccountingDocument(String documentNumber) {
        Class<? extends AccountingDocument> accountingDocumentClass = AccountingDocumentClassMappingUtils
                .getDocumentClassByDocumentType(documentTypeCode);
        
        AccountingDocument accountingDocument = MockDocumentUtils.buildMockAccountingDocument(accountingDocumentClass);
        
        populateNumberAndHeaderOnDocument(accountingDocument, documentNumber);
        addAccountingLinesToDocument(accountingDocument);
        addNotesToDocument(accountingDocument);
        addAdHocRecipientsToDocument(accountingDocument);
        
        return accountingDocument;
    }

    private void populateNumberAndHeaderOnDocument(AccountingDocument accountingDocument, String documentNumber) {
        FinancialSystemDocumentHeader documentHeader = (FinancialSystemDocumentHeader) accountingDocument.getDocumentHeader();
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
        
        accountingDocument.setSourceAccountingLines(new ArrayList<>());
        accountingDocument.setTargetAccountingLines(new ArrayList<>());
        accountingDocument.setNextSourceLineNumber(Integer.valueOf(1));
        accountingDocument.setNextTargetLineNumber(Integer.valueOf(1));
        
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
            .map(s -> s.description)
            .map(MockDocumentUtils::buildMockNote)
            .forEach(accountingDocument::addNote);
    }

    private void addAdHocRecipientsToDocument(AccountingDocument accountingDocument) {
        List<AdHocRoutePerson> adHocPersons = accountingDocument.getAdHocRoutePersons();
        adHocRecipients.stream()
                .map((fixture) -> fixture.toAdHocRoutePerson(accountingDocument.getDocumentNumber()))
                .forEach(adHocPersons::add);
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
