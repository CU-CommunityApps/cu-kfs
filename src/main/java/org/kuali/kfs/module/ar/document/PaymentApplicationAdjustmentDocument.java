/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.ApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.BlanketApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.document.service.impl.PaymentApplicationAdjustmentDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.rice.core.api.util.type.AbstractKualiDecimal;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * back-port FINP 7310
 */
public class PaymentApplicationAdjustmentDocument extends AccountingDocumentBase implements
        GeneralLedgerPendingEntrySource, PaymentApplicationAdjustableDocument, AmountTotaling {
    private static final Logger LOG = LogManager.getLogger();
    private static final String LAUNCHED_FROM_BATCH = "LaunchedBySystemUser";

    private final List<NonAppliedHolding> nonAppliedHoldings = new ArrayList<>();
    private final List<InvoicePaidApplied> invoicePaidApplieds = new ArrayList<>();

    private AccountsReceivableDocumentHeader accountsReceivableDocumentHeader;
    // The documentNumber of the PaymentApplication[Adjustment]Document being adjusted. Cannot be null.
    private String adjusteeDocumentNumber;
    // The documentNumber of the PaymentApplicationAdjustmentDocument which adjusts this Document. Can be null.
    private String adjustmentDocumentNumber;

    private transient DocumentService documentService;
    private transient PaymentApplicationAdjustmentDocumentService paymentApplicationAdjustmentDocumentService;

    public String getAdjusteeDocumentNumber() {
        return adjusteeDocumentNumber;
    }

    public void setAdjusteeDocumentNumber(final String adjusteeDocumentNumber) {
        Validate.isTrue(StringUtils.isNotBlank(adjusteeDocumentNumber),
                "An APPA doc must know what APP doc it adjusts");
        this.adjusteeDocumentNumber = adjusteeDocumentNumber;
    }

    @Override
    public String getAdjustmentDocumentNumber() {
        return adjustmentDocumentNumber;
    }

    public void setAdjustmentDocumentNumber(final String adjustmentDocumentNumber) {
        Validate.isTrue(StringUtils.isNotBlank(adjustmentDocumentNumber),
                "An APPA doc must know what APPA doc adjusts it");
        this.adjustmentDocumentNumber = adjustmentDocumentNumber;
    }

    @Override
    public boolean isAdjusted() {
        return StringUtils.isNotBlank(getAdjustmentDocumentNumber());
    }

    public void clearAdjustmentDocumentNumber() {
        adjustmentDocumentNumber = null;
    }

    @Override
    public List<NonAppliedHolding> getNonAppliedHoldings() {
        return nonAppliedHoldings;
    }

    public void setNonAppliedHoldings(final List<NonAppliedHolding> nonAppliedHoldings) {
        Validate.isTrue(nonAppliedHoldings != null, "nonAppliedHoldings cannot be null");
        this.nonAppliedHoldings.clear();
        this.nonAppliedHoldings.addAll(nonAppliedHoldings);
    }

    /**
     * PaymentApplicationAdjustment docs can contain multiple non applied holdings unlike the PaymentAdjustment doc
     * which can only have one. In order for us to manipulate the relationship between the nonAppliedHoldings and the
     * document, we need to add nonAppliedHoldings to the deletion aware lists so that they can be removed
     *
     * @return A list of lists
     */
    @Override
    public List buildListOfDeletionAwareLists() {
        final List deletionAwareLists = super.buildListOfDeletionAwareLists();
        CollectionUtils.addIgnoreNull(deletionAwareLists, invoicePaidApplieds);
        CollectionUtils.addIgnoreNull(deletionAwareLists, nonAppliedHoldings);
        return deletionAwareLists;
    }

    /**
     * The Form has access to the Document but not the Service. Using this pass-through to avoid the Form needing to
     * know anything else.
     */
    public void fillInFiscalPeriodYear(final Collection<GeneralLedgerPendingEntry> glpes) {
        getPaymentApplicationAdjustmentDocumentService().fillInFiscalPeriodYear(glpes);
    }

    public KualiDecimal getTotalApplied() {
        return KualiDecimal.ZERO
                .add(getSumOfInvoicePaidApplieds())
                .add(getSumOfNonAppliedHoldings())
                .add(getSumOfNonARLines());
    }

    private KualiDecimal getSumOfInvoicePaidApplieds() {
        return invoicePaidApplieds
                .stream()
                .map(InvoicePaidApplied::getInvoiceItemAppliedAmount)
                .reduce(KualiDecimal.ZERO, AbstractKualiDecimal::add);
    }

    private KualiDecimal getSumOfNonAppliedHoldings() {
        return nonAppliedHoldings
                .stream()
                .map(NonAppliedHolding::getFinancialDocumentLineAmount)
                .reduce(KualiDecimal.ZERO, AbstractKualiDecimal::add);
    }

    private KualiDecimal getSumOfNonARLines() {
        return getNonArAccountingLines()
                .stream()
                .map(SourceAccountingLine::getAmount)
                .reduce(KualiDecimal.ZERO, AbstractKualiDecimal::add);
    }

    /**
     * @return The total available to be applied on this document.
     * back-port FINP 7310
     */
    @Override
    public KualiDecimal getTotalDollarAmount() {
        PaymentApplicationAdjustableDocument adjusteeDocument = (PaymentApplicationAdjustableDocument) getAdjusteeDocument();
        KualiDecimal cumulativeAdjusteeNonArTotal = KualiDecimal.ZERO;
        if (anAppaIsBeingAdjusted(adjusteeDocument)) {
            while (adjusteeDocument instanceof PaymentApplicationAdjustmentDocument) {
                cumulativeAdjusteeNonArTotal = cumulativeAdjusteeNonArTotal.add(adjusteeDocument.getNonArTotal());
                adjusteeDocument = (PaymentApplicationAdjustableDocument) ((PaymentApplicationAdjustmentDocument) adjusteeDocument).getAdjusteeDocument();
            }
        } else {
            // An APP is being adjusted
            cumulativeAdjusteeNonArTotal = cumulativeAdjusteeNonArTotal.add(adjusteeDocument.getNonArTotal());
        }

        final PaymentApplicationDocument rootAdjusteeDocument = getRootAdjusteeDocument();
        final KualiDecimal rootAdjusteeTotalFromControl = rootAdjusteeDocument.getTotalFromControl();
        final KualiDecimal totalDollarAmount = rootAdjusteeTotalFromControl.subtract(cumulativeAdjusteeNonArTotal);

        LOG.debug("getTotalDollarAmount() - Exit : totalDollarAmount={}", totalDollarAmount);
        return totalDollarAmount;
    }
    
    private boolean anAppaIsBeingAdjusted(final PaymentApplicationAdjustableDocument adjusteeDocument) {
        return adjusteeDocument instanceof PaymentApplicationAdjustmentDocument;
    }

    @Override
    public KualiDecimal getNonArTotal() {
        return getSumOfNonARLines();
    }

    /**
     * This method subtracts the sum of the invoice paid applieds, non-ar and
     * unapplied totals from the outstanding amount received via the ??? document.
     * <p>
     * NOTE this method is not useful for a non-cash control PayApp, as it
     * doesn't have access to the control documents until it is saved.  Use
     * the same named method on the Form instead.
     */
    public KualiDecimal getUnallocatedBalance() {

        KualiDecimal amount = getTotalDollarAmount();
        amount = amount.subtract(getTotalApplied());
        return amount;
    }

    public boolean isFinal() {
        return getDocumentHeader().getWorkflowDocument().isApproved();
    }

    @Override
    public List<InvoicePaidApplied> getInvoicePaidApplieds() {
        return invoicePaidApplieds;
    }

    public void setInvoicePaidApplieds(final List<InvoicePaidApplied> invoicePaidApplieds) {
        Validate.isTrue(invoicePaidApplieds != null, "invoicePaidApplieds cannot be null");
        this.invoicePaidApplieds.clear();
        this.invoicePaidApplieds.addAll(invoicePaidApplieds);
    }

    public void setNonArAccountingLines(final List<SourceAccountingLine> nonArAccountingLines) {
        Validate.isTrue(nonArAccountingLines != null, "nonArAccountingLines cannot be null");
        sourceAccountingLines = nonArAccountingLines;
    }

    public List<SourceAccountingLine> getNonArAccountingLines() {
        return sourceAccountingLines;
    }

    public AccountsReceivableDocumentHeader getAccountsReceivableDocumentHeader() {
        return accountsReceivableDocumentHeader;
    }

    public void setAccountsReceivableDocumentHeader(AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = accountsReceivableDocumentHeader;
    }

    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper
    ) {
        try {
            final Document adjusteeDocument = getDocumentService().getByDocumentHeaderId(adjusteeDocumentNumber);

            getPaymentApplicationAdjustmentDocumentService().createPendingEntries(
                    adjusteeDocument,
                    this,
                    getPostingYear(),
                    sequenceHelper
            )
                    .forEach(this::addPendingEntry);

            return true;
        } catch (final WorkflowException e) {
            LOG.error("generateDocumentGeneralLedgerPendingEntries(...) - Failed to generate pending entries", e);
        }
        return false;
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return true;
    }

    @Override
    public KualiDecimal getGeneralLedgerPendingEntryAmountForDetail(
            GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {
        return null;
    }

    @Override
    public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        return false;
    }

    // Should use the same Invoices as doRouteStatusChange(...)
    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        final List<String> invoiceNumbers =
                getInvoicePaidApplieds()
                        .stream()
                        .map(InvoicePaidApplied::getFinancialDocumentReferenceInvoiceNumber)
                        .collect(Collectors.toList());
        if (invoiceNumbers.isEmpty()) {
            return null;
        }
        return invoiceNumbers;
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        LOG.debug("doRouteStatusChange(...) - Enter : statusChangedEvent={}", statusChangeEvent);

        super.doRouteStatusChange(statusChangeEvent);

        if (postProcessingShouldBeDone()) {
            final Document adjusteeDocument = getAdjusteeDocument();
            getPaymentApplicationAdjustmentDocumentService().postProcess(adjusteeDocument, this);
        }

        LOG.debug("doRouteStatusChange(...) - Exit");
    }

    private boolean postProcessingShouldBeDone() {
        LOG.debug("postProcessingShouldBeDone(...) - Enter");
        final WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        final boolean postProcessingShouldBeDone = workflowDocument.isFinal();
        LOG.debug("postProcessingShouldBeDone(...) - Exit : postProcessingShouldBeDone={}",
                postProcessingShouldBeDone);
        return postProcessingShouldBeDone;
    }

    private PaymentApplicationDocument getRootAdjusteeDocument() {
        Document adjusteeDocument = getAdjusteeDocument();
        while (true) {
            if (adjusteeDocument instanceof PaymentApplicationDocument) {
                return (PaymentApplicationDocument) adjusteeDocument;
            }
            adjusteeDocument = ((PaymentApplicationAdjustmentDocument) adjusteeDocument).getAdjusteeDocument();
        }
    }

    private Document getAdjusteeDocument() {
        Document adjusteeDocument = null;
        try {
            adjusteeDocument = getDocumentService().getByDocumentHeaderId(adjusteeDocumentNumber);
        } catch (final WorkflowException e) {
            LOG.error("getAdjusteeDocument(...) - This should not happen! : " +
                            "documentNumber={}; adjusteeDocumentNumber={}",
                    getDocumentNumber(),
                    adjusteeDocumentNumber,
                    e);
        }
        return adjusteeDocument;
    }

    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        super.prepareForSave(event);

        // set primary key for NonAppliedHolding if data entered
        nonAppliedHoldings
                .stream()
                .filter(ObjectUtils::isNotNull)
                .filter(nah -> ObjectUtils.isNull(nah.getReferenceFinancialDocumentNumber()))
                .forEach(nah -> nah.setReferenceFinancialDocumentNumber(documentNumber));

        if (generalLedgerPendingEntriesShouldBeCreated(event)) {

            final var entriesCreated = getGeneralLedgerPendingEntryService().generateGeneralLedgerPendingEntries(this);
            if (!entriesCreated) {
                logErrors();
                throw new ValidationException("GLPE generation failed");
            }
        }

    }

    // Create on Save, Submit, Approve, or Blanket Approve
    private static boolean generalLedgerPendingEntriesShouldBeCreated(final KualiDocumentEvent event) {
        return event instanceof SaveDocumentEvent ||
                event instanceof RouteDocumentEvent ||
                event instanceof ApproveDocumentEvent ||
                event instanceof BlanketApproveDocumentEvent;
    }

    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) {
        if (LAUNCHED_FROM_BATCH.equals(nodeName)) {
            return launchedFromBatch();
        }
        throw new UnsupportedOperationException("answerSplitNode('" + nodeName +
                "') was called but no handler for nodeName specified.");
    }

    // If the doc was launched by SYSTEM_USER, it was launched from batch.
    private boolean launchedFromBatch() {
        final String currentInitiatorPrincipalId =
                KimApiServiceLocator.getIdentityService()
                        .getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER)
                        .getPrincipalId();
        final String documentInitiatorPrincipalId =
                getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        return currentInitiatorPrincipalId.equalsIgnoreCase(documentInitiatorPrincipalId);
    }

    // Non-private for testing purposes
    DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = SpringContext.getBean(DocumentService.class);
        }
        return documentService;
    }

    private PaymentApplicationAdjustmentDocumentService getPaymentApplicationAdjustmentDocumentService() {
        if (paymentApplicationAdjustmentDocumentService == null) {
            paymentApplicationAdjustmentDocumentService =
                    SpringContext.getBean(PaymentApplicationAdjustmentDocumentService.class);
        }
        return paymentApplicationAdjustmentDocumentService;
    }
}
