package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;

public enum AccountingXmlDocumentBackupLinkFixture {
    CORNELL_INDEX_PAGE("http://www.cornell.edu/index.html", "Cornell index page"),
    DFA_INDEX_PAGE("https://www.dfa.cornell.edu/index.cfm", "DFA index page");

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
