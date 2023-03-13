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
package org.kuali.kfs.module.ar.document.authorization;


import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArAuthorizationConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArParameterConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.sys.service.UniversityDateService;

import java.util.Date;
import java.util.Set;

/**
 * Contracts & Grants Invoice Document Presentation Controller class.
 */
//CU customization: backport FINP-8555; this file can be removed when we upgrade to the 07/14/2022 version of financials
public class ContractsGrantsInvoiceDocumentPresentationController extends
        CustomerInvoiceDocumentPresentationController {

    private static final String FUNDS_MANAGER_ROUTE_NODE = "FundsManager";

    protected UniversityDateService universityDateService;
    private FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService;

    /**
     * @see org.kuali.kfs.module.ar.document.authorization.ContractsGrantsInvoiceDocumentPresentationController#canErrorCorrect(org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument)
     */
    @Override
    public boolean canErrorCorrect(FinancialSystemTransactionalDocument document) {
        DocumentHeader documentHeader = document.getDocumentHeader();
        boolean invoiceReversal = ((ContractsGrantsInvoiceDocument) document).isInvoiceReversal();

        DateTime dateApproved = null;
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
        if (ObjectUtils.isNotNull(workflowDocument)) {
            dateApproved = workflowDocument.getDateApproved();
        }

        return canErrorCorrect((ContractsGrantsInvoiceDocument) document, documentHeader,
                invoiceReversal, dateApproved);
    }

    protected boolean canErrorCorrect(ContractsGrantsInvoiceDocument document,
                                      DocumentHeader documentHeader,
                                      boolean invoiceReversal,
                                      DateTime dateApproved) {
        if (hasBeenCorrected(documentHeader)) {
            return false;
        }

        if (invoiceReversal) {
            return false;
        }

        if (ObjectUtils.isNotNull(dateApproved) && dateApproved.isBefore(getStartOfCurrentFiscalYear().toInstant())) {
            return false;
        }

        return isDocFinalWithNoAppliedAmountsExceptDiscounts(document);
    }

    private boolean hasBeenCorrected(DocumentHeader documentHeader) {
        return StringUtils.isNotBlank(documentHeader.getCorrectedByDocumentId());
    }

    protected DateTime getStartOfCurrentFiscalYear() {
        final Date today = new DateTime().toDate();
        final Integer fiscalYear = getUniversityDateService().getFiscalYear(today);
        final Date firstDateOfFiscalYear = getUniversityDateService().getFirstDateOfFiscalYear(fiscalYear);

        return new DateTime(firstDateOfFiscalYear);
    }

    /**
     * CINVs created by the Letter of Credit process should be forever read only
     *
     * @see org.kuali.kfs.krad.document.DocumentPresentationControllerBase#canEdit(org.kuali.kfs.krad.document.Document)
     */
    @Override
    public boolean canEdit(Document document) {
        final boolean canEdit = super.canEdit(document);
        if (canEdit) {
            final ContractsGrantsInvoiceDocument contractsGrantsInvoice = (ContractsGrantsInvoiceDocument) document;
            if (ArConstants.BillingFrequencyValues.isLetterOfCredit(contractsGrantsInvoice.getInvoiceGeneralDetail().getAward())) {
                return false;
            }
        }

        return canEdit;
    }

    /**
     * Overridden so the Fund Manager can edit doc overview fields prior to approval.
     */
    @Override
    //CU customization: backport FINP-8555
    public boolean canEditDocumentOverview(final Document document) {
        if (fundManagerApproving(document)) {
            return true;
        }

        return super.canEditDocumentOverview(document);
    }
    
    //CU customization: backport FINP-8555
    private boolean fundManagerApproving(final Document document) {    
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        final Set<String> currentNodeNames = workflowDocument.getCurrentNodeNames();
        final boolean isAtFundsManagerNode = currentNodeNames.contains(FUNDS_MANAGER_ROUTE_NODE);

        if (isAtFundsManagerNode
            && workflowDocument.isApprovalRequested()
            && !isAdhocApprovalRequestedForPrincipal(workflowDocument)
            && !((ContractsGrantsInvoiceDocument) document).isCorrectionDocument()
        ) {
            return true;
        }

        return false;
    }

    private boolean isAdhocApprovalRequestedForPrincipal(final WorkflowDocument workflowDocument) {
        return getFinancialSystemWorkflowHelperService().isAdhocApprovalRequestedForPrincipal(
                workflowDocument,
                GlobalVariables.getUserSession().getPrincipalId()
        );
    }

    public boolean canProrate(ContractsGrantsInvoiceDocument document) {
        return canEdit(document)
                && getParameterService().getParameterValueAsBoolean(
                        ArConstants.AR_NAMESPACE_CODE, ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, ArParameterConstants.PRORATE_BILL_IND)
                && !ArConstants.BillingFrequencyValues.isMilestone(document.getInvoiceGeneralDetail())
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(document.getInvoiceGeneralDetail());
    }

    public boolean canModifyTransmissionDate(ContractsGrantsInvoiceDocument document) {
        if (document.hasInvoiceBeenCorrected()) {
            return false;
        }

        if (document.isInvoiceReversal()) {
            return false;
        }

        if (ArConstants.BillingFrequencyValues.isLetterOfCredit(document.getInvoiceGeneralDetail())) {
            return false;
        }

        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return ObjectUtils.isNotNull(workflowDocument) && (workflowDocument.isProcessed() || workflowDocument.isFinal());
    }

    @Override
    //CU customization: FINP-8555
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);

        final ContractsGrantsInvoiceDocument cinv = (ContractsGrantsInvoiceDocument) document;
        if (canModifyTransmissionDate(cinv)) {
            editModes.add(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.MODIFY_TRANSMISSION_DATE);
        }
        
        // For LOC docs, even though they are normally readonly, if the Funds Manager is approving the doc, the final
        // bill indicator should be editable
        if (canEdit(document) || fundManagerApproving(document)) {
            editModes.add(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.EDIT_FINAL_BILL_INDICATOR);
        }

        final WorkflowDocument workflowDocument = cinv.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isApproved() && !cinv.isInvoiceReversal()) {
            editModes.add(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.UPDATE_FINAL_BILL_INDICATOR);
        }

        return editModes;
    }

    @Override
    public Set<String> getDocumentActions(Document document) {
        Set<String> documentActions = super.getDocumentActions(document);
        documentActions.remove(KRADConstants.KUALI_ACTION_CAN_COPY);
        return documentActions;
    }

    @Override
    public UniversityDateService getUniversityDateService() {
        if (universityDateService == null) {
            universityDateService = SpringContext.getBean(UniversityDateService.class);
        }
        return universityDateService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public FinancialSystemWorkflowHelperService getFinancialSystemWorkflowHelperService() {
        if (financialSystemWorkflowHelperService == null) {
            financialSystemWorkflowHelperService = SpringContext.getBean(FinancialSystemWorkflowHelperService.class);
        }
        return financialSystemWorkflowHelperService;
    }

    public void setFinancialSystemWorkflowHelperService(
            final FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService) {
        this.financialSystemWorkflowHelperService = financialSystemWorkflowHelperService;
    }
}
