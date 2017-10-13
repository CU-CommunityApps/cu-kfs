package edu.cornell.kfs.fp.batch.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = StringUtils.EMPTY, propOrder = {
    "index",
    "documentTypeCode",
    "description",
    "explanation",
    "organizationDocumentNumber",
    "sourceAccountingLines",
    "targetAccountingLines",
    "notes",
    "adHocRecipients",
    "backupLinks"
})
@XmlRootElement(name = "Document", namespace = StringUtils.EMPTY)
public class AccountingXmlDocumentEntry {

    @XmlElement(name = "Index", namespace = StringUtils.EMPTY, required = true)
    protected Long index;

    @XmlElement(name = "DocumentType", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String documentTypeCode;

    @XmlElement(name = "Description", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String description;

    @XmlElement(name = "Explanation", namespace = StringUtils.EMPTY, required = false)
    protected String explanation;

    @XmlElement(name = "OrganizationDocumentNumber", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String organizationDocumentNumber;

    @XmlElementWrapper(name = "AccountingListFrom", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Accounting", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAccountingLine> sourceAccountingLines;

    @XmlElementWrapper(name = "AccountingListTo", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Accounting", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAccountingLine> targetAccountingLines;

    @XmlElementWrapper(name = "NoteList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Note", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentNote> notes;

    @XmlElementWrapper(name = "AdHocRecipientList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Recipient", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAdHocRecipient> adHocRecipients;

    @XmlElementWrapper(name = "BackupDocumentLinks", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "BackupLink", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentBackupLink> backupLinks;

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getOrganizationDocumentNumber() {
        return organizationDocumentNumber;
    }

    public void setOrganizationDocumentNumber(String organizationDocumentNumber) {
        this.organizationDocumentNumber = organizationDocumentNumber;
    }

    public List<AccountingXmlDocumentAccountingLine> getSourceAccountingLines() {
        return sourceAccountingLines;
    }

    public void setSourceAccountingLines(List<AccountingXmlDocumentAccountingLine> sourceAccountingLines) {
        this.sourceAccountingLines = sourceAccountingLines;
    }

    public List<AccountingXmlDocumentAccountingLine> getTargetAccountingLines() {
        return targetAccountingLines;
    }

    public void setTargetAccountingLines(List<AccountingXmlDocumentAccountingLine> targetAccountingLines) {
        this.targetAccountingLines = targetAccountingLines;
    }

    public List<AccountingXmlDocumentNote> getNotes() {
        return notes;
    }

    public void setNotes(List<AccountingXmlDocumentNote> notes) {
        this.notes = notes;
    }

    public List<AccountingXmlDocumentAdHocRecipient> getAdHocRecipients() {
        return adHocRecipients;
    }

    public void setAdHocRecipients(List<AccountingXmlDocumentAdHocRecipient> adHocRecipients) {
        this.adHocRecipients = adHocRecipients;
    }

    public List<AccountingXmlDocumentBackupLink> getBackupLinks() {
        return backupLinks;
    }

    public void setBackupLinks(List<AccountingXmlDocumentBackupLink> backupLinks) {
        this.backupLinks = backupLinks;
    }

}
