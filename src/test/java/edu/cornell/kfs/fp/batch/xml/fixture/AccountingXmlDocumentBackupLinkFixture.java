package edu.cornell.kfs.fp.batch.xml.fixture;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;

public enum AccountingXmlDocumentBackupLinkFixture {
    CORNELL_INDEX_PAGE("http://www.cornell.edu/index.html", "Cornell index page", "index.html"),
    DFA_INDEX_PAGE("https://www.dfa.cornell.edu/index.cfm", "DFA index page", "index.cfm"),
    AWS_BILLING_INVOICE("https://kfsaws-support.cd.cucloud.net/v1_0/aws/billing/invoice?year=2017&month=01&account=999999999999", "AWS Billing Invoice", 
            "invoice.pdf", "AWS-Bill");

    public final String linkUrl;
    public final String description;
    public final String fileName;
    public final String groupCode;

    private AccountingXmlDocumentBackupLinkFixture(String linkUrl, String description, String fileName) {
        this(linkUrl, description, fileName, StringUtils.EMPTY);
    }
    
    private AccountingXmlDocumentBackupLinkFixture(String linkUrl, String description, String fileName, String groupCode) {
        this.linkUrl = linkUrl;
        this.description = description;
        this.fileName = fileName;
        this.groupCode = groupCode;
    }

    public AccountingXmlDocumentBackupLink toBackupLinkPojo() {
        AccountingXmlDocumentBackupLink backupLink = new AccountingXmlDocumentBackupLink();
        backupLink.setLinkUrl(linkUrl);
        backupLink.setDescription(description);
        backupLink.setFileName(fileName);
        backupLink.setCredentialGroupCode(groupCode);
        return backupLink;
    }

}
