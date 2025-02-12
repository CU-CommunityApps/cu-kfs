package edu.cornell.kfs.module.ar.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.kfs.module.ar.CuArConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.collections4.CollectionUtils;
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
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

public class CuContractsGrantsInvoiceDocumentAction extends ContractsGrantsInvoiceDocumentAction {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentAction.class);

    @Override
    public ActionForward prorateBill(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm = (ContractsGrantsInvoiceDocumentForm) form;
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
    public ActionForward approve(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward forward = validateBillingPeriod(mapping, form, request, response);
        return forward != null ? forward : super.approve(mapping, form, request, response);
    }

    @Override
    public ActionForward save(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward forward = validateBillingPeriod(mapping, form, request, response);
        return forward != null ? forward : super.save(mapping, form, request, response);
    }

    @Override
    public ActionForward route(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward forward = validateBillingPeriod(mapping, form, request, response);
        return forward != null ? forward : super.route(mapping, form, request, response);
    }

    protected ActionForward validateBillingPeriod(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = ((ContractsGrantsInvoiceDocumentForm) form).getContractsGrantsInvoiceDocument();
        ActionForward forward = null;

        String warningMessage = getContractsGrantsInvoiceDocumentWarningMessage(contractsGrantsInvoiceDocument);
        if (StringUtils.isNotEmpty(warningMessage)) {
            forward = promptForFinalBillConfirmation(mapping, form, request, response, KFSConstants.ROUTE_METHOD, warningMessage, contractsGrantsInvoiceDocument);
        }
        return forward;
    }

    protected String getContractsGrantsInvoiceDocumentWarningMessage(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (contractsGrantsInvoiceDocument.isCorrectionDocument()) {
            return StringUtils.EMPTY;
        }

        List<String> warningMessages = new ArrayList<>();
        ConfigurationService configurationService = SpringContext.getBean(ConfigurationService.class);
        if (contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().isFinalBillIndicator()) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_FINAL_BILL_INDICATOR));
        }

        String billingPeriod = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingPeriod();
        if (StringUtils.length(billingPeriod) != CuArConstants.CINV_DATE_RANGE_EXPECTED_FORMAT_LENGTH) {
            billingPeriod = StringUtils.defaultString(billingPeriod);
            String warningMessage = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_DATE_RANGE_INVALID_FORMAT_LENGTH);
            warningMessages.add(MessageFormat.format(warningMessage, new String[] {billingPeriod}));
        } else if (shouldValidateBillingPeriodDateRange(contractsGrantsInvoiceDocument)) {
            try {
                validateBillingPeriodDateRange(contractsGrantsInvoiceDocument, warningMessages);
            } catch (ParseException ex) {
                LOG.error("getContractsGrantsInvoiceDocumentWarningMessage: " + ex.getMessage());
                String warningMessage = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_DATE_RANGE_PARSE_EXCEPTION);
                warningMessages.add(MessageFormat.format(warningMessage, new String[] {billingPeriod}));
            }
        }

        return StringUtils.join(warningMessages, CuArConstants.QUESTION_NEWLINE_STRING);
    }

    protected void validateBillingPeriodDateRange(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, List<String> warningMessages) throws ParseException {
        Pair<Date, Date> billingPeriodDates = parseDateRange(contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingPeriod());
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
        ConfigurationService configurationService = SpringContext.getBean(ConfigurationService.class);

        Date billingPeriodStartDate = billingPeriodDates.getLeft();
        Date billingPeriodEndDate = billingPeriodDates.getRight();
        if (dateTimeService.dateDiff(billingPeriodEndDate, billingPeriodStartDate, false) >= 1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_END_DATE_BEFORE_BILLING_PERIOD_START_DATE));
        }
        if (dateTimeService.dateDiff(dateTimeService.getCurrentSqlDate(), billingPeriodEndDate, false) >= 1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_END_DATE_AFTER_TODAY));
        }

        Date lastBilledDate = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getLastBilledDate();
        if (dateTimeService.dateDiff(billingPeriodEndDate, lastBilledDate, false) >= 1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_END_DATE_BEFORE_LAST_BILLED_DATE));
        }
        if (dateTimeService.dateDiff(billingPeriodEndDate, lastBilledDate, false) <= -1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_END_DATE_AFTER_LAST_BILLED_DATE));
        }
        if (dateTimeService.dateDiff(billingPeriodStartDate, lastBilledDate, false) <= -1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_START_DATE_AFTER_LAST_BILLED_DATE));
        }

        Pair<Date, Date> awardDateRange = parseDateRange(contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAwardDateRange());
        if (dateTimeService.dateDiff(billingPeriodStartDate, awardDateRange.getLeft(), false) >= 1) {
            warningMessages.add(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_BILLING_PERIOD_START_DATE_BEFORE_AWARD_START_DATE));
        }
    }
    
    private boolean shouldValidateBillingPeriodDateRange(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        String frequencyCode = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingFrequencyCode();
        if (StringUtils.equalsIgnoreCase(frequencyCode, ArConstants.BillingFrequencyValues.LETTER_OF_CREDIT.getCode()) ||
                StringUtils.equalsIgnoreCase(frequencyCode, ArConstants.BillingFrequencyValues.PREDETERMINED_BILLING.getCode()) ||
                StringUtils.equalsIgnoreCase(frequencyCode, ArConstants.BillingFrequencyValues.MILESTONE.getCode())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("shouldValidateBillingPeriodDateRange, the billing frequency '" + frequencyCode + "' should NOT require validation of billing period date range ");
            }
            return false;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("shouldValidateBillingPeriodDateRange, the billing frequency '" + frequencyCode + "' should require validation of billing period date range ");
            }
            return true;
        }
    }

    protected ActionForward promptForFinalBillConfirmation(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String caller, String warningMessage, ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) throws Exception {
        ActionForward forward = null;

        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        if (question == null) {
            return performQuestionWithoutInput(mapping, form, request, response, CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION, warningMessage, KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
        }

        Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
        if (CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION.equals(question)) {
            if (ConfirmationQuestion.NO.equals(buttonClicked)) {
                forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
            } else if (ConfirmationQuestion.YES.equals(buttonClicked)) {
                try {
                    Pair<Date, Date> billingPeriod = parseDateRange(contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getBillingPeriod());
                    Date billingPeriodEndDate = billingPeriod.getRight();
                    DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
                    if (dateTimeService.dateDiff(billingPeriodEndDate, contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getLastBilledDate(), false) != 0) {
                        contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().setLastBilledDate(billingPeriodEndDate);
                    }
                } catch (Exception ex) {
                    ConfigurationService configurationService = SpringContext.getBean(ConfigurationService.class);
                    String message = configurationService.getPropertyValueAsString(CUKFSKeyConstants.ERROR_CINV_SETTING_LAST_BILLED_DATE);
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM, message);
                    return mapping.findForward(KFSConstants.MAPPING_BASIC);
                }
            }
        }

        return forward;
    }

    @Override
    protected ActionForward promptForSuspensionCategories(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response,
            final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, final String caller) throws Exception {
        ActionForward forward = null;

        if (CollectionUtils.size(contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories()) > 0) {
            final Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (question == null || question.toString().equals(CuArConstants.CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION)) {
                final String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                        ArKeyConstants.WARNING_SUSPENSION_CATEGORIES_PRESENT);
                return performQuestionWithoutInput(mapping, form, request, response, ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
            }

            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION.equals(question) && ConfirmationQuestion.NO.equals(buttonClicked)) {
                forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        return forward;
    }


    private KualiDecimal findAwardBudgetTotal(final ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm) {
        final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();
        final Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        final AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        KualiDecimal budgetTotalAmount = null;
        if (ObjectUtils.isNotNull(awardExtension)) {
            budgetTotalAmount = awardExtension.getBudgetTotalAmount();
        } else {
            LOG.error("findAwardBudgetTotal, there is no award extension object on award " + award.getProposalNumber());
        }
        
        return budgetTotalAmount;
    }

    private Pair<Date, Date> parseDateRange(String dateRange) throws ParseException {
        if (StringUtils.length(dateRange) != CuArConstants.CINV_DATE_RANGE_EXPECTED_FORMAT_LENGTH) {
            dateRange = StringUtils.defaultString(dateRange);
            final String errorMessage = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(CUKFSKeyConstants.WARNING_CINV_DATE_RANGE_INVALID_FORMAT_LENGTH);
            throw new ParseException(MessageFormat.format(errorMessage, new String[] {dateRange}), 0);
        }

        final DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
        final Date startDate = dateTimeService.convertToSqlDate(dateRange.substring(CuArConstants.CINV_DATE_RANGE_START_DATE_START_INDEX, CuArConstants.CINV_DATE_RANGE_START_DATE_END_INDEX));
        final Date endDate = dateTimeService.convertToSqlDate(dateRange.substring(CuArConstants.CINV_DATE_RANGE_END_DATE_START_INDEX));
        return Pair.of(startDate, endDate);
    }

}
