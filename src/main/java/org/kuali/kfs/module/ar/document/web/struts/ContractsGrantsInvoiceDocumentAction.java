/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.DocumentStatusCategory;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.TransmissionDetailStatus;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.AccountingDocumentSaveWithNoLedgerEntryGenerationEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//CU customization: backport FINP-8642; this file can be removed when we upgrade to the 07/21/2022 version of financials
public class ContractsGrantsInvoiceDocumentAction extends CustomerInvoiceAction {

    protected static volatile ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService;
    protected static volatile FinancialSystemDocumentService financialSystemDocumentService;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final ContractsGrantsInvoiceDocumentForm cinvForm = (ContractsGrantsInvoiceDocumentForm) form;
        if (ObjectUtils.isNotNull(cinvForm.getContractsGrantsInvoiceDocument())) {
            final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument =
                    cinvForm.getContractsGrantsInvoiceDocument();
            if (getFinancialSystemDocumentService().getPendingDocumentStatuses().contains(
                    contractsGrantsInvoiceDocument.getDocumentHeader().getWorkflowDocumentStatusCode())) {
                getContractsGrantsInvoiceDocumentService().recalculateSourceAccountingLineTotals(
                        contractsGrantsInvoiceDocument);
            }
        }
        return super.execute(mapping, form, request, response);
    }

    /**
     * Overridden to recheck the suspension categories when the document is opened
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) {
        super.loadDocument(kualiDocumentFormBase);
        ContractsGrantsInvoiceDocumentForm cgInvoiceForm = (ContractsGrantsInvoiceDocumentForm) kualiDocumentFormBase;
        final ContractsGrantsInvoiceDocument cgInvoice = cgInvoiceForm.getContractsGrantsInvoiceDocument();
        ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        if (shouldUpdateSuspensionCategoriesAndRecalculateTotalAmountBilledToDate(cgInvoice)) {
            if (!ArConstants.BillingFrequencyValues.isMilestone(cgInvoice.getInvoiceGeneralDetail())
                    && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(cgInvoice.getInvoiceGeneralDetail())) {
                contractsGrantsInvoiceDocumentService.recalculateTotalAmountBilledToDate(cgInvoice);
            }
            updateSuspensionCategoriesOnDocument(cgInvoiceForm);
        } else {
            contractsGrantsInvoiceDocumentService.calculatePreviouslyBilledAmounts(cgInvoice);
        }
    }

    /**
     * Determines if the given c&g invoice should have its suspension categories updated and new total billed
     * recalculated or not
     *
     * @param cgInvoice the invoice to determine the suspension category updatability of
     * @return true if suspension categories should be updated and new total bill should be recalculated, false
     *         otherwise
     */
    protected boolean shouldUpdateSuspensionCategoriesAndRecalculateTotalAmountBilledToDate(
            ContractsGrantsInvoiceDocument cgInvoice) {
        final DocumentStatus documentStatus = DocumentStatus.fromCode(cgInvoice.getDocumentHeader()
                .getWorkflowDocumentStatusCode());
        return documentStatus.getCategory() != DocumentStatusCategory.SUCCESSFUL
                && documentStatus.getCategory() != DocumentStatusCategory.UNSUCCESSFUL
                && documentStatus != DocumentStatus.EXCEPTION;
    }

    /**
     * Recalculates the Total Expenditures in the Invoice Detail section and also the New Total Billed using the
     * Invoice Object Codes BO
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward recalculateTotalAmountBilledToDate(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument =
                contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();
        ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        contractsGrantsInvoiceDocumentService.recalculateTotalAmountBilledToDate(contractsGrantsInvoiceDocument);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Recalculates the Total Expenditures in the Invoice Detail section due to reaching limit of the total award.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward prorateBill(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument =
                contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();
        ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        contractsGrantsInvoiceDocumentService.prorateBill(contractsGrantsInvoiceDocument);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Sets the transmission date, [sent] by, and increments the times sent for the corresponding invoice transmission
     * detail to indicate the invoice was sent manually separately from the automated transmission process in the
     * system.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward markManuallySent(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm
                .getContractsGrantsInvoiceDocument();
        getContractsGrantsInvoiceDocumentService().markManuallySent(contractsGrantsInvoiceDocument);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Add invoice address detail into the current transmission queue
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward queueTransmission(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm
                .getContractsGrantsInvoiceDocument();
        getContractsGrantsInvoiceDocumentService().queueInvoiceTransmissions(contractsGrantsInvoiceDocument);

        GlobalVariables.getMessageMap().putInfo("document.invoiceAddressDetails",
                ArKeyConstants.ContractsGrantsInvoiceConstants.MESSAGE_CONTRACTS_GRANTS_INVOICE_TRANSMISSION_QUEUED);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Remove invoice address detail from the current transmission queue
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward unqueueTransmission(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        int index = getSelectedLine(request);

        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm
                .getContractsGrantsInvoiceDocument();

        InvoiceAddressDetail invoiceAddressDetail = contractsGrantsInvoiceDocument.getInvoiceAddressDetails()
                .get(index);
        if (invoiceAddressDetail.getInitialTransmissionDate() != null) {
            invoiceAddressDetail.setTransmissionStatusCode(TransmissionDetailStatus.Sent.getCode());
        } else {
            invoiceAddressDetail.setTransmissionStatusCode("");
        }
        SpringContext.getBean(BusinessObjectService.class).save(contractsGrantsInvoiceDocument);

        GlobalVariables.getMessageMap().putInfo("document.invoiceAddressDetails",
                ArKeyConstants.ContractsGrantsInvoiceConstants.MESSAGE_CONTRACTS_GRANTS_INVOICE_TRANSMISSION_UNQUEUED);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = updateSuspensionCategoriesOnDocument(form);

        ActionForward forward = promptForSuspensionCategories(mapping, form, request, response,
                contractsGrantsInvoiceDocument, KFSConstants.APPROVE_METHOD);
        if (forward != null) {
            return forward;
        }

        return super.approve(mapping, form, request, response);
    }

    /**
     * Save the document prior to canceling in case the amounts on the General Tab need to be recalculated.
     */
    @Override
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
        final String question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);

        if (StringUtils.equals(question, KRADConstants.DOCUMENT_CANCEL_QUESTION)
                && StringUtils.equals(buttonClicked, ConfirmationQuestion.YES)) {
            final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument =
                    updateSuspensionCategoriesOnDocument(form);
            contractsGrantsInvoiceDocument.clearAnyGeneralLedgerPendingEntries();
            SpringContext.getBean(DocumentService.class).saveDocument(contractsGrantsInvoiceDocument,
                    AccountingDocumentSaveWithNoLedgerEntryGenerationEvent.class);
        }

        return super.cancel(mapping, form, request, response);
    }

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = updateSuspensionCategoriesOnDocument(form);

        ActionForward forward = promptForSuspensionCategories(mapping, form, request, response,
                contractsGrantsInvoiceDocument, KFSConstants.ROUTE_METHOD);
        if (forward != null) {
            return forward;
        }

        return super.route(mapping, form, request, response);
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        updateSuspensionCategoriesOnDocument(form);

        return super.save(mapping, form, request, response);
    }

    /**
     * This method gets the ContractsGrantsInvoiceDocument from the form and updates the suspension categories
     * on the document in case circumstances have changed and the suspension categories should be different than when
     * the document was created.
     *
     * @param form
     * @return ContractsGrantsInvoiceDocument
     */
    protected ContractsGrantsInvoiceDocument updateSuspensionCategoriesOnDocument(ActionForm form) {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument =
                contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();

        ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        contractsGrantsInvoiceDocumentService.updateSuspensionCategoriesOnDocument(contractsGrantsInvoiceDocument);

        return contractsGrantsInvoiceDocument;
    }

    /**
     * This method checks if there are suspension categories on the Contracts & Grants Invoice document, and if there
     * are, prompts the user to make sure they want to continue. If yes, this method returns null. If no, this method
     * returns the "basic" forward.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param contractsGrantsInvoiceDocument
     * @param caller
     * @return
     * @throws Exception
     */
    protected ActionForward promptForSuspensionCategories(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response,
            ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument, String caller) throws Exception {
        ActionForward forward = null;

        if (contractsGrantsInvoiceDocument.getInvoiceSuspensionCategories().size() > 0) {
            Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (question == null) {
                String questionText = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                        ArKeyConstants.WARNING_SUSPENSION_CATEGORIES_PRESENT);
                return performQuestionWithoutInput(mapping, form, request, response,
                        ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION, questionText,
                        KFSConstants.CONFIRMATION_QUESTION, caller, StringUtils.EMPTY);
            }

            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if (ArConstants.SUSPENSION_CATEGORIES_PRESENT_QUESTION.equals(question)
                    && ConfirmationQuestion.NO.equals(buttonClicked)) {
                forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        return forward;
    }
    
    //CU customization: backport FINP-8642
    public ActionForward updateFinalBillIndicator(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm =
                (ContractsGrantsInvoiceDocumentForm) form;
        final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm
                .getContractsGrantsInvoiceDocument();
        if (contractsGrantsInvoiceDocument.getDocumentHeader().getWorkflowDocument().isApproved()) {
            getContractsGrantsInvoiceDocumentService().updateFinalBillIndicator(contractsGrantsInvoiceDocument);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public static ContractsGrantsInvoiceDocumentService getContractsGrantsInvoiceDocumentService() {
        if (contractsGrantsInvoiceDocumentService == null) {
            contractsGrantsInvoiceDocumentService = SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        }
        return contractsGrantsInvoiceDocumentService;
    }

    public static FinancialSystemDocumentService getFinancialSystemDocumentService() {
        if (financialSystemDocumentService == null) {
            financialSystemDocumentService = SpringContext.getBean(FinancialSystemDocumentService.class);
        }
        return financialSystemDocumentService;
    }
}
