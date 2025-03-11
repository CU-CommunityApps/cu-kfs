package edu.cornell.kfs.module.purap.service.impl;

import static org.kuali.kfs.module.purap.PurapConstants.HUNDRED;
import static org.kuali.kfs.module.purap.PurapConstants.PURAP_ORIGIN_CODE;
import static org.kuali.kfs.core.api.util.type.KualiDecimal.ZERO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapAccountRevisionService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.impl.PurapGeneralLedgerServiceImpl;
import org.kuali.kfs.module.purap.util.SummaryAccount;
import org.kuali.kfs.module.purap.util.UseTaxContainer;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;


public class CuPurapGeneralLedgerServiceImpl extends PurapGeneralLedgerServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    // KFSPTS-1891
    protected CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;
    // END MOD
    private PurapAccountingService purapAccountingService;
    private PurchaseOrderService purchaseOrderService;
    private PurapAccountRevisionService purapAccountRevisionService;
    
    protected int getNextAvailableSequence(final String documentNumber) {
        LOG.debug("getNextAvailableSequence() started");
        final Map<String, String>  fieldValues = new HashMap();
        fieldValues.put("financialSystemOriginationCode", PURAP_ORIGIN_CODE);
        fieldValues.put("documentNumber", documentNumber);
        List<GeneralLedgerPendingEntry> glpes = (List <GeneralLedgerPendingEntry>)SpringContext.getBean(org.kuali.kfs.krad.service.BusinessObjectService.class).findMatching(GeneralLedgerPendingEntry.class, fieldValues);
//      KFSPTS-2632 : Bankoffset will not be posted by nightly batch job because its status is 'N'.
        // so, we need to find the highest transactionsequence.  Otherwise it may cause OLE
        int count = 0;
        if (CollectionUtils.isNotEmpty(glpes)) {
        	for (GeneralLedgerPendingEntry glpe : glpes) {
        		if (glpe.getTransactionLedgerEntrySequenceNumber() > count) {
        			count = glpe.getTransactionLedgerEntrySequenceNumber();
        		}
        	}
        }
        return count + 1;
    }
    
    @Override
    /*
     * Cornell Customization:
     * KualiCo 2023-04-19 version of the method with Cornell customization to generate any document
     * level GL entries (offsets or fee charges) when booking the actuals (not the encumbrances) from
     * KFSPTS-1891.
     * 
     * Needed to also do the following when customizing this method:
     *    - Declare purapAccountingService and purapAccountRevisionService locally and set the corresponding
     *      services in the super class due to the super class not declaring getters.
     *      
     *    - Methods cancellingShouldReverseExternalEntries and cancellingShouldReverseWireTransferEntries
     *      were declared private in KualiCo 2023-04-19 version of the PurapGeneralLedgerServiceImpl class and 
     *      therefore both had to be duplicated in this customized class to retain base code functionality.
     */
    protected boolean generateEntriesPaymentRequest(
            final PaymentRequestDocument preq, final List encumbrances,
            final List summaryAccounts, final String processType) {
        LOG.debug("generateEntriesPaymentRequest() started");
        final boolean success = true;
        preq.setGeneralLedgerPendingEntries(new ArrayList<>());

        /*
         * Can't let generalLedgerPendingEntryService just create all the entries because we need the sequenceHelper
         * to carry over from the encumbrances to the actuals and also because we need to tell the
         * PaymentRequestDocumentRule customize entry method how to customize differently based on if creating an
         * encumbrance or actual.
         */
        final GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(
                getNextAvailableSequence(preq.getDocumentNumber()));

        // when cancelling a PREQ, do not book encumbrances if PO is CLOSED
        if (encumbrances != null && !(CANCEL_PAYMENT_REQUEST.equals(processType)
                && PurchaseOrderStatuses.APPDOC_CLOSED.equals(
                        preq.getPurchaseOrderDocument().getApplicationDocumentStatus()))) {
            LOG.debug("generateEntriesPaymentRequest() generate encumbrance entries");
            if (CREATE_PAYMENT_REQUEST.equals(processType)) {
                // on create, use CREDIT code for encumbrances
                preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_CREDIT_CODE);
            } else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                // on cancel, use DEBIT code
                preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_DEBIT_CODE);
            }

            preq.setGenerateEncumbranceEntries(true);
            for (final Object encumbrance : encumbrances) {
                final AccountingLine accountingLine = (AccountingLine) encumbrance;
                preq.generateGeneralLedgerPendingEntries(accountingLine, sequenceHelper);
                sequenceHelper.increment();
            }
        }

        if (ObjectUtils.isNotNull(summaryAccounts) && !summaryAccounts.isEmpty()) {
            LOG.debug("generateEntriesPaymentRequest() now book the actuals");
            preq.setGenerateEncumbranceEntries(false);

            if (CREATE_PAYMENT_REQUEST.equals(processType) || MODIFY_PAYMENT_REQUEST.equals(processType)) {
                // on create and modify, use DEBIT code
                preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_DEBIT_CODE);
            } else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                // on cancel, use CREDIT code
                preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_CREDIT_CODE);

                preq.setGenerateExternalEntries(cancellingShouldReverseExternalEntries(preq));
                preq.setGenerateWireTransferEntries(cancellingShouldReverseWireTransferEntries(preq));
            }

            for (final Object account : summaryAccounts) {
                final SummaryAccount summaryAccount = (SummaryAccount) account;
                preq.generateGeneralLedgerPendingEntries(summaryAccount.getAccount(), sequenceHelper);
                sequenceHelper.increment();
            }

            preq.setGenerateExternalEntries(false);
            preq.setGenerateWireTransferEntries(false);

            // generate offset accounts for use tax if it exists (useTaxContainers will be empty if not a use tax
            // document)
            final List<UseTaxContainer> useTaxContainers = purapAccountingService.generateUseTaxAccount(preq);
            for (final UseTaxContainer useTaxContainer : useTaxContainers) {
                final List<SourceAccountingLine> accounts = useTaxContainer.getAccounts();
                for (final SourceAccountingLine sourceAccountingLine : accounts) {
                    preq.generateGeneralLedgerPendingEntries(sourceAccountingLine, sequenceHelper,
                            useTaxContainer.getUseTax());
                    sequenceHelper.increment();
                }

            }

            // Manually save preq summary accounts
            if (MODIFY_PAYMENT_REQUEST.equals(processType)) {
                //for modify, regenerate the summary from the doc
                final List<SummaryAccount> summaryAccountsForModify =
                        purapAccountingService.generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
                saveAccountsPayableSummaryAccounts(summaryAccountsForModify, preq.getPurapDocumentIdentifier(),
                        PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
            } else {
                //for create, process and cancel, use the summary accounts
                saveAccountsPayableSummaryAccounts(summaryAccounts, preq.getPurapDocumentIdentifier(),
                        PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
            }

            // manually save cm account change tables (CAMS needs this)
            if (CREATE_PAYMENT_REQUEST.equals(processType)
                || MODIFY_PAYMENT_REQUEST.equals(processType)
                || PROCESS_PAYMENT_REQUEST.equals(processType)
            ) {
                purapAccountRevisionService.savePaymentRequestAccountRevisions(preq.getItems(),
                        preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
            } else if (CANCEL_PAYMENT_REQUEST.equals(processType)) {
                purapAccountRevisionService.cancelPaymentRequestAccountRevisions(preq.getItems(),
                        preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
            }

            // Start: Cornell Customization: KFSPTS-1891
            // generate any document level GL entries (offsets or fee charges)
            // we would only want to do this when booking the actuals (not the encumbrances)
            if (preq.getGeneralLedgerPendingEntries() == null || preq.getGeneralLedgerPendingEntries().size() < 2) {
                LOG.warn("No gl entries for accounting lines.");
            } else {
                // Upon a modify, we need to skip re-assessing any fees
                // in fact, we need to skip making any of these entries since there could be a combination
                // of debits and credit entries in the entry list - this will cause problems if the first is a
                // credit since it uses that to determine the sign of all the other transactions
            
                // upon create, build the entries normally
                if ( CREATE_PAYMENT_REQUEST.equals(processType) ) {
                    getPaymentMethodGeneralLedgerPendingEntryService().generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
                            preq, preq.getPaymentMethodCode(), preq.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", preq.getGeneralLedgerPendingEntry(0), false, false, sequenceHelper);
                } else if ( MODIFY_PAYMENT_REQUEST.equals(processType) ) {
                    // upon modify, we need to calculate the deltas here and pass them in so the appropriate adjustments are created
                    KualiDecimal bankOffsetAmount = KualiDecimal.ZERO;
                    final Map<String,KualiDecimal> changesByChart = new HashMap<String, KualiDecimal>();
                    if (ObjectUtils.isNotNull(summaryAccounts) && !summaryAccounts.isEmpty()) {
                        for ( final SummaryAccount a : (List<SummaryAccount>)summaryAccounts ) {
                            bankOffsetAmount = bankOffsetAmount.add(a.getAccount().getAmount());
                            if (changesByChart.get(a.getAccount().getChartOfAccountsCode()) == null) {
                                changesByChart.put(a.getAccount().getChartOfAccountsCode(), a.getAccount().getAmount());
                            } else {
                               changesByChart.put(a.getAccount().getChartOfAccountsCode(), changesByChart.get(a.getAccount().getChartOfAccountsCode()).add(a.getAccount().getAmount()));
                            }
                        }
                    }
                
                    getPaymentMethodGeneralLedgerPendingEntryService().generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
                            preq, preq.getPaymentMethodCode(), preq.getBankCode(), KRADConstants.DOCUMENT_PROPERTY_NAME + "." + "bankCode", preq.getGeneralLedgerPendingEntry(0), true, false, sequenceHelper, bankOffsetAmount, changesByChart);
                }
            }
            preq.generateDocumentGeneralLedgerPendingEntries(sequenceHelper);
            // End: Cornell Customization: KFSPTS-1891
        }

        // Manually save GL entries for Payment Request and encumbrances
        saveGLEntries(preq.getGeneralLedgerPendingEntries());

        return success;
    }

    /* Cornell Customization:
     * KualiCo 2023-04-19 version of the method had to be copied into this class due to base code's restrictive scope.
     * 
     * When an external payment PREQ is entering PROCESSED in the workflow, reversing entries are creating for the PREQ
     * actuals and then forward entries are generated as the PRQX actuals (see generateEntriesProcessedPaymentRequest).
     * So for an external payment PREQ, if it is processed/final, we need to be generating PRQX entries to cancel
     * the existing PRQX actuals instead of reversing entries for the PREQ actuals.
     */
    private static boolean cancellingShouldReverseExternalEntries(final PaymentRequestDocument preq) {
        return KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_EXTERNAL.equals(preq.getPaymentMethodCode())
               && (
                       preq.getDocumentHeader().getWorkflowDocument().isProcessed()
                       || preq.getDocumentHeader().getWorkflowDocument().isFinal()
               );
    }

    /* Cornell Customization:
     * KualiCo 2023-04-19 version of the method had to be copied into this class due to base code's restrictive scope.
     * 
     * When a WT PREQ is entering PROCESSED in the workflow, reversing entries are creating for the PREQ actuals and
     * then forward entries are generated as the PRQW actuals (see generateEntriesProcessedPaymentRequest()).
     * So for a WT PREQ, if it is processed/final, we need to be generating PRQW entries to cancel the existing PRQW
     * actuals instead of reversing entries for the PREQ actuals.
     */
    private static boolean cancellingShouldReverseWireTransferEntries(final PaymentRequestDocument preq) {
        return KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(preq.getPaymentMethodCode())
               && (
                       preq.getDocumentHeader().getWorkflowDocument().isProcessed()
                       || preq.getDocumentHeader().getWorkflowDocument().isFinal()
               );
    }

	public CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
		return paymentMethodGeneralLedgerPendingEntryService;
	}

	public void setPaymentMethodGeneralLedgerPendingEntryService(
				final CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
		this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
	}
		
    @Override
    protected List<SourceAccountingLine> reencumberEncumbrance(final PaymentRequestDocument preq) {
        LOG.debug("reencumberEncumbrance() started");

        final PurchaseOrderDocument po = purchaseOrderService.getCurrentPurchaseOrder(preq.getPurchaseOrderIdentifier());
        final Map<SourceAccountingLine, KualiDecimal> encumbranceAccountMap = new HashMap();

        // Get each item one by one
        for (final Object item : preq.getItems()) {
            final PaymentRequestItem payRequestItem = (PaymentRequestItem) item;
            final PurchaseOrderItem poItem = getPoItem(po, payRequestItem.getItemLineNumber(), payRequestItem.getItemType());

            // Amount to reencumber for this item
            KualiDecimal itemReEncumber; 

            final String logItmNbr = "Item # " + payRequestItem.getItemLineNumber();
            LOG.debug("reencumberEncumbrance() {}", logItmNbr);

            // If there isn't a PO item or the total amount is 0, we don't need encumbrances
            final KualiDecimal preqItemTotalAmount = (payRequestItem.getTotalAmount() == null) ? KualiDecimal.ZERO : payRequestItem.getTotalAmount();
            if (poItem == null || preqItemTotalAmount.doubleValue() == 0) {
                if (poItem != null) {
                    // KFSUPGRADE-893 recumber $0 item too
                    if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                        LOG.debug("reencumberEncumbrance() {} Calculate encumbrance based on quantity", logItmNbr);

                        // Do disencumbrance calculations based on quantity
                        final KualiDecimal preqQuantity = payRequestItem.getItemQuantity() == null ? ZERO : payRequestItem.getItemQuantity();
                        final KualiDecimal outstandingEncumberedQuantity = poItem.getItemOutstandingEncumberedQuantity() == null ? ZERO : poItem.getItemOutstandingEncumberedQuantity();
                        final KualiDecimal invoicedTotal = poItem.getItemInvoicedTotalQuantity() == null ? ZERO : poItem.getItemInvoicedTotalQuantity();

                        poItem.setItemInvoicedTotalQuantity(invoicedTotal.subtract(preqQuantity));
                        poItem.setItemOutstandingEncumberedQuantity(outstandingEncumberedQuantity.add(preqQuantity));
                    }
                }
                LOG.debug("reencumberEncumbrance() {} No encumbrances required", logItmNbr);
            }
            else {
                LOG.debug("reencumberEncumbrance() {} Calculate encumbrance GL entries", logItmNbr);

                // Do we calculate the encumbrance amount based on quantity or amount?
                if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    LOG.debug("reencumberEncumbrance() {} Calculate encumbrance based on quantity", logItmNbr);

                    // Do disencumbrance calculations based on quantity
                    final KualiDecimal preqQuantity = payRequestItem.getItemQuantity() == null ? ZERO : payRequestItem.getItemQuantity();
                    final KualiDecimal outstandingEncumberedQuantity = poItem.getItemOutstandingEncumberedQuantity() == null ? ZERO : poItem.getItemOutstandingEncumberedQuantity();
                    final KualiDecimal invoicedTotal = poItem.getItemInvoicedTotalQuantity() == null ? ZERO : poItem.getItemInvoicedTotalQuantity();

                    poItem.setItemInvoicedTotalQuantity(invoicedTotal.subtract(preqQuantity));
                    poItem.setItemOutstandingEncumberedQuantity(outstandingEncumberedQuantity.add(preqQuantity));

                    //do math as big decimal as doing it as a KualiDecimal will cause the item price to round to 2 digits
                    itemReEncumber = new KualiDecimal(preqQuantity.bigDecimalValue().multiply(poItem.getItemUnitPrice()));

                    //add tax for encumbrance
                    final KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? ZERO : poItem.getItemTaxAmount();
                    final KualiDecimal encumbranceTaxAmount = preqQuantity.divide(poItem.getItemQuantity()).multiply(itemTaxAmount);
                    itemReEncumber = itemReEncumber.add(encumbranceTaxAmount);

                }
                else {
                    LOG.debug("reencumberEncumbrance() {} Calculate encumbrance based on amount", logItmNbr);

                    itemReEncumber = preqItemTotalAmount;
                    // if re-encumber amount is more than original PO ordered amount... do not exceed ordered amount
                    // this prevents negative encumbrance
                    if ((poItem.getTotalAmount() != null) && (poItem.getTotalAmount().bigDecimalValue().signum() < 0)) {
                        // po item extended cost is negative
                    	if (poItem.getTotalAmount().compareTo(itemReEncumber) > 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    }
                    else if ((poItem.getTotalAmount() != null) && (poItem.getTotalAmount().bigDecimalValue().signum() >= 0)) {
                        // po item extended cost is positive
                        if ((poItem.getTotalAmount().compareTo(itemReEncumber)) < 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    }
                }

                LOG.debug("reencumberEncumbrance() {} Amount to reencumber: {}", logItmNbr, itemReEncumber);

                final KualiDecimal outstandingEncumberedAmount = poItem.getItemOutstandingEncumberedAmount() == null ? ZERO : poItem.getItemOutstandingEncumberedAmount();
                LOG.debug(
                        "reencumberEncumbrance() {} PO Item Outstanding Encumbrance Amount set to: {}",
                        logItmNbr,
                        outstandingEncumberedAmount
                );
                final KualiDecimal newOutstandingEncumberedAmount = outstandingEncumberedAmount.add(itemReEncumber);
                LOG.debug(
                        "reencumberEncumbrance() {} New PO Item Outstanding Encumbrance Amount to set: {}",
                        logItmNbr,
                        newOutstandingEncumberedAmount
                );
                poItem.setItemOutstandingEncumberedAmount(newOutstandingEncumberedAmount);

                final KualiDecimal invoicedTotalAmount = poItem.getItemInvoicedTotalAmount() == null ? ZERO : poItem.getItemInvoicedTotalAmount();
                LOG.debug(
                        "reencumberEncumbrance() {} PO Item Invoiced Total Amount set to: {}",
                        logItmNbr,
                        invoicedTotalAmount
                );
                final KualiDecimal newInvoicedTotalAmount = invoicedTotalAmount.subtract(preqItemTotalAmount);
                LOG.debug(
                        "reencumberEncumbrance() {} New PO Item Invoiced Total Amount to set: {}",
                        logItmNbr,
                        newInvoicedTotalAmount
                );
                poItem.setItemInvoicedTotalAmount(newInvoicedTotalAmount);

                // make the list of accounts for the reencumbrance entry
                PurchaseOrderAccount lastAccount = null;
                KualiDecimal accountTotal = ZERO;

                // Sort accounts
                Collections.sort((List) poItem.getSourceAccountingLines());

                for (final PurApAccountingLine purApAccountingLine : poItem.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    if (!account.isEmpty()) {
                        final SourceAccountingLine acctString = account.generateSourceAccountingLine();

                        // amount = item reencumber * account percent / 100
                        final KualiDecimal reencumbranceAmount = itemReEncumber.multiply(new KualiDecimal(account.getAccountLinePercent().toString())).divide(HUNDRED);

                        account.setItemAccountOutstandingEncumbranceAmount(account.getItemAccountOutstandingEncumbranceAmount().add(reencumbranceAmount));

                        // For rounding check at the end
                        accountTotal = accountTotal.add(reencumbranceAmount);

                        lastAccount = account;

                        LOG.debug("reencumberEncumbrance() {} {} = {}", logItmNbr, acctString, reencumbranceAmount);
                        if (encumbranceAccountMap.containsKey(acctString)) {
                            final KualiDecimal currentAmount = (KualiDecimal) encumbranceAccountMap.get(acctString);
                            encumbranceAccountMap.put(acctString, reencumbranceAmount.add(currentAmount));
                        }
                        else {
                            encumbranceAccountMap.put(acctString, reencumbranceAmount);
                        }
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemReEncumber.subtract(accountTotal);
                    LOG.debug("reencumberEncumbrance() difference: {} {}", logItmNbr, difference);

                    final SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                    final KualiDecimal amount = (KualiDecimal) encumbranceAccountMap.get(acctString);
                    if (amount == null) {
                        encumbranceAccountMap.put(acctString, difference);
                    }
                    else {
                        encumbranceAccountMap.put(acctString, amount.add(difference));
                    }
                    lastAccount.setItemAccountOutstandingEncumbranceAmount(lastAccount.getItemAccountOutstandingEncumbranceAmount().add(difference));
                }
            }
        }

        SpringContext.getBean(BusinessObjectService.class).save(po);

        final List<SourceAccountingLine> encumbranceAccounts = new ArrayList<SourceAccountingLine>();
        for (final SourceAccountingLine acctString : encumbranceAccountMap.keySet()) {
            final KualiDecimal amount = encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        return encumbranceAccounts;
    }

    public void setPurapAccountingService(final PurapAccountingService purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
        super.setPurapAccountingService(purapAccountingService);
    }

    public void setPurchaseOrderService(final PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
        super.setPurchaseOrderService(purchaseOrderService);
    }

    public void setPurapAccountRevisionService(PurapAccountRevisionService purapAccountRevisionService) {
        this.purapAccountRevisionService = purapAccountRevisionService;
        super.setPurapAccountRevisionService(purapAccountRevisionService);
    }

}
