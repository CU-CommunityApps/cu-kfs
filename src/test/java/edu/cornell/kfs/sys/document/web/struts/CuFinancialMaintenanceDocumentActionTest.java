package edu.cornell.kfs.sys.document.web.struts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.impl.document.WorkflowDocumentImpl;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.NoteType;

@SuppressWarnings("deprecation")
public class CuFinancialMaintenanceDocumentActionTest {

    private CuFinancialMaintenanceDocumentAction strutsAction;
    private Document testDocument;
    private Note testNote;

    @Before
    public void setUp() throws Exception {
        setupPartiallyMockedStrutsAction();
    }



    @Test
    public void testInitiatedAccountDocWithBoNote() throws Exception {
        setupMockMaintenanceDocument(Account.class, DocumentStatus.INITIATED, null);
        setupMockNote(NoteType.BUSINESS_OBJECT);
        assertFalse("INITIATED Account maintenance document should not allow doc-auto-saving for BO note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
    }

    @Test
    public void testSavedAccountDocWithBoNote() throws Exception {
        setupMockMaintenanceDocument(Account.class, DocumentStatus.SAVED, "08642");
        setupMockNote(NoteType.BUSINESS_OBJECT);
        assertTrue("SAVED Account maintenance document should allow doc-auto-saving for BO note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
        assertTrue("SAVED Account maintenance document should have been in a state to save notes to database, due to non-blank object ID on BO",
                strutsAction.isTargetReadyForNotes(testDocument));
    }

    @Test
    public void testEnrouteAccountDocWithBoNote() throws Exception {
        setupMockMaintenanceDocument(Account.class, DocumentStatus.ENROUTE, null);
        setupMockNote(NoteType.BUSINESS_OBJECT);
        assertTrue("ENROUTE Account maintenance document should allow doc-auto-saving for BO note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
        assertFalse("ENROUTE Account maintenance document should not have been in a state to save notes to database, due to blank object ID on BO",
                strutsAction.isTargetReadyForNotes(testDocument));
    }

    @Test
    public void testFinalAccountDocWithBoNote() throws Exception {
        setupMockMaintenanceDocument(Account.class, DocumentStatus.FINAL, "12345678");
        setupMockNote(NoteType.BUSINESS_OBJECT);
        assertTrue("FINAL Account maintenance document should allow doc-auto-saving for BO note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
        assertTrue("FINAL Account maintenance document should have been in a state to save notes to database, due to non-blank object ID on BO",
                strutsAction.isTargetReadyForNotes(testDocument));
    }

    @Test
    public void testSavedAccountDocWithDocHeaderNote() throws Exception {
        setupMockMaintenanceDocument(Account.class, DocumentStatus.SAVED, null);
        setupMockNote(NoteType.DOCUMENT_HEADER);
        assertFalse("SAVED Account maintenance document should not allow doc-auto-saving for DH note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
    }

    @Test
    public void testOrganizationDocWithDocHeaderNote() throws Exception {
        setupMockMaintenanceDocument(Organization.class, DocumentStatus.SAVED, null);
        setupMockNote(NoteType.DOCUMENT_HEADER);
        assertFalse("SAVED Organization maintenance document should not allow doc-auto-saving for DH note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
    }

    @Test
    public void testSavedVendorDocWithBoNote() throws Exception {
        setupMockMaintenanceDocument(VendorDetail.class, DocumentStatus.SAVED, null);
        setupMockNote(NoteType.BUSINESS_OBJECT);
        assertFalse("SAVED Vendor maintenance document should not allow doc-auto-saving for BO note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
    }

    @Test
    public void testTransactionalDocWithDocHeaderNote() throws Exception {
        setupMockTransactionalDocument(DocumentStatus.SAVED);
        setupMockNote(NoteType.DOCUMENT_HEADER);
        assertFalse("Transactional document should not allow doc-auto-saving for note change",
                strutsAction.shouldSaveBoNoteAfterUpdate(testDocument, testNote));
    }



    protected void setupPartiallyMockedStrutsAction() {
        List<String> mockedMethods = new ArrayList<>();
        Set<String> usableOrOverloadedMethods = new HashSet<>(Arrays.asList(
                "shouldSaveDocumentAfterNoteUpdate", "promptBeforeValidation"));
        for (Method method : CuFinancialMaintenanceDocumentAction.class.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !usableOrOverloadedMethods.contains(method.getName())) {
                mockedMethods.add(method.getName());
            }
        }
        IMockBuilder<CuFinancialMaintenanceDocumentAction> builder = EasyMock.createMockBuilder(
                CuFinancialMaintenanceDocumentAction.class).addMockedMethods(mockedMethods.toArray(new String[0]));
        strutsAction = builder.createMock();
        EasyMock.replay(strutsAction);
    }

    protected void setupMockMaintenanceDocument(Class<? extends PersistableBusinessObject> dataObjectClass, DocumentStatus documentStatus, String objectId) {
        Maintainable mockMaintainable = createMockMaintainable(dataObjectClass, objectId);
        testDocument = EasyMock.createMock(FinancialSystemMaintenanceDocument.class);
        EasyMock.expect(((FinancialSystemMaintenanceDocument) testDocument).getNewMaintainableObject()).andStubReturn(
                mockMaintainable);
        EasyMock.expect(testDocument.getNoteTarget()).andStubReturn(
                (PersistableBusinessObject) mockMaintainable.getDataObject());
        EasyMock.expect(testDocument.getDocumentHeader()).andStubReturn(
                createMockDocumentHeader(documentStatus));
        EasyMock.replay(testDocument);
    }

    protected void setupMockTransactionalDocument(DocumentStatus documentStatus) {
        testDocument = EasyMock.createMock(FinancialSystemTransactionalDocumentBase.class);
        EasyMock.expect(testDocument.getDocumentHeader()).andStubReturn(
                createMockDocumentHeader(documentStatus));
        EasyMock.replay(testDocument);
    }

    protected Maintainable createMockMaintainable(Class<? extends PersistableBusinessObject> dataObjectClass, String objectId) {
        PersistableBusinessObject mockDataObject = createMockDataObject(dataObjectClass, objectId);
        Maintainable maintainable = EasyMock.createMock(FinancialSystemMaintainable.class);
        EasyMock.expect(maintainable.getDataObjectClass()).andStubReturn(dataObjectClass);
        EasyMock.expect(maintainable.getDataObject()).andStubReturn(mockDataObject);
        EasyMock.expect(maintainable.getBusinessObject()).andStubReturn(mockDataObject);
        EasyMock.replay(maintainable);
        return maintainable;
    }

    protected <T extends PersistableBusinessObject> T createMockDataObject(Class<T> dataObjectClass, String objectId) {
        T dataObject = EasyMock.createMock(dataObjectClass);
        EasyMock.expect(dataObject.getObjectId()).andStubReturn(objectId);
        EasyMock.replay(dataObject);
        return dataObject;
    }

    protected DocumentHeader createMockDocumentHeader(DocumentStatus documentStatus) {
        DocumentHeader documentHeader = EasyMock.createMock(FinancialSystemDocumentHeader.class);
        EasyMock.expect(documentHeader.getWorkflowDocument()).andStubReturn(
                createMockWorkflowDocument(documentStatus));
        EasyMock.replay(documentHeader);
        return documentHeader;
    }

    protected WorkflowDocument createMockWorkflowDocument(DocumentStatus documentStatus) {
        WorkflowDocument workflowDocument = EasyMock.createMock(WorkflowDocumentImpl.class);
        EasyMock.expect(workflowDocument.getStatus()).andStubReturn(documentStatus);
        EasyMock.expect(workflowDocument.isInitiated()).andStubReturn(DocumentStatus.INITIATED.equals(documentStatus));
        EasyMock.expect(workflowDocument.isSaved()).andStubReturn(DocumentStatus.SAVED.equals(documentStatus));
        EasyMock.expect(workflowDocument.isEnroute()).andStubReturn(DocumentStatus.ENROUTE.equals(documentStatus));
        EasyMock.expect(workflowDocument.isException()).andStubReturn(DocumentStatus.EXCEPTION.equals(documentStatus));
        EasyMock.expect(workflowDocument.isProcessed()).andStubReturn(DocumentStatus.PROCESSED.equals(documentStatus));
        EasyMock.expect(workflowDocument.isFinal()).andStubReturn(DocumentStatus.FINAL.equals(documentStatus));
        EasyMock.expect(workflowDocument.isCanceled()).andStubReturn(DocumentStatus.CANCELED.equals(documentStatus));
        EasyMock.expect(workflowDocument.isDisapproved()).andStubReturn(DocumentStatus.DISAPPROVED.equals(documentStatus));
        EasyMock.expect(workflowDocument.isRecalled()).andStubReturn(DocumentStatus.RECALLED.equals(documentStatus));
        EasyMock.replay(workflowDocument);
        return workflowDocument;
    }

    protected void setupMockNote(NoteType noteType) {
        testNote = EasyMock.createMock(Note.class);
        EasyMock.expect(testNote.getNoteType()).andStubReturn(
                createMockNoteTypeBo(noteType));
        EasyMock.expect(testNote.getNoteTypeCode()).andStubReturn(noteType.getCode());
        EasyMock.replay(testNote);
    }

    protected org.kuali.kfs.krad.bo.NoteType createMockNoteTypeBo(NoteType noteTypeEnum) {
        org.kuali.kfs.krad.bo.NoteType noteTypeBo = EasyMock.createMock(org.kuali.kfs.krad.bo.NoteType.class);
        EasyMock.expect(noteTypeBo.getNoteTypeCode()).andStubReturn(noteTypeEnum.getCode());
        EasyMock.replay(noteTypeBo);
        return noteTypeBo;
    }
}
