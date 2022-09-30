package edu.cornell.kfs.fp.batch.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.action.ActionRequestType;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;

import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAdHocRecipient;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentBackupLink;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;

public abstract class AccountingDocumentGeneratorBase<T extends AccountingDocument> implements AccountingDocumentGenerator<T> {
	private static final Logger LOG = LogManager.getLogger(AccountingDocumentGeneratorBase.class);
    
    protected PersonService personService;
    protected Supplier<Note> emptyNoteGenerator;
    protected Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator;
    protected AccountingXmlDocumentDownloadAttachmentService accountingXmlDocumentDownloadAttachmentService;
    protected ConfigurationService configurationService;

    protected AccountingDocumentGeneratorBase() {
        this(Note::new, AdHocRoutePerson::new);
    }

    protected AccountingDocumentGeneratorBase(
            Supplier<Note> emptyNoteGenerator, Supplier<AdHocRoutePerson> emptyAdHocRoutePersonGenerator) {
        this.emptyNoteGenerator = emptyNoteGenerator;
        this.emptyAdHocRoutePersonGenerator = emptyAdHocRoutePersonGenerator;
    }

    @Override
    public T createDocument(Function<Class<? extends Document>, Document> emptyDocumentGenerator, AccountingXmlDocumentEntry documentEntry) {
        Document emptyDocument = emptyDocumentGenerator.apply(getDocumentClass());
        T document = getDocumentClass().cast(emptyDocument);
        
        populateDocumentHeader(document, documentEntry);
        populateAccountingLines(document, documentEntry);
        populateDocumentNotes(document, documentEntry);
        populateDocumentAttachments(document, documentEntry);
        populateAdHocRecipients(document, documentEntry);
        populateCustomAccountingDocumentData(document, documentEntry);
        return document;
    }

    protected void populateDocumentHeader(T document, AccountingXmlDocumentEntry documentEntry) {
        DocumentHeader documentHeader = document.getDocumentHeader();
        documentHeader.setDocumentDescription(documentEntry.getDescription());
        documentHeader.setExplanation(documentEntry.getExplanation());
        documentHeader.setOrganizationDocumentNumber(documentEntry.getOrganizationDocumentNumber());
    }

    @SuppressWarnings("unchecked")
    protected void populateAccountingLines(T document, AccountingXmlDocumentEntry documentEntry) {
        Class<? extends SourceAccountingLine> sourceAccountingLineClass = document.getSourceAccountingLineClass();
        Class<? extends TargetAccountingLine> targetAccountingLineClass = document.getTargetAccountingLineClass();
        String documentNumber = document.getDocumentNumber();
        
        documentEntry.getSourceAccountingLines().stream()
                .map((xmlLine) -> buildAccountingLine(sourceAccountingLineClass, documentNumber, xmlLine))
                .forEach(document::addSourceAccountingLine);
        
        documentEntry.getTargetAccountingLines().stream()
                .map((xmlLine) -> buildAccountingLine(targetAccountingLineClass, documentNumber, xmlLine))
                .forEach(document::addTargetAccountingLine);
    }

    protected <A extends AccountingLine> A buildAccountingLine(
            Class<A> accountingLineClass, String documentNumber, AccountingXmlDocumentAccountingLine xmlLine) {
        try {
            A accountingLine = accountingLineClass.newInstance();
            accountingLine.setDocumentNumber(documentNumber);
            accountingLine.setChartOfAccountsCode(xmlLine.getChartCode());
            accountingLine.setAccountNumber(xmlLine.getAccountNumber());
            accountingLine.setSubAccountNumber(xmlLine.getSubAccountNumber());
            accountingLine.setFinancialObjectCode(xmlLine.getObjectCode());
            accountingLine.setFinancialSubObjectCode(xmlLine.getSubObjectCode());
            accountingLine.setProjectCode(xmlLine.getProjectCode());
            accountingLine.setOrganizationReferenceId(xmlLine.getOrgRefId());
            accountingLine.setFinancialDocumentLineDescription(xmlLine.getLineDescription());
            accountingLine.setAmount(xmlLine.getAmount());
            return accountingLine;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected void populateDocumentNotes(T document, AccountingXmlDocumentEntry documentEntry) {
        documentEntry.getNotes().stream()
                .map(this::buildDocumentNote)
                .forEach(document::addNote);
    }
    
    protected Note buildDocumentNote(AccountingXmlDocumentNote xmlNote) {
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        Note note = emptyNoteGenerator.get();
        note.setNoteText(xmlNote.getDescription());
        note.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        note.setNotePostedTimestampToCurrent();
        return note;
    }
    
    protected void populateDocumentAttachments(T document, AccountingXmlDocumentEntry documentEntry) {
        documentEntry.getBackupLinks().stream()
            .filter(link -> StringUtils.isNotBlank(link.getLinkUrl()))
            .map(link -> buildDocumentNoteAttachment(document, link))
            .forEach(document::addNote);
    }
    
    protected Note buildDocumentNoteAttachment(T document, AccountingXmlDocumentBackupLink backupLink) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildDocumentNoteAttachment: " + backupLink);
        }
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        Note note = emptyNoteGenerator.get();
        note.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        note.setNotePostedTimestampToCurrent();
        addAttachmentToNote(document, backupLink, note);
        note.setNoteText(backupLink.getDescription());
        return note;
    }

    protected void addAttachmentToNote(T document, AccountingXmlDocumentBackupLink backupLink, Note note) {
        Attachment attachment = accountingXmlDocumentDownloadAttachmentService.createAttachmentFromBackupLink(document, backupLink);
        note.setAttachment(attachment);      
    }

    protected void populateAdHocRecipients(T document, AccountingXmlDocumentEntry documentEntry) {
        String documentNumber = document.getDocumentNumber();
        List<AdHocRoutePerson> adHocPersons = document.getAdHocRoutePersons();
        
        documentEntry.getAdHocRecipients().stream()
                .map((xmlRecipient) -> buildAdHocRoutePerson(xmlRecipient, documentNumber))
                .forEach(adHocPersons::add);
    }

    protected AdHocRoutePerson buildAdHocRoutePerson(AccountingXmlDocumentAdHocRecipient xmlRecipient, String documentNumber) {
        AdHocRoutePerson adHocPerson = emptyAdHocRoutePersonGenerator.get();
        adHocPerson.setdocumentNumber(documentNumber);
        adHocPerson.setId(xmlRecipient.getNetId());
        adHocPerson.setActionRequested(getActionRequestCode(xmlRecipient.getActionRequested()));
        return adHocPerson;
    }

    protected String getActionRequestCode(String actionRequestedLabel) {
        ActionRequestType actionRequestType = ActionRequestType.valueOf(StringUtils.upperCase(actionRequestedLabel, Locale.US));
        return actionRequestType.getCode();
    }

    protected void populateCustomAccountingDocumentData(T document, AccountingXmlDocumentEntry documentEntry) {
        // Do nothing by default.
    }
    
    @Override
    public void handleDocumentWarningMessage(CreateAccountingDocumentReportItemDetail reportDetail) {
        // Do nothing by default.
    }
    

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setAccountingXmlDocumentDownloadAttachmentService(
            AccountingXmlDocumentDownloadAttachmentService accountingXmlDocumentDownloadAttachmentService) {
        this.accountingXmlDocumentDownloadAttachmentService = accountingXmlDocumentDownloadAttachmentService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
