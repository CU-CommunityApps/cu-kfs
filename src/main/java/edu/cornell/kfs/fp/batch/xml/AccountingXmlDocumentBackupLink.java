package edu.cornell.kfs.fp.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "BackupLink", namespace = StringUtils.EMPTY)
public class AccountingXmlDocumentBackupLink {

    @XmlElement(name = "Link", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String linkUrl;

    @XmlElement(name = "Description", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String description;
    
    @XmlElement(name = "FileName", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String fileName;
    
    @XmlElement(name = "CredentialGroupCode", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String credentialGroupCode;

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCredentialGroupCode() {
        return credentialGroupCode;
    }

    public void setCredentialGroupCode(String credentialGroupCode) {
        this.credentialGroupCode = credentialGroupCode;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccountingXmlDocumentBackupLink, linkUrl: ").append(linkUrl);
        sb.append(" description: ").append(description);
        sb.append(" fileName: ").append(fileName);
        sb.append(" credentialGroupCode: ").append(credentialGroupCode);
        return sb.toString();
    }

}
