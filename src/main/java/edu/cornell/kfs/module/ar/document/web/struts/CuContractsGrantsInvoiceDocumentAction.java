package edu.cornell.kfs.module.ar.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.sys.CUKFSConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentAction;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentForm;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AccountingDocumentSaveWithNoLedgerEntryGenerationEvent;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuContractsGrantsInvoiceDocumentAction extends ContractsGrantsInvoiceDocumentAction {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentAction.class);

    public ActionForward finalizeBill(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
        String question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);

        if (StringUtils.equals(question, KRADConstants.DOCUMENT_CANCEL_QUESTION) && StringUtils.equals(buttonClicked, ConfirmationQuestion.YES)) {
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = updateSuspensionCategoriesOnDocument(form);
            contractsGrantsInvoiceDocument.clearAnyGeneralLedgerPendingEntries();
            SpringContext.getBean(DocumentService.class).saveDocument(contractsGrantsInvoiceDocument, AccountingDocumentSaveWithNoLedgerEntryGenerationEvent.class);
        }

        return super.cancel(mapping, form, request, response);
    }

    @Override
    public ActionForward prorateBill(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm = (ContractsGrantsInvoiceDocumentForm) form;
        KualiDecimal budgetTotalAmount = findAwardBudgetTotal(contractsGrantsInvoiceDocumentForm);
        
        if (budgetTotalAmount == null || budgetTotalAmount.isLessEqual(KualiDecimal.ZERO)) {
            String budgetTotalAmountString = budgetTotalAmount != null ? budgetTotalAmount.toString() : "0"; 
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, 
                    CUKFSKeyConstants.ERROR_DOCUMENT_CONTRACT_GRANT_INVOICE_PRORATE_NO_AWARD_BUDGET_TOTAL, budgetTotalAmountString);
            
            LOG.error("prorateBill, Prorate is not valid as the budgetTotalAmount is " + budgetTotalAmount);

            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        return super.prorateBill(mapping, form, request, response);
    }

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = updateSuspensionCategoriesOnDocument(form);

        ActionForward forward = promptForSuspensionCategories(mapping, form, request, response, contractsGrantsInvoiceDocument, KFSConstants.ROUTE_METHOD);
        if (forward != null) {
            return forward;
        }

        if (contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().isFinalBillIndicator()) {
            //todo only approve and when final bill indicator was unchecked before
            forward = promptForFinalBillConfirmation(mapping, form, request, response, contractsGrantsInvoiceDocument, KFSConstants.ROUTE_METHOD);
            if (forward != null) {
                return forward;
            }
        }

        return super.route(mapping, form, request, response);
    }

    protected ActionForward promptForFinalBillConfirmation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, String caller) throws Exception {
        ActionForward forward = null;

        if (contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().size() > 0) {
            Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (question == null) {
                String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_FINAL_BILL_INDICATOR);
                return performQuestionWithoutInput(mapping, form, request, response, CUKFSConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
            }

            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (CUKFSConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION.equals(question) && ConfirmationQuestion.NO.equals(buttonClicked)) {
                forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        return forward;
    }


    private KualiDecimal findAwardBudgetTotal(ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm) {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();
        Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        KualiDecimal budgetTotalAmount = null;
        if (ObjectUtils.isNotNull(awardExtension)) {
            budgetTotalAmount = awardExtension.getBudgetTotalAmount();
        } else {
            LOG.error("findAwardBudgetTotal, there is no award extension object on award " + award.getProposalNumber());
        }
        
        return budgetTotalAmount;
    }

}
