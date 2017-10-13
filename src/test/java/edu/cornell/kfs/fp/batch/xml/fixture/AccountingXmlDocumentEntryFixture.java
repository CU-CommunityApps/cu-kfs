package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.List;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;

public enum AccountingXmlDocumentEntryFixture {
    TEST_DOCUMENT1(1, null, null, null, null, null, null, null, null, null);

    public final Long index;
    public final String documentTypeCode;
    public final String description;
    public final String explanation;
    public final String organizationDocumentNumber;
    public final List<AccountingXmlDocumentAccountingLineFixture> sourceAccountingLines;
    public final List<AccountingXmlDocumentAccountingLineFixture> targetAccountingLines;
    public final List<AccountingXmlDocumentNoteFixture> notes;
    public final List<AccountingXmlDocumentAdHocRecipientFixture> adHocRecipients;
    public final List<AccountingXmlDocumentBackupLinkFixture> backupLinks;

    private AccountingXmlDocumentEntryFixture(long index, String documentTypeCode, String description,
            String explanation, String organizationDocumentNumber, AccountingXmlDocumentAccountingLineFixture[] sourceAccountingLines,
            AccountingXmlDocumentAccountingLineFixture[] targetAccountingLines, AccountingXmlDocumentNoteFixture[] notes,
            AccountingXmlDocumentAdHocRecipientFixture[] adHocRecipients, AccountingXmlDocumentBackupLinkFixture[] backupLinks) {
        this.index = Long.valueOf(index);
        this.documentTypeCode = documentTypeCode;
        this.description = description;
        this.explanation = explanation;
        this.organizationDocumentNumber = organizationDocumentNumber;
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
                AccountingXmlDocumentFixtureUtils.convertToPojoList(notes, AccountingXmlDocumentNoteFixture::toDocumentNotePojo));
        documentEntry.setAdHocRecipients(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(adHocRecipients, AccountingXmlDocumentAdHocRecipientFixture::toAdHocRecipientPojo));
        documentEntry.setBackupLinks(
                AccountingXmlDocumentFixtureUtils.convertToPojoList(backupLinks, AccountingXmlDocumentBackupLinkFixture::toBackupLinkPojo));
        return documentEntry;
    }

}
