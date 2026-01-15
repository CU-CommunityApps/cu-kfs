package edu.cornell.kfs.coa.rest.resource.fixture;

public enum AccountAttachmentControllerTestData {
    @AccountAttachmentListingFixture(
        chartOfAccountsCode = "IT",
        accountNumber = "G123456",
        accountName = "Account without notes",
        notes = {}
    )
    ACCOUNT_WITHOUT_NOTES,

    @AccountAttachmentListingFixture(
        chartOfAccountsCode = "MC",
        accountNumber = "7575757",
        accountName = "Account with non-attachment notes",
        notes = {
            @AccountAttachmentNoteFixture(
                noteId = 1001L,
                noteText = "Submitted by user abc123",
                hasAttachment = false
            ),
            @AccountAttachmentNoteFixture(
                noteId = 1002L,
                noteText = "Approved by user zyx987",
                hasAttachment = false
            )
        }
    )
    ACCOUNT_WITH_NON_ATTACHMENT_NOTES,

    @AccountAttachmentListingFixture(
        chartOfAccountsCode = "IT",
        accountNumber = "J444444",
        accountName = "Account with single attachment",
        notes = {
            @AccountAttachmentNoteFixture(
                noteId = 2400L,
                noteText = "Documentation",
                attachmentId = "12345678-abcd-5555-gggg-a1b2c3d4e5f6",
                mimeType = "application/pdf",
                fileName = "acct-documentation.pdf"
            ),
        }
    )
    ACCOUNT_WITH_SINGLE_ATTACHMENT,

    @AccountAttachmentListingFixture(
        chartOfAccountsCode = "EO",
        accountNumber = "EIEI000",
        accountName = "Account with multiple attachments",
        notes = {
            @AccountAttachmentNoteFixture(
                noteId = 3011L,
                noteText = "Account Info",
                attachmentId = "11111111-aaaa-2222-bbbb-3c3c3c3c3c3c",
                mimeType = "application/pdf",
                fileName = "acct-documentation.pdf"
            ),
            @AccountAttachmentNoteFixture(
                noteId = 3011L,
                noteText = "Other Info",
                attachmentId = "11111111-zzzz-2222-yyyy-3c3c3c3c3c3c",
                mimeType = "text/plain",
                fileName = "other-info.txt"
            )
        }
    )
    ACCOUNT_WITH_MULTIPLE_ATTACHMENTS,

    @AccountAttachmentListingFixture(
        chartOfAccountsCode = "IT",
        accountNumber = "5656565",
        accountName = "Account with mixed notes",
        notes = {
            @AccountAttachmentNoteFixture(
                noteId = 7885L,
                noteText = "Miscellaneous information",
                attachmentId = "98989898-7777-6666-5555-432143214321",
                mimeType = "text/plain",
                fileName = "other-info.txt"
            ),
            @AccountAttachmentNoteFixture(
                noteId = 7886L,
                noteText = "Submitted by user abc444",
                hasAttachment = false
            ),
            @AccountAttachmentNoteFixture(
                noteId = 7887L,
                noteText = "Data for account",
                attachmentId = "pqrspqrs-7777-6666-5555-yzyzyzyzyzyz",
                mimeType = "application/pdf",
                fileName = "acct-documentation.pdf"
            )
        }
    )
    ACCOUNT_WITH_MIXED_NOTES;

}
