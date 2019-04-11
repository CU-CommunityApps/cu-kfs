package edu.cornell.kfs.fp.document.web.struts;

import edu.cornell.kfs.fp.document.PreEncumbranceDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.document.LedgerPostingDocumentBase;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@RunWith(PowerMockRunner.class)
@PrepareForTest({PreEncumbranceDocument.class, KualiAccountingDocumentFormBase.class})
@SuppressWarnings({"rawtypes", "deprecation"})
public class CuPreEncumbranceActionTest {

    private static final String ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_NEW_SOURCE_LINE_TAB_NAME = "EncumbranceAutomaticPartialDisEncumbrances-newSourceLine";
    private static final String OPEN_TAB_STATE = "OPEN";
    private static final String AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_SECTION_FOR_NEW_SOURCE_LINE_SHOULD_BE_OPEN_ASSERTION_MESSAGE = "Automatic Partial Dis-Encumbrances section for new source line should be OPEN";
    private static final String TAB_STATES_SHOULD_NOT_BE_EMPTY_ASSERTION_MESSAGE = "TabStates should NOT be empty";
    private CuPreEncumbranceAction cuPreEncumbranceAction;
    private KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase;
    private PreEncumbranceDocument preEncumbranceDocument;

    @Before
    public void setUp() {
        preEncumbranceDocument = buildMockPreEncumbranceDocument();
        preEncumbranceDocument.setSourceAccountingLines(new ArrayList());

        kualiAccountingDocumentFormBase = buildMockKualiAccountingDocumentFormBase();
        kualiAccountingDocumentFormBase.setTabStates(new HashMap<>());
        kualiAccountingDocumentFormBase.setDocument(preEncumbranceDocument);

        cuPreEncumbranceAction = new CuPreEncumbranceAction();
    }
    
    private KualiAccountingDocumentFormBase buildMockKualiAccountingDocumentFormBase() {
        PowerMockito.suppress(PowerMockito.constructor(KualiAccountingDocumentFormBase.class));
        KualiAccountingDocumentFormBase document = PowerMockito.spy(new KualiAccountingDocumentFormBase());
        return document;
    }
    
    private PreEncumbranceDocument buildMockPreEncumbranceDocument() {
        PowerMockito.suppress(PowerMockito.constructor(LedgerPostingDocumentBase.class));
        PreEncumbranceDocument document = PowerMockito.spy(new PreEncumbranceDocument());
        return document;
    }

    @Test
    public void setTabStates() throws Exception {
        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse(TAB_STATES_SHOULD_NOT_BE_EMPTY_ASSERTION_MESSAGE, kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals(AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_SECTION_FOR_NEW_SOURCE_LINE_SHOULD_BE_OPEN_ASSERTION_MESSAGE, OPEN_TAB_STATE, 
                kualiAccountingDocumentFormBase.getTabState(ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_NEW_SOURCE_LINE_TAB_NAME));
    }

    @Test
    public void setTabStatesHasGLPEs() throws Exception {
        List<GeneralLedgerPendingEntry> generalLedgerPendingEntries = new ArrayList<>();
        generalLedgerPendingEntries.add(new GeneralLedgerPendingEntry());
        generalLedgerPendingEntries.add(new GeneralLedgerPendingEntry());
        preEncumbranceDocument.setGeneralLedgerPendingEntries(generalLedgerPendingEntries);
        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse(TAB_STATES_SHOULD_NOT_BE_EMPTY_ASSERTION_MESSAGE, kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals("General Ledger Pending Entries tab should be OPEN",OPEN_TAB_STATE, kualiAccountingDocumentFormBase.getTabState("GeneralLedgerPendingEntries"));
    }

    @Test
    public void setTabStatesHasTwoAccountingLines() throws Exception {
        List<SourceAccountingLine> sourceAccountingLines = new ArrayList<>();
        sourceAccountingLines.add(new SourceAccountingLine());
        sourceAccountingLines.add(new SourceAccountingLine());
        preEncumbranceDocument.setSourceAccountingLines(sourceAccountingLines);

        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse(TAB_STATES_SHOULD_NOT_BE_EMPTY_ASSERTION_MESSAGE, kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals(AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_SECTION_FOR_NEW_SOURCE_LINE_SHOULD_BE_OPEN_ASSERTION_MESSAGE, OPEN_TAB_STATE, 
                kualiAccountingDocumentFormBase.getTabState(ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_NEW_SOURCE_LINE_TAB_NAME));
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for first source accounting line should be OPEN",OPEN_TAB_STATE, 
                kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine(0)"));
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for second source accounting line should be OPEN",OPEN_TAB_STATE, 
                kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine(1)"));
    }

}