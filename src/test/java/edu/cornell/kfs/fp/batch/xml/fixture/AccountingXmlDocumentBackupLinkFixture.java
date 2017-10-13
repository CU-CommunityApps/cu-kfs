package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;

public enum AccountingXmlDocumentBackupLinkFixture {
    TEST_LINK1(null, null);

    public final String linkUrl;
    public final String description;

    private AccountingXmlDocumentBackupLinkFixture(String linkUrl, String description) {
        this.linkUrl = linkUrl;
        this.description = description;
    }

    public AccountingXmlDocumentBackupLink toBackupLinkPojo() {
        AccountingXmlDocumentBackupLink backupLink = new AccountingXmlDocumentBackupLink();
        backupLink.setLinkUrl(linkUrl);
        backupLink.setDescription(description);
        return backupLink;
    }

}
