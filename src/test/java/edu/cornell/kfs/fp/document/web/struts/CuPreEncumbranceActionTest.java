package edu.cornell.kfs.fp.document.web.struts;

import edu.cornell.kfs.fp.document.PreEncumbranceDocument;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CuPreEncumbranceActionTest {

    private CuPreEncumbranceAction cuPreEncumbranceAction;
    private KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase;
    private PreEncumbranceDocument preEncumbranceDocument;

    @Before
    public void setUp() {
        preEncumbranceDocument = createMock(PreEncumbranceDocument.class);
        preEncumbranceDocument.setSourceAccountingLines(new ArrayList());

        kualiAccountingDocumentFormBase = createMock(KualiAccountingDocumentFormBase.class);
        kualiAccountingDocumentFormBase.setTabStates(new HashMap<>());
        kualiAccountingDocumentFormBase.setDocument(preEncumbranceDocument);

        cuPreEncumbranceAction = new CuPreEncumbranceAction();
    }

    private <T> T createMock(Class<T> classToMock) {
        ArrayList<String> methodNames = new ArrayList<>();
        for (Method method : classToMock.getMethods()) {
            if (!Modifier.isFinal(method.getModifiers()) && !method.getName().startsWith("set") && !method.getName().startsWith("get")) {
                methodNames.add(method.getName());
            }
        }
        IMockBuilder builder = EasyMock.createMockBuilder(classToMock).addMockedMethods(methodNames.toArray(new String[0]));
        return (T) builder.createNiceMock();
    }

    @Test
    public void setTabStates() throws Exception {
        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse("TabStates should NOT be empty", kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for new source line should be OPEN", "OPEN", kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-newSourceLine"));
    }

    @Test
    public void setTabStatesHasGLPEs() throws Exception {
        List<GeneralLedgerPendingEntry> generalLedgerPendingEntries = new ArrayList<>();
        generalLedgerPendingEntries.add(new GeneralLedgerPendingEntry());
        generalLedgerPendingEntries.add(new GeneralLedgerPendingEntry());
        preEncumbranceDocument.setGeneralLedgerPendingEntries(generalLedgerPendingEntries);
        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse("TabStates should NOT be empty", kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals("General Ledger Pending Entries tab should be OPEN","OPEN", kualiAccountingDocumentFormBase.getTabState("GeneralLedgerPendingEntries"));
    }

    @Test
    public void setTabStatesHasTwoAccountingLines() throws Exception {
        List<SourceAccountingLine> sourceAccountingLines = new ArrayList<>();
        sourceAccountingLines.add(new SourceAccountingLine());
        sourceAccountingLines.add(new SourceAccountingLine());
        preEncumbranceDocument.setSourceAccountingLines(sourceAccountingLines);

        cuPreEncumbranceAction.setTabStates(kualiAccountingDocumentFormBase);

        Assert.assertFalse("TabStates should NOT be empty", kualiAccountingDocumentFormBase.getTabStates().isEmpty());
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for new source line should be OPEN", "OPEN", kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-newSourceLine"));
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for first source accounting line should be OPEN","OPEN", kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine(0)"));
        Assert.assertEquals("Automatic Partial Dis-Encumbrances section for second source accounting line should be OPEN","OPEN", kualiAccountingDocumentFormBase.getTabState("EncumbranceAutomaticPartialDisEncumbrances-document-sourceAccountingLine(1)"));
    }

}