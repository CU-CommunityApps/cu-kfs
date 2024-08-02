/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar.document.web.struts;

import edu.cornell.kfs.module.ar.CuArKeyConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.exception.UnknownDocumentIdException;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.SessionDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.document.CashControlDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.CashControlDocumentService;
import org.kuali.kfs.module.ar.document.validation.event.AddCashControlDetailEvent;
import org.kuali.kfs.module.ar.service.CashControlDetailImportService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.sys.service.BankService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class CashControlAction extends FinancialSystemTransactionalDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger();
    private CashControlDetailImportService cashControlDetailImportService;
    private CashControlDocumentService cashControlDocumentService;

    @Override
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        final CashControlForm ccForm = (CashControlForm) kualiDocumentFormBase;
        final CashControlDocument cashControlDocument = ccForm.getCashControlDocument();

        // now that the form has been originally loaded, we need to set a few Form variables that are used by
        // JSP JSTL expressions because they are used directly and immediately upon initial form display
        if (cashControlDocument != null && cashControlDocument.getCustomerPaymentMediumCode() != null) {
            ccForm.setCashPaymentMediumSelected(ArConstants.PaymentMediumCode.CASH
                    .equalsIgnoreCase(cashControlDocument.getCustomerPaymentMediumCode()));
        }

        if (cashControlDocument != null) {
            // get the PaymentApplicationDocuments by reference number
            for (final CashControlDetail cashControlDetail : cashControlDocument.getCashControlDetails()) {
                final String docId = cashControlDetail.getReferenceFinancialDocumentNumber();
                final PaymentApplicationDocument doc = (PaymentApplicationDocument) SpringContext.getBean(
                        DocumentService.class).getByDocumentHeaderId(docId);
                if (doc == null) {
                    throw new UnknownDocumentIdException("Document " + docId
                            + " no longer exists.  It may have been cancelled before being saved.");
                }

                cashControlDetail.setReferenceFinancialDocument(doc);
                final WorkflowDocument workflowDoc = doc.getDocumentHeader().getWorkflowDocument();
                // KualiDocumentFormBase.populate() needs this updated in the session
                SpringContext.getBean(SessionDocumentService.class)
                        .addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDoc);
            }
            cashControlDocument.recalculateTotals();
        }
    }

    /**
     * Adds handling for cash control detail amount updates.
     */
    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CashControlForm ccForm = (CashControlForm) form;
        CashControlDocument ccDoc = ccForm.getCashControlDocument();

        if (ccDoc != null) {
            ccForm.setCashPaymentMediumSelected(ArConstants.PaymentMediumCode.CASH.equalsIgnoreCase(
                    ccDoc.getCustomerPaymentMediumCode()));
        }

        if (ccForm.hasDocumentId()) {
            ccDoc = ccForm.getCashControlDocument();
            ccDoc.refreshReferenceObject("customerPaymentMedium");
            // recalc b/c changes to the amounts could have happened
            ccDoc.recalculateTotals();
        }

        return super.execute(mapping, form, request, response);
    }

    @Override
    protected void createDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        super.createDocument(kualiDocumentFormBase);
        final CashControlForm form = (CashControlForm) kualiDocumentFormBase;
        final CashControlDocument document = form.getCashControlDocument();

        //get the default bank code for the given document type, which is CTRL for this document.

        document.setBankCode("");
        final Bank defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(form.getDocTypeName());
        if (defaultBank != null) {
            document.setBankCode(defaultBank.getBankCode());
        }

        // set up the default values for the AR DOC Header (SHOULD PROBABLY MAKE THIS A SERVICE)
        final AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService =
                SpringContext.getBean(AccountsReceivableDocumentHeaderService.class);
        final AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = accountsReceivableDocumentHeaderService
                .getNewAccountsReceivableDocumentHeaderForCurrentUser();
        accountsReceivableDocumentHeader.setDocumentNumber(document.getDocumentNumber());
        document.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);

    }

    @Override
    public ActionForward cancel(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CashControlForm cashControlDocForm = (CashControlForm) form;
        final CashControlDocument cashControlDocument = cashControlDocForm.getCashControlDocument();

        if (hasUserConfirmedCancel(request)) {
            // If the cancel works, proceed to canceling the cash control doc, otherwise return to the cash control doc
            // displaying the error.
            if (cancelLinkedPaymentApplicationDocuments(cashControlDocument)) {
                // the cash control details may have been saved when canceling Pay App docs, so we need to refresh this
                // collection to avoid an OptimisticLockException
                cashControlDocument.refreshReferenceObject(KFSPropertyConstants.CASH_CONTROL_DETAILS);
                cashControlDocument.recalculateTotals();
                getBusinessObjectService().save(cashControlDocument);
            } else {
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        return super.cancel(mapping, form, request, response);
    }

    private boolean hasUserConfirmedCancel(final HttpServletRequest request) {
        final String buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
        final String question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);

        return StringUtils.equals(question, KRADConstants.DOCUMENT_CANCEL_QUESTION)
                && StringUtils.equals(buttonClicked, ConfirmationQuestion.YES);
    }

    @Override
    public ActionForward disapprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CashControlForm cashControlDocForm = (CashControlForm) form;
        final CashControlDocument cashControlDocument = cashControlDocForm.getCashControlDocument();

        final boolean success = cancelLinkedPaymentApplicationDocuments(cashControlDocument);
        if (!success) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        return super.disapprove(mapping, form, request, response);
    }

    /**
     * This method cancels all linked Payment Application documents that are not already in approved, canceled or
     * disapproved status.
     *
     * @param cashControlDocument cash control document used to find related payment application documents to cancel
     */
    protected boolean cancelLinkedPaymentApplicationDocuments(final CashControlDocument cashControlDocument) {
        boolean success = true;
        final DocumentService documentService = SpringContext.getBean(DocumentService.class);
        final List<CashControlDetail> details = cashControlDocument.getCashControlDetails();
        for (final CashControlDetail cashControlDetail : details) {
            final PaymentApplicationDocument applicationDocument = (PaymentApplicationDocument) documentService
                    .getByDocumentHeaderId(cashControlDetail.getReferenceFinancialDocumentNumber());
            final String financialDocumentStatusCode = applicationDocument.getDocumentHeader()
                    .getFinancialDocumentStatusCode();

            if (KFSConstants.DocumentStatusCodes.APPROVED.equals(financialDocumentStatusCode)) {
                GlobalVariables.getMessageMap().putError(
                        ArPropertyConstants.CashControlDetailFields.CASH_CONTROL_DETAILS_TAB,
                        ArKeyConstants.ERROR_CANT_CANCEL_CASH_CONTROL_DOC_WITH_ASSOCIATED_APPROVED_PAYMENT_APPLICATION);
                success = false;
            } else {
                cancelLinkedPaymentApplicationDocument(cashControlDocument, cashControlDetail);
            }
        }
        return success;
    }

    /**
     * This method adds a new cash control detail
     *
     * @param mapping  action mapping
     * @param form     action form
     * @param request
     * @param response
     * @return forward action
     * @throws Exception
     */
    public ActionForward addCashControlDetail(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CashControlForm cashControlDocForm = (CashControlForm) form;
        final CashControlDocument cashControlDocument = cashControlDocForm.getCashControlDocument();
        final ConfigurationService kualiConfiguration = SpringContext.getBean(ConfigurationService.class);

        final CashControlDetail newCashControlDetail = cashControlDocForm.getNewCashControlDetail();
        newCashControlDetail.setDocumentNumber(cashControlDocument.getDocumentNumber());

        String customerNumber = newCashControlDetail.getCustomerNumber();
        if (StringUtils.isNotEmpty(customerNumber)) {
            // force customer numbers to upper case, since its a primary key
            customerNumber = customerNumber.toUpperCase(Locale.US);
        }
        newCashControlDetail.setCustomerNumber(customerNumber);

        // save the document, which will run business rules and make sure the doc is ready for lines
        final KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        // apply save rules for the doc
        boolean rulePassed = ruleService.applyRules(new SaveDocumentEvent(KFSConstants.DOCUMENT_HEADER_ERRORS,
                cashControlDocument));

        // apply rules for the new cash control detail
        rulePassed &= ruleService
                .applyRules(new AddCashControlDetailEvent(ArConstants.NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX,
                        cashControlDocument, newCashControlDetail));

        // add the new detail if rules passed
        if (rulePassed) {
            final CashControlDocumentService cashControlDocumentService =
                    SpringContext.getBean(CashControlDocumentService.class);

            // add cash control detail. implicitly saves the cash control document
            cashControlDocumentService.addNewCashControlDetail(kualiConfiguration.getPropertyValueAsString(
                    ArKeyConstants.CREATED_BY_CASH_CTRL_DOC), cashControlDocument, newCashControlDetail);

            // set a new blank cash control detail
            cashControlDocForm.setNewCashControlDetail(new CashControlDetail());
        }

        // recalc totals, including the docHeader total
        cashControlDocument.recalculateTotals();

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method cancels a cash control detail
     *
     * @param mapping  action mapping
     * @param form     action form
     * @param request
     * @param response
     * @return action forward
     * @throws Exception
     */
    public ActionForward cancelCashControlDetail(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final CashControlForm cashControlDocForm = (CashControlForm) form;
        final CashControlDocument cashControlDocument = cashControlDocForm.getCashControlDocument();

        final int indexOfLineToDelete = getLineToDelete(request);
        final CashControlDetail cashControlDetail = cashControlDocument.getCashControlDetail(indexOfLineToDelete);
        cancelLinkedPaymentApplicationDocument(cashControlDocument, cashControlDetail);

        // load document so the displayed total amount is accurate and pay app is displayed as canceled
        loadDocument(cashControlDocForm);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Recalculates the cash control total since user could have changed it during their update.
     *
     * @param cashControlDocument
     */
    protected KualiDecimal calculateCashControlTotal(final CashControlDocument cashControlDocument) {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final CashControlDetail cashControlDetail : cashControlDocument.getCashControlDetails()) {
            total = total.add(cashControlDetail.getFinancialDocumentLineAmount());
        }
        return total;
    }

    /**
     * Processes a PaymentApplicationDocument linked to a given cash control to be deleted. Documents will only be
     * processed if they are not already cancelled or disapproved. Enroute docs are disapproved, all others are cancelled.
     * The provided cash control detail will also be cancelled and its amount will be removed from the cash control
     * document's total amount
     *
     * @param cashControlDocument The cash control document that the provided cash control detail belongs to
     * @param cashControlDetail The cash control detail to be canceled
     */
    private void cancelLinkedPaymentApplicationDocument(
            final CashControlDocument cashControlDocument,
            final CashControlDetail cashControlDetail
    ) {
        final DocumentService documentService = SpringContext.getBean(DocumentService.class);
        final PaymentApplicationDocument payAppDoc = (PaymentApplicationDocument) documentService
                .getByDocumentHeaderId(cashControlDetail.getReferenceFinancialDocumentNumber());
        final WorkflowDocument workflowDocument = payAppDoc.getDocumentHeader().getWorkflowDocument();

        if (!workflowDocument.isCanceled() && !workflowDocument.isDisapproved()) {
            if (workflowDocument.isEnroute()) {
                documentService.superUserDisapproveDocument(payAppDoc, ArKeyConstants.DOCUMENT_DELETED_FROM_CASH_CTRL_DOC);
            } else {
                documentService.cancelDocument(payAppDoc, ArKeyConstants.DOCUMENT_DELETED_FROM_CASH_CTRL_DOC);
            }
        }
        cashControlDocument.recalculateTotals();
    }

    /**
     * Import items to the document from a spreadsheet.
     *
     * @param mapping  An ActionMapping
     * @param form     An ActionForm
     * @param request  The HttpServletRequest
     * @param response The HttpServletResponse
     * @return An ActionForward
     */
    public ActionForward importDetails(
            final ActionMapping mapping,
            final ActionForm form,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        LOG.info("Importing detail lines");

        final CashControlForm cashControlForm = (CashControlForm) form;
        final CashControlDocument cashControlDocument = (CashControlDocument) cashControlForm.getDocument();
        final FormFile detailImportFile = cashControlForm.getDetailImportFile();

        if (checkCashControlDetailFile(detailImportFile)) {
            try {
                final InputStream detailImportFileInputStream = detailImportFile.getInputStream();
                getCashControlDetailImportService().processDetailImportFile(detailImportFile.getFileName(),
                        detailImportFileInputStream,
                        cashControlDocument
                );
            } catch (final IOException e) {
                LOG.error("importDetails() - unable to open import file", e);
                GlobalVariables.getMessageMap().putError(
                        ArConstants.NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX,
                        KFSKeyConstants.ERROR_UPLOADFILE_NULL
                );
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Checks whether the specified cashControlDetail import file is not null and of a valid format; report
     * errors if conditions not satisfied.
     *
     * @param detailImportFile the specified cashControlDetail import file
     * @return true if import file is valid, false otherwise
     */
    private static boolean checkCashControlDetailFile(final FormFile detailImportFile) {
        if (detailImportFile == null) {
            GlobalVariables.getMessageMap().putError(
                    ArConstants.NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX,
                    KFSKeyConstants.ERROR_UPLOADFILE_NULL
            );
            return false;
        }

        final String fileName = detailImportFile.getFileName();
        if (StringUtils.isNotBlank(fileName) && !StringUtils.lowerCase(fileName).endsWith(".csv")
                && !StringUtils.lowerCase(fileName).endsWith(".xls")) {
            GlobalVariables.getMessageMap().putError(
                    ArConstants.NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX,
                    CuArKeyConstants.CashControlDetailConstants.ERROR_DETAILPARSER_INVALID_FILE_FORMAT,
                    fileName
            );
            return false;
        }

        return true;
    }

    private CashControlDetailImportService getCashControlDetailImportService() {
        if (cashControlDetailImportService == null) {
            cashControlDetailImportService = SpringContext.getBean(CashControlDetailImportService.class);
        }
        return cashControlDetailImportService;
    }

    private CashControlDocumentService getCashControlDocumentService() {
        if (cashControlDocumentService == null) {
            cashControlDocumentService = SpringContext.getBean(CashControlDocumentService.class);
        }
        return cashControlDocumentService;
    }

}