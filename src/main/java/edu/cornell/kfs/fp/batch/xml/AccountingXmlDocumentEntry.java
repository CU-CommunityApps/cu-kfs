package edu.cornell.kfs.fp.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
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

    @XmlElement(name = "PostingFiscalYear", namespace = StringUtils.EMPTY, required = false)
    protected Integer postingFiscalYear;

    @XmlElement(name = "AccountingPeriod", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String accountingPeriod;

    @XmlElement(name = "AdjustmentAccrualVoucherType", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String adjustmentAccrualVoucherType;

    @XmlElement(name = "ReversalDate", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    protected Date reversalDate;

    @XmlElementWrapper(name = "SourceAccountingLineList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Accounting", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAccountingLine> sourceAccountingLines;

    @XmlElementWrapper(name = "TargetAccountingLineList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Accounting", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAccountingLine> targetAccountingLines;

    @XmlElementWrapper(name = "ItemList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Item", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentItem> items;

    @XmlElementWrapper(name = "NoteList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Note", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentNote> notes;

    @XmlElementWrapper(name = "AdhocRecipientList", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "Recipient", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentAdHocRecipient> adHocRecipients;

    @XmlElementWrapper(name = "BackupDocumentLinks", namespace = StringUtils.EMPTY, required = false)
    @XmlElement(name = "BackupLink", namespace = StringUtils.EMPTY, required = false)
    protected List<AccountingXmlDocumentBackupLink> backupLinks;
    
    @XmlElement(name = "dv_detail", namespace = StringUtils.EMPTY, required = false)
    protected DisbursementVoucherDetailXml disbursementVoucherDetail;

    @XmlTransient
    protected List<ValidationEvent> validationErrors;

    public AccountingXmlDocumentEntry() {
        this.sourceAccountingLines = new ArrayList<>();
        this.targetAccountingLines = new ArrayList<>();
        this.items = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.adHocRecipients = new ArrayList<>();
        this.backupLinks = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
    }

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

    public Integer getPostingFiscalYear() {
        return postingFiscalYear;
    }

    public void setPostingFiscalYear(Integer postingFiscalYear) {
        this.postingFiscalYear = postingFiscalYear;
    }

    public String getAccountingPeriod() {
        return accountingPeriod;
    }

    public void setAccountingPeriod(String accountingPeriod) {
        this.accountingPeriod = accountingPeriod;
    }

    public String getAdjustmentAccrualVoucherType() {
        return adjustmentAccrualVoucherType;
    }

    public void setAdjustmentAccrualVoucherType(String adjustmentAccrualVoucherType) {
        this.adjustmentAccrualVoucherType = adjustmentAccrualVoucherType;
    }

    public Date getReversalDate() {
        return reversalDate;
    }

    public void setReversalDate(Date reversalDate) {
        this.reversalDate = reversalDate;
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

    public List<AccountingXmlDocumentItem> getItems() {
        return items;
    }

    public void setItems(List<AccountingXmlDocumentItem> items) {
        this.items = items;
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

    public DisbursementVoucherDetailXml getDisbursementVoucherDetail() {
        return disbursementVoucherDetail;
    }

    public void setDisbursementVoucherDetail(DisbursementVoucherDetailXml disbursementVoucherDetail) {
        this.disbursementVoucherDetail = disbursementVoucherDetail;
    }

    public List<ValidationEvent> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationEvent> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void addValidationError(ValidationEvent validationError) {
        validationErrors.add(validationError);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccountingXmlDocumentEntry:: ").append(System.lineSeparator());
        sb.append("Index: ").append((ObjectUtils.isNotNull(index) ? index.intValue() : "null")).append(System.lineSeparator());
        sb.append("DocumentType: ").append(documentTypeCode).append(System.lineSeparator());
        sb.append("Description: ").append(description).append(System.lineSeparator());
        sb.append("Explanation: ").append(explanation).append(System.lineSeparator());
        sb.append("OrganizationDocumentNumber: ").append(organizationDocumentNumber).append(System.lineSeparator());
        sb.append("PostingFiscalYear: ").append((ObjectUtils.isNotNull(postingFiscalYear) ? postingFiscalYear.intValue() : "null")).append(System.lineSeparator());
        sb.append("AccountingPeriod: ").append(accountingPeriod).append(System.lineSeparator());
        sb.append("AdjustmentAccrualVoucherType: ").append(adjustmentAccrualVoucherType).append(System.lineSeparator());
        sb.append("ReversalDate: ").append(reversalDate).append(System.lineSeparator());
        return sb.toString();
    }
    
}
