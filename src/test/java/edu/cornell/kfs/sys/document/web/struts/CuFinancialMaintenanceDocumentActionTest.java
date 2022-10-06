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

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.impl.document.WorkflowDocumentImpl;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.NoteType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

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
            strutsAction = mock(CuFinancialMaintenanceDocumentAction.class, CALLS_REAL_METHODS);
    }

    protected void setupMockMaintenanceDocument(Class<? extends PersistableBusinessObject> dataObjectClass, DocumentStatus documentStatus, String objectId) {
        Maintainable mockMaintainable = createMockMaintainable(dataObjectClass, objectId);
        PersistableBusinessObject mockDataObject = (PersistableBusinessObject) mockMaintainable.getDataObject();
        DocumentHeader mockDocumentHeader = createMockDocumentHeader(documentStatus);
        testDocument = mock(FinancialSystemMaintenanceDocument.class);
        when(((FinancialSystemMaintenanceDocument) testDocument).getNewMaintainableObject()).thenReturn(mockMaintainable);
        when(testDocument.getNoteTarget()).thenReturn(mockDataObject);
        when(testDocument.getDocumentHeader()).thenReturn(mockDocumentHeader);
    }

    protected void setupMockTransactionalDocument(DocumentStatus documentStatus) {
        testDocument = mock(FinancialSystemTransactionalDocumentBase.class);
        DocumentHeader mockDocumentHeader = createMockDocumentHeader(documentStatus);
        when(testDocument.getDocumentHeader()).thenReturn(mockDocumentHeader);
    }

    protected Maintainable createMockMaintainable(Class<? extends PersistableBusinessObject> dataObjectClass, String objectId) {
        PersistableBusinessObject mockDataObject = createMockDataObject(dataObjectClass, objectId);
        Maintainable maintainable = mock(FinancialSystemMaintainable.class);
        when(maintainable.getDataObjectClass()).thenReturn(dataObjectClass);
        when(maintainable.getDataObject()).thenReturn(mockDataObject);
        when(maintainable.getBusinessObject()).thenReturn(mockDataObject);
        return maintainable;
    }

    protected <T extends PersistableBusinessObject> T createMockDataObject(Class<T> dataObjectClass, String objectId) {
        T dataObject = mock(dataObjectClass);
        when(dataObject.getObjectId()).thenReturn(objectId);
        return dataObject;
    }

    protected DocumentHeader createMockDocumentHeader(DocumentStatus documentStatus) {
        DocumentHeader documentHeader = mock(DocumentHeader.class);
        WorkflowDocument mockWorkflowDocument = createMockWorkflowDocument(documentStatus);
        when(documentHeader.getWorkflowDocument()).thenReturn(mockWorkflowDocument);
        return documentHeader;
    }

    protected WorkflowDocument createMockWorkflowDocument(DocumentStatus documentStatus) {
        WorkflowDocument workflowDocument = mock(WorkflowDocumentImpl.class);
        when(workflowDocument.getStatus()).thenReturn(documentStatus);
        when(workflowDocument.isInitiated()).thenReturn(DocumentStatus.INITIATED.equals(documentStatus));
        when(workflowDocument.isSaved()).thenReturn(DocumentStatus.SAVED.equals(documentStatus));
        when(workflowDocument.isEnroute()).thenReturn(DocumentStatus.ENROUTE.equals(documentStatus));
        when(workflowDocument.isException()).thenReturn(DocumentStatus.EXCEPTION.equals(documentStatus));
        when(workflowDocument.isProcessed()).thenReturn(DocumentStatus.PROCESSED.equals(documentStatus));
        when(workflowDocument.isFinal()).thenReturn(DocumentStatus.FINAL.equals(documentStatus));
        when(workflowDocument.isCanceled()).thenReturn(DocumentStatus.CANCELED.equals(documentStatus));
        when(workflowDocument.isDisapproved()).thenReturn(DocumentStatus.DISAPPROVED.equals(documentStatus));
        when(workflowDocument.isRecalled()).thenReturn(DocumentStatus.RECALLED.equals(documentStatus));
        
        return workflowDocument;
    }

    protected void setupMockNote(NoteType noteType) {
        testNote = mock(Note.class);
        org.kuali.kfs.krad.bo.NoteType mockNoteType = createMockNoteTypeBo(noteType);
        when(testNote.getNoteType()).thenReturn(mockNoteType);
        when(testNote.getNoteTypeCode()).thenReturn(noteType.getCode());
    }

    protected org.kuali.kfs.krad.bo.NoteType createMockNoteTypeBo(NoteType noteTypeEnum) {
        org.kuali.kfs.krad.bo.NoteType noteTypeBo = mock(org.kuali.kfs.krad.bo.NoteType.class);
        when(noteTypeBo.getNoteTypeCode()).thenReturn(noteTypeEnum.getCode());
        return noteTypeBo;
    }
}
