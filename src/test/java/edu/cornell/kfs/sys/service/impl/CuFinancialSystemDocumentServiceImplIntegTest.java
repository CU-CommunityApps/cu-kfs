package edu.cornell.kfs.sys.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.List;

import org.kuali.kfs.fp.document.IndirectCostAdjustmentDocument;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentIntegTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.sys.document.service.CUFinancialSystemDocumentService;
import edu.cornell.kfs.sys.service.impl.fixture.IndirectCostAdjustmentDocumentFixture;

@ConfigureContext(session = ccs1)
public class CuFinancialSystemDocumentServiceImplIntegTest extends KualiIntegTestBase {
    private CUFinancialSystemDocumentService cUFinancialSystemDocumentService;
    IndirectCostAdjustmentDocument icaDocument;
    IndirectCostAdjustmentDocument copied;   
    List<Note> savedNotes;
    List<Note> originalNotes;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cUFinancialSystemDocumentService = SpringContext.getBean(CUFinancialSystemDocumentService.class);
        icaDocument = IndirectCostAdjustmentDocumentFixture.ICA_GOOD.createIndirectCostAdjustmentDocument();
        AccountingDocumentIntegTestUtils.routeDocument(icaDocument, SpringContext.getBean(DocumentService.class));
        // switch user to FO
        changeCurrentUser(UserNameFixture.kan2);
        // icaDocument, and the 'saveDoc' in cUFinancialSystemDocumentService are the same instance.
        // So, need a copy
        copied = (IndirectCostAdjustmentDocument)ObjectUtils.deepCopy(icaDocument);
        savedNotes = copied.getNotes();
        originalNotes = icaDocument.getNotes();
    }

    /*
     * test Add note for accounting line change
     */
    public void testAddNoteForAccountingLineChange() {
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        assertTrue("Should have no New note added", savedNotes.size() == originalNotes.size());
        
        copied.getSourceAccountingLine(0).setOrganizationReferenceId("changed");
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        // Note is only created, but not saved yet.
        assertTrue("Should have new note added for Source referenceid change ", savedNotes.size() > originalNotes.size());
        assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
        copied.getSourceAccountingLine(0).setOrganizationReferenceId("");
        copied.getTargetAccountingLine(0).setOrganizationReferenceId("changed");
        savedNotes.clear();
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        assertTrue("Should have new note added for Target referenceid change ", savedNotes.size() > originalNotes.size());
        assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
        copied.getTargetAccountingLine(0).setOrganizationReferenceId("");
        copied.getSourceAccountingLine(0).setAccountNumber("G254700");
        savedNotes.clear();
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        assertTrue("Should have new note added for Source Account Number change ", savedNotes.size() > originalNotes.size());
        assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
        copied.getSourceAccountingLine(0).setAccountNumber("G264750");
        copied.getTargetAccountingLine(0).setAccountNumber("G264750");
        savedNotes.clear();
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        // Note is only created, but not saved yet.
        assertTrue("Should have new note added for Target Account Number change ", savedNotes.size() > originalNotes.size());
        assertTrue("New note is accounting line change note ", savedNotes.get(0).getNoteText().startsWith("Accounting Line changed from:"));
    }
    
    /*
     * test no note being added for no accounting line change
     */
    public void testNotAddNoteForNoAccountingLineChange() {
        cUFinancialSystemDocumentService.checkAccountingLinesForChanges(copied);;
        assertTrue("Should have no New note added", savedNotes.size() == originalNotes.size());
    }

}
