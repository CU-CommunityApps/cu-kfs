package edu.cornell.kfs.sys.util;

import java.util.ArrayList;

import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.LedgerPostingDocumentBase;
import org.powermock.api.mockito.PowerMockito;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.kim.impl.identity.PersonExtension;

public final class MockDocumentUtils {
    
    private MockDocumentUtils() {
        
    }

    public static <T extends Document> T buildMockDocument(Class<T> documentClass) {
        PowerMockito.suppress(PowerMockito.constructor(LedgerPostingDocumentBase.class));
        T document = null;
        try {
            document = PowerMockito.spy(documentClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        performInitializationFromSkippedConstructor(document);
        return document;
    }

    public static <T extends AccountingDocument> T buildMockAccountingDocument(Class<T> documentClass) {
        T document = buildMockDocument(documentClass);
        
        Class<? extends SourceAccountingLine> sourceAccountingLineClass = AccountingDocumentClassMappingUtils
                .getSourceAccountingLineClassByDocumentClass(documentClass);
        Class<? extends TargetAccountingLine> targetAccountingLineClass = AccountingDocumentClassMappingUtils
                .getTargetAccountingLineClassByDocumentClass(documentClass);
        
        try {
            PowerMockito.doReturn(sourceAccountingLineClass).when(document, "getSourceAccountingLineClass");
            PowerMockito.doReturn(targetAccountingLineClass).when(document, "getTargetAccountingLineClass");
        } catch (Exception e) {
            throw new RuntimeException(e);
            
        }
        return document;
    }

    private static void performInitializationFromSkippedConstructor(Document document) {
        document.setDocumentHeader(new DocumentHeader());
        document.setAdHocRoutePersons(new ArrayList<>());
        document.setAdHocRouteWorkgroups(new ArrayList<>());
        document.setNotes(new ArrayList<>());
        
        if (document instanceof AccountingDocument) {
            AccountingDocument accountingDocument = (AccountingDocument) document;
            accountingDocument.setSourceAccountingLines(new ArrayList<>());
            accountingDocument.setTargetAccountingLines(new ArrayList<>());
            accountingDocument.setNextSourceLineNumber(Integer.valueOf(1));
            accountingDocument.setNextTargetLineNumber(Integer.valueOf(1));
        }
        
        if (document instanceof InternalBillingDocument) {
            InternalBillingDocument internalBillingDocument = (InternalBillingDocument) document;
            internalBillingDocument.setItems(new ArrayList<>());
            internalBillingDocument.setNextItemLineNumber(Integer.valueOf(1));
        }
        
        if (document instanceof PurchasingAccountsPayableDocument) {
            PurchasingAccountsPayableDocument purapDocument = (PurchasingAccountsPayableDocument) document;
            purapDocument.setItems(new ArrayList<>());
        }
        
        if (document instanceof PurchasingDocument) {
            PurchasingDocument purchasingDocument = (PurchasingDocument) document;
            purchasingDocument.setPurchasingCapitalAssetItems(new ArrayList<>());
            purchasingDocument.setPurchasingCapitalAssetSystems(new ArrayList<>());
        }
        
        if (document instanceof CuDisbursementVoucherDocument) {
            CuDisbursementVoucherDocument dvDocument = (CuDisbursementVoucherDocument) document;
            CuDisbursementVoucherPayeeDetail dvPayeeDetail = new CuDisbursementVoucherPayeeDetail();
            dvDocument.setDvPayeeDetail(dvPayeeDetail);
            DisbursementVoucherNonEmployeeTravel dvNonEmployeeTravel = new DisbursementVoucherNonEmployeeTravel();
            dvDocument.setDvNonEmployeeTravel(dvNonEmployeeTravel);
            DisbursementVoucherPreConferenceDetail dvPreConferenceDetail = new DisbursementVoucherPreConferenceDetail();
            dvDocument.setDvPreConferenceDetail(dvPreConferenceDetail);
            dvDocument.setFinDocNextRegistrantLineNbr(1);
            
        }
    }

    /*
     * If you use this function you must add the following annotation to your unit test
     * @PrepareForTest({MockDocumentUtils.TestNote.class})
     */
    public static Note buildMockNote(String noteText) {
        Note note = buildMockNote();
        note.setNoteText(noteText);
        return note;
    }
    
    /*
     * If you use this function you must add the following annotation to your unit test
     * @PrepareForTest({MockDocumentUtils.TestNote.class})
     */
    public static Note buildMockNote() {
        PowerMockito.suppress(PowerMockito.constructor(Note.class));
        TestNote note = PowerMockito.spy(new TestNote());
        try {
            PowerMockito.doNothing().when(note, "setNotePostedTimestampToCurrent");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        note.setNotePostedTimestampToCurrent();
        
        return note;
    }
    
    public static class TestNote extends Note {
        private static final long serialVersionUID = 5758013202304326694L;
        
    }
    
    /*
     * If you use this function you must add the following annotation to your unit test
     * @PrepareForTest({MockDocumentUtils.TestAdHocRoutePerson.class})
     */
    public static AdHocRoutePerson buildMockAdHocRoutePerson() {
        Person person = new Person();
        person.setExtension(new PersonExtension());

        PowerMockito.suppress(PowerMockito.constructor(AdHocRoutePerson.class));
        TestAdHocRoutePerson adHocPerson = PowerMockito.spy(new TestAdHocRoutePerson());
        adHocPerson.setType(AdHocRouteRecipient.PERSON_TYPE);
        adHocPerson.setPerson(person);
        return adHocPerson;
    }

    public static class TestAdHocRoutePerson extends AdHocRoutePerson {
        private static final long serialVersionUID = 1L;

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }
}
