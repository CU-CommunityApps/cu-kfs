package edu.cornell.kfs.fp.batch.service.impl;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.action.ActionRequestType;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAdHocRecipient;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentNote;

public abstract class AccountingDocumentGeneratorBase<T extends AccountingDocument> implements AccountingDocumentGenerator<T> {

    protected PersonService personService;
    protected Supplier<Note> bareNoteGenerator;
    protected Supplier<AdHocRoutePerson> bareAdHocRoutePersonGenerator;

    protected AccountingDocumentGeneratorBase() {
        this(Note::new, AdHocRoutePerson::new);
    }

    protected AccountingDocumentGeneratorBase(
            Supplier<Note> bareNoteGenerator, Supplier<AdHocRoutePerson> bareAdHocRoutePersonGenerator) {
        this.bareNoteGenerator = bareNoteGenerator;
        this.bareAdHocRoutePersonGenerator = bareAdHocRoutePersonGenerator;
    }

    protected abstract Class<? extends T> getDocumentClass();

    @Override
    public T createDocument(Function<Class<? extends Document>, Document> bareDocumentGenerator, AccountingXmlDocumentEntry documentEntry) {
        Document bareDocument = bareDocumentGenerator.apply(getDocumentClass());
        T document = getDocumentClass().cast(bareDocument);
        
        populateDocumentHeader(document, documentEntry);
        populateAccountingLines(document, documentEntry);
        populateDocumentNotes(document, documentEntry);
        populateAdHocRecipients(document, documentEntry);
        populateCustomAccountingDocumentData(document, documentEntry);
        return document;
    }

    protected void populateDocumentHeader(T document, AccountingXmlDocumentEntry documentEntry) {
        FinancialSystemDocumentHeader documentHeader = (FinancialSystemDocumentHeader) document.getDocumentHeader();
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
        Note note = bareNoteGenerator.get();
        note.setNoteText(xmlNote.getDescription());
        note.setAuthorUniversalIdentifier(systemUser.getPrincipalId());
        note.setNotePostedTimestampToCurrent();
        return note;
    }

    protected void populateAdHocRecipients(T document, AccountingXmlDocumentEntry documentEntry) {
        String documentNumber = document.getDocumentNumber();
        List<AdHocRoutePerson> adHocPersons = document.getAdHocRoutePersons();
        
        documentEntry.getAdHocRecipients().stream()
                .map((xmlRecipient) -> buildAdHocRoutePerson(xmlRecipient, documentNumber))
                .forEach(adHocPersons::add);
    }

    protected AdHocRoutePerson buildAdHocRoutePerson(AccountingXmlDocumentAdHocRecipient xmlRecipient, String documentNumber) {
        AdHocRoutePerson adHocPerson = bareAdHocRoutePersonGenerator.get();
        adHocPerson.setdocumentNumber(documentNumber);
        adHocPerson.setId(xmlRecipient.getNetId());
        adHocPerson.setActionRequested(getActionRequestCode(xmlRecipient.getActionRequested()));
        return adHocPerson;
    }

    protected String getActionRequestCode(String actionRequestedLabel) {
        ActionRequestType actionRequestType = ActionRequestType.valueOf(StringUtils.upperCase(actionRequestedLabel));
        return actionRequestType.getCode();
    }

    protected void populateCustomAccountingDocumentData(T document, AccountingXmlDocumentEntry documentEntry) {
        // Do nothing by default.
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
