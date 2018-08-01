package edu.cornell.kfs.sys.util;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.easymock.EasyMock;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonEmployeeTravel;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPreConferenceDetail;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kim.impl.identity.PersonImpl;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

public class MockDocumentUtils {

    public static <T extends Document> T buildMockDocument(Class<T> documentClass) {
        return buildMockDocument(documentClass, (document) -> {});
    }

    public static <T extends AccountingDocument> T buildMockAccountingDocument(Class<T> documentClass) {
        return buildMockDocument(documentClass,
                (accountingDocument) -> setupMockedAccountingDocumentMethods(accountingDocument, documentClass),
                "getSourceAccountingLineClass", "getTargetAccountingLineClass");
    }

    private static void setupMockedAccountingDocumentMethods(AccountingDocument accountingDocument, Class<? extends AccountingDocument> documentClass) {
        Class<? extends SourceAccountingLine> sourceAccountingLineClass = AccountingDocumentClassMappingUtils
                .getSourceAccountingLineClassByDocumentClass(documentClass);
        Class<? extends TargetAccountingLine> targetAccountingLineClass = AccountingDocumentClassMappingUtils
                .getTargetAccountingLineClassByDocumentClass(documentClass);
        
        EasyMock.expect(accountingDocument.getSourceAccountingLineClass())
                .andStubReturn(sourceAccountingLineClass);
        EasyMock.expect(accountingDocument.getTargetAccountingLineClass())
                .andStubReturn(targetAccountingLineClass);
    }

    public static <T extends Document> T buildMockDocument(Class<T> documentClass, Consumer<? super T> documentMockingConfigurer, String... mockedMethods) {
        T document = EasyMock.partialMockBuilder(documentClass)
                .addMockedMethods(mockedMethods)
                .createNiceMock();
        documentMockingConfigurer.accept(document);
        EasyMock.replay(document);
        
        performInitializationFromSkippedConstructor(document);
        
        return document;
    }

    private static void performInitializationFromSkippedConstructor(Document document) {
        document.setDocumentHeader(new FinancialSystemDocumentHeader());
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
            dvDocument.setFinDocNextRegistrantLineNbr(new Integer(1));
            
        }
    }

    public static Note buildMockNote(String noteText) {
        Note note = buildMockNote();
        note.setNoteText(noteText);
        return note;
    }

    public static Note buildMockNote() {
        Note note = EasyMock.partialMockBuilder(Note.class)
                .addMockedMethod("setNotePostedTimestampToCurrent")
                .createNiceMock();
        
        note.setNotePostedTimestampToCurrent();
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.replay(note);
        return note;
    }

    public static AdHocRoutePerson buildMockAdHocRoutePerson() {
        TestAdHocRoutePerson adHocPerson = EasyMock.partialMockBuilder(TestAdHocRoutePerson.class)
                .createNiceMock();
        EasyMock.replay(adHocPerson);
        adHocPerson.setType(AdHocRouteRecipient.PERSON_TYPE);
        adHocPerson.setPerson(new PersonImpl());
        return adHocPerson;
    }

    /**
     * Helper AdHocRoutePerson subclass that allows for setting the recipient's ID
     * without invoking any service locators.
     */
    public static class TestAdHocRoutePerson extends AdHocRoutePerson {
        private static final long serialVersionUID = 1L;

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }
}
