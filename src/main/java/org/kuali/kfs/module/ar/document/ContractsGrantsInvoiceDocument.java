/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.CollectionEvent;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsInvoiceObjectCode;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceAddressDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceBill;
import org.kuali.kfs.module.ar.businessobject.InvoiceDetailAccountObjectCode;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceMilestone;
import org.kuali.kfs.module.ar.businessobject.InvoiceSuspensionCategory;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Contracts & Grants Invoice document extending Customer Invoice document.
 */
//CU customization to allow billing period to be edited when doc is enroute
public class ContractsGrantsInvoiceDocument extends CustomerInvoiceDocument {

    private static final Logger LOG = LogManager.getLogger();
    private KualiDecimal paymentAmount = KualiDecimal.ZERO;
    private KualiDecimal balanceDue = KualiDecimal.ZERO;
    private KualiDecimal previouslyBilledInvoiceAmount = KualiDecimal.ZERO;
    private KualiDecimal previouslyBilledTotal = KualiDecimal.ZERO;
    private KualiDecimal totalInvoiceInvoiceAmount = KualiDecimal.ZERO;
    private List<ContractsGrantsInvoiceDetail> invoiceDetails;
    private List<CollectionEvent> collectionEvents;
    private List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes;
    private List<InvoiceAddressDetail> invoiceAddressDetails;
    private List<InvoiceAccountDetail> accountDetails;
    private InvoiceGeneralDetail invoiceGeneralDetail;
    private List<InvoiceMilestone> invoiceMilestones;
    private List<InvoiceBill> invoiceBills;
    private List<InvoiceSuspensionCategory> invoiceSuspensionCategories;

    private final String REQUIRES_APPROVAL_SPLIT = "RequiresApprovalSplit";

    public ContractsGrantsInvoiceDocument() {
        invoiceAddressDetails = new ArrayList<>();
        invoiceDetails = new ArrayList<>();
        collectionEvents = new ArrayList<>();
        accountDetails = new ArrayList<>();
        invoiceMilestones = new ArrayList<>();
        invoiceBills = new ArrayList<>();
        invoiceDetailAccountObjectCodes = new ArrayList<>();
        invoiceSuspensionCategories = new ArrayList<>();
    }

    public boolean isFinalizable() {
        final LocalDateTime awardEndingDate =
                getDateTimeService().getLocalDateTime(getInvoiceGeneralDetail().getAward().getAwardEndingDate());
        return getDocumentHeader().getWorkflowDocument().getDateCreated().isAfter(awardEndingDate);
    }

    public boolean isCorrectionDocument() {
        return StringUtils.isNotEmpty(getDocumentHeader().getFinancialDocumentInErrorNumber());
    }

    @Override
    public List buildListOfDeletionAwareLists() {
        final List deletionAwareLists = super.buildListOfDeletionAwareLists();
        if (invoiceSuspensionCategories != null) {
            deletionAwareLists.add(invoiceSuspensionCategories);
        }
        return deletionAwareLists;
    }

    @Override
    public void prepareForSave() {
        super.prepareForSave();
        // To do a recalculate of current expenditures in invoice details section so that the totals get affected properly.

        // To be performed whenever the document is saved only for awards without Milestones or Bills
        if (!ArConstants.BillingFrequencyValues.isMilestone(getInvoiceGeneralDetail())
                && !ArConstants.BillingFrequencyValues.isPredeterminedBilling(getInvoiceGeneralDetail())) {
            final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                    SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
            contractsGrantsInvoiceDocumentService.recalculateTotalAmountBilledToDate(this);
        }
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);

        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            // update award accounts to final billed
            contractsGrantsInvoiceDocumentService.updateLastBilledDate(this);
            if (isInvoiceReversal()) {
                // Invoice correction process when corrected invoice goes to FINAL
                getInvoiceGeneralDetail().setFinalBillIndicator(false);
                final ContractsGrantsInvoiceDocument invoice = (ContractsGrantsInvoiceDocument) SpringContext.getBean(
                        DocumentService.class).getByDocumentHeaderId(getDocumentHeader()
                        .getFinancialDocumentInErrorNumber());
                if (ObjectUtils.isNotNull(invoice)) {
                    //CU customization to get current time from date service
                    invoice.setInvoiceDueDate(getDateTimeService().getCurrentSqlDate());
                    invoice.getInvoiceGeneralDetail().setFinalBillIndicator(false);
                    SpringContext.getBean(DocumentService.class).updateDocument(invoice);
                    // update correction to the AwardAccount Objects since the Invoice was unmarked as Final
                    contractsGrantsInvoiceDocumentService.updateUnfinalizationToAwardAccount(
                            invoice.getAccountDetails(), invoice.getInvoiceGeneralDetail().getProposalNumber());
                    getInvoiceGeneralDetail().setLastBilledDate(null);

                    if (ArConstants.BillingFrequencyValues.isMilestone(invoice.getInvoiceGeneralDetail())) {
                        contractsGrantsInvoiceDocumentService.updateMilestonesBilledIndicator(false,
                                invoice.getInvoiceMilestones());
                    } else if (ArConstants.BillingFrequencyValues.isPredeterminedBilling(
                            invoice.getInvoiceGeneralDetail())) {
                        contractsGrantsInvoiceDocumentService.updateBillsBilledIndicator(false,
                                invoice.getInvoiceBills());
                    }
                } else {
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                            KFSKeyConstants.ERROR_CORRECTED_INVOICE_NOT_FOUND_ERROR,
                            ArKeyConstants.CORRECTED_INVOICE_NOT_FOUND_ERROR);
                }
            } else {
                contractsGrantsInvoiceDocumentService.queueInvoiceTransmissions(this);
                // update Milestones and Bills when invoice goes to final state
                contractsGrantsInvoiceDocumentService.updateBillsAndMilestones(true, invoiceMilestones, invoiceBills);

                // generate the invoices from templates
                contractsGrantsInvoiceDocumentService.generateInvoicesForInvoiceAddresses(this);
            }

            contractsGrantsInvoiceDocumentService.addToAccountObjectCodeBilledTotal(invoiceDetailAccountObjectCodes);
        }
    }

    @Override
    public void toErrorCorrection() {
        super.toErrorCorrection();
        invoiceSuspensionCategories.clear();
        SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class).correctContractsGrantsInvoiceDocument(this);
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(
            final GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        final CustomerInvoiceDetail cid = (CustomerInvoiceDetail) glpeSourceDetail;
        if (cid.getFinancialObjectCode() == null || cid.getAccountsReceivableObjectCode() == null) {
            // do not generate entries if the codes are not set
            // do not cause a validation error either - this is caught by the validation when trying to submit/approve
            // the doc only
            return true;
        }
        return super.generateGeneralLedgerPendingEntries(glpeSourceDetail, sequenceHelper);
    }

    @Override
    public void updateAccountReceivableObjectCodes() {
        final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        for (final Object accountingLine : getSourceAccountingLines()) {
            final CustomerInvoiceDetail cid = (CustomerInvoiceDetail) accountingLine;
            if (cid.getFinancialObjectCode() == null || cid.getAccountsReceivableObjectCode() == null) {
                final Account account = cid.getAccount();
                final ContractsGrantsInvoiceObjectCode cgbiObjectCode = contractsGrantsInvoiceDocumentService
                        .contractGrantsInvoiceObjectCodeForSubFundGroup(account.getSubFundGroup(),
                                account.getChartOfAccountsCode());
                if (cgbiObjectCode != null) {
                    cid.setFinancialObjectCode(cgbiObjectCode.getIncomeFinancialObjectCode());
                    cid.setAccountsReceivableObjectCode(cgbiObjectCode.getReceivableFinancialObjectCode());
                }
            }
        }
    }

    /**
     * @return the list of invoice Details without the Total fields or any indirect cost categories
     */
    public List<ContractsGrantsInvoiceDetail> getDirectCostInvoiceDetails() {
        final List<ContractsGrantsInvoiceDetail> invDetails = new ArrayList<>();
        for (final ContractsGrantsInvoiceDetail invD : invoiceDetails) {
            if (!invD.isIndirectCostIndicator()) {
                invDetails.add(invD);
            }
        }
        return invDetails;
    }

    /**
     * @return a list of invoice details which are indirect costs only. These invoice details are not shown on the
     *         document and is different from the other method getInDirectCostInvoiceDetails() because that method
     *         returns the total.
     */
    public List<ContractsGrantsInvoiceDetail> getIndirectCostInvoiceDetails() {
        final List<ContractsGrantsInvoiceDetail> invDetails = new ArrayList<>();
        for (final ContractsGrantsInvoiceDetail invD : invoiceDetails) {
            if (invD.isIndirectCostIndicator()) {
                invDetails.add(invD);
            }
        }
        return invDetails;
    }

    public List<ContractsGrantsInvoiceDetail> getInvoiceDetails() {
        return invoiceDetails;
    }

    public void setInvoiceDetails(final List<ContractsGrantsInvoiceDetail> invoiceDetails) {
        this.invoiceDetails = invoiceDetails;
    }

    public List<InvoiceDetailAccountObjectCode> getInvoiceDetailAccountObjectCodes() {
        return invoiceDetailAccountObjectCodes;
    }

    public void setInvoiceDetailAccountObjectCodes(
            final List<InvoiceDetailAccountObjectCode> invoiceDetailAccountObjectCodes) {
        this.invoiceDetailAccountObjectCodes = invoiceDetailAccountObjectCodes;
    }

    public List<InvoiceAddressDetail> getInvoiceAddressDetails() {
        return invoiceAddressDetails;
    }

    public void setInvoiceAddressDetails(final List<InvoiceAddressDetail> invoiceAddressDetails) {
        this.invoiceAddressDetails = invoiceAddressDetails;
    }

    public List<InvoiceAccountDetail> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(final List<InvoiceAccountDetail> accountDetails) {
        this.accountDetails = accountDetails;
    }

    public InvoiceGeneralDetail getInvoiceGeneralDetail() {
        return invoiceGeneralDetail;
    }

    public void setInvoiceGeneralDetail(final InvoiceGeneralDetail invoiceGeneralDetail) {
        this.invoiceGeneralDetail = invoiceGeneralDetail;
    }

    public List<InvoiceMilestone> getInvoiceMilestones() {
        return invoiceMilestones;
    }

    public void setInvoiceMilestones(final List<InvoiceMilestone> invoiceMilestones) {
        this.invoiceMilestones = invoiceMilestones;
    }

    public List<InvoiceBill> getInvoiceBills() {
        return invoiceBills;
    }

    public void setInvoiceBills(final List<InvoiceBill> invoiceBills) {
        this.invoiceBills = invoiceBills;
    }

    public ContractsGrantsInvoiceDetail getTotalDirectCostInvoiceDetail() {
        final ContractsGrantsInvoiceDetail totalDirectCostInvoiceDetail = new ContractsGrantsInvoiceDetail();
        for (final ContractsGrantsInvoiceDetail currentInvoiceDetail : getDirectCostInvoiceDetails()) {
            totalDirectCostInvoiceDetail.sumInvoiceDetail(currentInvoiceDetail);
        }
        return totalDirectCostInvoiceDetail;
    }

    public List<CollectionEvent> getCollectionEvents() {
        return collectionEvents;
    }

    public void setCollectionEvents(final List<CollectionEvent> collectionEvents) {
        this.collectionEvents = collectionEvents;
    }

    /**
     * Generate the next Collection Event Code by concatenating a the count of current events +1 formatted, to the
     * document number for this invoice.
     *
     * @return next collection event code
     */
    public String getNextCollectionEventCode() {
        return documentNumber + "-" + String.format("%03d", collectionEvents.size() + 1);
    }

    public ContractsGrantsInvoiceDetail getTotalIndirectCostInvoiceDetail() {
        final ContractsGrantsInvoiceDetail totalInDirectCostInvoiceDetail = new ContractsGrantsInvoiceDetail();
        for (final ContractsGrantsInvoiceDetail currentInvoiceDetail : getIndirectCostInvoiceDetails()) {
            totalInDirectCostInvoiceDetail.sumInvoiceDetail(currentInvoiceDetail);
        }
        return totalInDirectCostInvoiceDetail;
    }

    public ContractsGrantsInvoiceDetail getTotalCostInvoiceDetail() {
        final ContractsGrantsInvoiceDetail totalCostInvoiceDetail = new ContractsGrantsInvoiceDetail();
        totalCostInvoiceDetail.sumInvoiceDetail(getTotalDirectCostInvoiceDetail());
        totalCostInvoiceDetail.sumInvoiceDetail(getTotalIndirectCostInvoiceDetail());
        return totalCostInvoiceDetail;
    }

    public List<InvoiceSuspensionCategory> getInvoiceSuspensionCategories() {
        return invoiceSuspensionCategories;
    }

    public void setInvoiceSuspensionCategories(final List<InvoiceSuspensionCategory> invoiceSuspensionCategories) {
        this.invoiceSuspensionCategories = invoiceSuspensionCategories;
    }

    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(REQUIRES_APPROVAL_SPLIT)) {
            return isRequiresFundingManagerApproval();
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName +
                "\"");
    }

    /**
     * @return true if this CINV should route to the fund managers, false if it should skip
     */
    private boolean isRequiresFundingManagerApproval() {
        final ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService =
                SpringContext.getBean(ContractsGrantsInvoiceDocumentService.class);
        // if auto approve on the award is false or suspension exists or the award is auto-approve but fails to pass
        // validation, then we need to have funds manager approve.
        final boolean result;
        result = !CollectionUtils.isEmpty(getInvoiceSuspensionCategories())
                 || !getInvoiceGeneralDetail().getAward().getAutoApproveIndicator()
                 || contractsGrantsInvoiceDocumentService.isDocumentBatchCreated(this)
                     && !contractsGrantsInvoiceDocumentService.doesInvoicePassValidation(this);
        return result;
    }

    public KualiDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final KualiDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public KualiDecimal getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(final KualiDecimal balanceDue) {
        this.balanceDue = balanceDue;
    }

    public KualiDecimal getPreviouslyBilledInvoiceAmount() {
        return previouslyBilledInvoiceAmount;
    }

    public void setPreviouslyBilledInvoiceAmount(final KualiDecimal previouslyBilledInvoiceAmount) {
        this.previouslyBilledInvoiceAmount = previouslyBilledInvoiceAmount;
    }

    public KualiDecimal getPreviouslyBilledTotal() {
        return previouslyBilledTotal;
    }

    public void setPreviouslyBilledTotal(final KualiDecimal previouslyBilledTotal) {
        this.previouslyBilledTotal = previouslyBilledTotal;
    }

    public KualiDecimal getTotalInvoiceInvoiceAmount() {
        return totalInvoiceInvoiceAmount;
    }

    public void setTotalInvoiceInvoiceAmount(final KualiDecimal totalInvoiceInvoiceAmount) {
        this.totalInvoiceInvoiceAmount = totalInvoiceInvoiceAmount;
    }

    public String getCustomerNumber() {
        return accountsReceivableDocumentHeader.getCustomerNumber();
    }

    public KualiDecimal getTotalBudgetAmount() {
        KualiDecimal total = KualiDecimal.ZERO;

        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getTotalBudget());
        }

        return total;
    }

    public KualiDecimal getTotalInvoiceAmount() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getInvoiceAmount());
        }
        return total;
    }

    public KualiDecimal getTotalCumulativeExpenditures() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getCumulativeExpenditures());
        }
        return total;
    }

    public KualiDecimal getTotalBudgetRemaining() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getBudgetRemaining());
        }
        return total;
    }

    public KualiDecimal getTotalPreviouslyBilled() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getTotalPreviouslyBilled());
        }
        return total;
    }

    public KualiDecimal getTotalAmountBilledToDate() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final InvoiceAccountDetail accountDetail : accountDetails) {
            total = total.add(accountDetail.getTotalAmountBilledToDate());
        }
        return total;
    }

    /**
     * The CINV's rule is that if an invoice detail is positive, it's a debit, and if it's negative, it's a credit
     */
    @Override
    protected boolean isInvoiceDetailReceivableDebit(final CustomerInvoiceDetail customerInvoiceDetail) {
        return customerInvoiceDetail.getAmount().isZero() || customerInvoiceDetail.getAmount().isPositive();
    }

    /**
     * If the invoice detail is negative, then return debit here, otherwise it's a credit
     */
    @Override
    protected boolean isInvoiceDetailIncomeDebit(final CustomerInvoiceDetail customerInvoiceDetail) {
        return customerInvoiceDetail.getAmount().isZero() || customerInvoiceDetail.getAmount().isNegative();
    }

    public boolean isBillingPeriodAdjusted() {
        final WorkflowDocument workflowDoc = this.getDocumentHeader().getWorkflowDocument();
        // CU customization to allow billing period to be edited when doc is enroute
        return (workflowDoc.isSaved() || workflowDoc.isEnroute()) && getInvoiceGeneralDetail().isBillingPeriodAdjusted();
    }
}
