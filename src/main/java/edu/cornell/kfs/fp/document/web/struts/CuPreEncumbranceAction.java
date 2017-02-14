package edu.cornell.kfs.fp.document.web.struts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.fp.CuFPConstants;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.PreEncumbranceDocument;
import org.kuali.kfs.fp.document.web.struts.PreEncumbranceAction;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;

public class CuPreEncumbranceAction extends PreEncumbranceAction  {

    String dot = ".";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward actionForward = super.execute(mapping, form, request, response);
        setTabStates(form);
        return actionForward;
    }

    protected void setTabStates(ActionForm form) {
        KualiAccountingDocumentFormBase kualiForm = (KualiAccountingDocumentFormBase) form;
        AccountingDocument accountingDocument = kualiForm.getFinancialDocument();
        Map<String, String> tabStates = kualiForm.getTabStates();

        setAutomaticPartialDisEncumbrancesTabsOpen(accountingDocument, tabStates);
        setGeneralLedgerPendingEntriesTabOpenWhenGLPEsExist(accountingDocument, tabStates);

        kualiForm.setTabStates(tabStates);
    }

    private void setAutomaticPartialDisEncumbrancesTabsOpen(AccountingDocument accountingDocument, Map<String, String> tabStates) {
        tabStates.put(buildTabIdForNewSourceLine(), CuFPConstants.OPEN);

        for (int i = 0;i < accountingDocument.getSourceAccountingLines().size(); i++) {
            tabStates.put(buildTabIdForSourceAccountingLines(i), CuFPConstants.OPEN);
        }
    }

    private String buildTabIdForSourceAccountingLines(int i) {
        return WebUtils.generateTabKey(CuFPConstants.ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_TITLE) +
                KFSConstants.DASH + KFSConstants.DOCUMENT_PROPERTY_NAME +
                KFSConstants.DASH + KFSPropertyConstants.SOURCE_ACCOUNTING_LINE +
                CuFPConstants.LEFT_PARENTHESIS + i + CuFPConstants.RIGHT_PARENTHESIS;
    }

    private String buildTabIdForNewSourceLine() {
        return WebUtils.generateTabKey(CuFPConstants.ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_TITLE) +
                KFSConstants.DASH + KFSPropertyConstants.NEW_SOURCE_LINE;
    }

    private void setGeneralLedgerPendingEntriesTabOpenWhenGLPEsExist(AccountingDocument accountingDocument, Map<String, String> tabStates) {
        List<GeneralLedgerPendingEntry> generalLedgerPendingEntries = accountingDocument.getGeneralLedgerPendingEntries();

        if (ObjectUtils.isNotNull(generalLedgerPendingEntries) && generalLedgerPendingEntries.size() > 0) {
            String tabIdForGeneralLedgerPendingEntries = WebUtils.generateTabKey(CuFPConstants.GENERAL_LEDGER_PENDING_ENTRIES_TITLE);
            tabStates.put(tabIdForGeneralLedgerPendingEntries, CuFPConstants.OPEN);
        }
    }

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiAccountingDocumentFormBase tmpForm = (KualiAccountingDocumentFormBase) form;

        
        String docNum = tmpForm.getDocId();
       
        PreEncumbranceDocument preEncDoc = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(PreEncumbranceDocument.class, docNum);
        if (preEncDoc == null) {
            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(dot + "Save", 
                 KFSKeyConstants.ERROR_CUSTOM, "This Document needs to be saved before Submit");

        }

        ActionForward forward = super.route(mapping, form, request, response);

        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getSourceAccountingLines());
        checkSalesTaxRequiredAllLines(tmpForm, tmpForm.getFinancialDocument().getTargetAccountingLines());

        return forward;
    }

 
    protected void processAccountingLines(AccountingDocument transDoc, KualiAccountingDocumentFormBase transForm, String lineSet) {
        // figure out which set of lines we're looking at
        List formLines;
        String pathPrefix;
        boolean source;
        if (lineSet.equals(KFSConstants.SOURCE)) {
            formLines = transDoc.getSourceAccountingLines();
            pathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + dot + KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME;
            source = true;
        } else {
            formLines = transDoc.getTargetAccountingLines();
            pathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + dot + KFSConstants.EXISTING_TARGET_ACCT_LINE_PROPERTY_NAME;
            source = false;
        }

        // find and process corresponding form and baselines
        int index = 0;
        for (Iterator i = formLines.iterator(); i.hasNext(); index++) {
            AccountingLine formLine = (AccountingLine) i.next();
            if (formLine.isSourceAccountingLine()) {
                formLine.setReferenceNumber(transDoc.getDocumentNumber());
            }
            // update sales tax required attribute for view
            // handleSalesTaxRequired(transDoc, formLine, source, false, index);
            checkSalesTax(transDoc, formLine, source, false, index);
        }
    }

}
