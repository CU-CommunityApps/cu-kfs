package edu.cornell.kfs.coa.fixture;

public @interface AccountAttachmentListingFixture {

    String chartOfAccountsCode();
    String accountNumber();
    String accountName();
    AccountAttachmentNoteFixture[] notes();

}
