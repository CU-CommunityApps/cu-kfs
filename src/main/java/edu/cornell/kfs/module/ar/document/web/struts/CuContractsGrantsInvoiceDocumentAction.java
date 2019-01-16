package edu.cornell.kfs.module.ar.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.module.ar.CuArConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentAction;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentForm;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

import java.util.ArrayList;
import java.util.List;

public class CuContractsGrantsInvoiceDocumentAction extends ContractsGrantsInvoiceDocumentAction {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentAction.class);

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
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = ((ContractsGrantsInvoiceDocumentForm) form).getContractsGrantsInvoiceDocument();

        String warningMessage = getContractsGrantsInvoiceDocumentWarningMessage(contractsGrantsInvoiceDocument);
        if (StringUtils.isNotEmpty(warningMessage)) {
            ActionForward forward = promptForFinalBillConfirmation(mapping, form, request, response, KFSConstants.ROUTE_METHOD, warningMessage);
            if (forward != null) {
                return forward;
            }
        }

        return super.route(mapping, form, request, response);
    }

    protected String getContractsGrantsInvoiceDocumentWarningMessage(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        List<String> warningMessages = new ArrayList<String>();
        if (contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().isFinalBillIndicator()) {
            warningMessages.add(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_FINAL_BILL_INDICATOR));
        }

        String billingPeriod = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingPeriod();
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
        String billingPeriodEndDateRaw = billingPeriod.substring(14);
        String billingPeriodStartDateRaw = billingPeriod.substring(0, 10);
        try {
            java.util.Date billingPeriodEndDate = dateTimeService.convertToDate(billingPeriodEndDateRaw);
            java.util.Date billingPeriodStartDate = dateTimeService.convertToDate(billingPeriodStartDateRaw);

            if (dateTimeService.dateDiff(billingPeriodEndDate, contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getLastBilledDate(), true) >= 1) {
                warningMessages.add(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_END_DATE_AFTER_LAST_BILLED_DATE));
            }
            if (dateTimeService.dateDiff(billingPeriodStartDate, contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getLastBilledDate(), true) <= -1) {
                warningMessages.add(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_START_DATE_BEFORE_LAST_BILLED_DATE));
            }

        } catch (java.text.ParseException ex) {
            warningMessages.add("ParseException occurred while parsing the billing period. Do you want to Proceed?\n\n" + ex.toString());
        }

        return CollectionUtils.isEmpty(warningMessages) ? null : StringUtils.join(warningMessages, ", ");
    }

    protected ActionForward promptForFinalBillConfirmation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String caller, String warningMessage) throws Exception {
        ActionForward forward = null;

        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        if (question == null) {
            return performQuestionWithoutInput(mapping, form, request, response, CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION, warningMessage, KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
        }

        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
        if (CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION.equals(question) && ConfirmationQuestion.NO.equals(buttonClicked)) {
            forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        return forward;
    }

    @Override
    protected ActionForward promptForSuspensionCategories(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, String caller) throws Exception {
        ActionForward forward = null;

        if (contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().size() > 0) {
            Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (question == null || question.toString().equals(CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION)) {
                String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(ArKeyConstants.WARNING_SUSPENSION_CATEGORIES_PRESENT);
                return performQuestionWithoutInput(mapping, form, request, response, ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
            }

            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION.equals(question) && ConfirmationQuestion.NO.equals(buttonClicked)) {
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
