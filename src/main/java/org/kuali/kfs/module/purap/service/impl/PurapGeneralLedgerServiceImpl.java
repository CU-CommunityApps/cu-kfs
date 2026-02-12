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
package org.kuali.kfs.module.purap.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.AccountsPayableSummaryAccount;
import org.kuali.kfs.module.purap.businessobject.CreditMemoItem;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccountRevision;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.service.PurapAccountRevisionService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.module.purap.util.SummaryAccount;
import org.kuali.kfs.module.purap.util.UseTaxContainer;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//CU customization: change method access from private to protected
@Transactional
public class PurapGeneralLedgerServiceImpl implements PurapGeneralLedgerService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
    private PaymentRequestService paymentRequestService;
    private PurapAccountingService purapAccountingService;
    private PurchaseOrderService purchaseOrderService;
    private UniversityDateService universityDateService;
    private ObjectCodeService objectCodeService;
    private PurapAccountRevisionService purapAccountRevisionService;

    /**
     * This method sets various fields in explicitEntry based on other parameters.
     */
    @Override
    public void customizeGeneralLedgerPendingEntry(
            final PurchasingAccountsPayableDocument purapDocument,
            final AccountingLine accountingLine, final GeneralLedgerPendingEntry explicitEntry, final Integer referenceDocumentNumber,
            final String debitCreditCode, final String docType, final boolean isEncumbrance) {
        LOG.debug("customizeGeneralLedgerPendingEntry() started");

        explicitEntry.setDocumentNumber(purapDocument.getDocumentNumber());
        explicitEntry.setTransactionLedgerEntryDescription(entryDescription(purapDocument.getVendorName()));
        explicitEntry.setFinancialSystemOriginationCode(PurapConstants.PURAP_ORIGIN_CODE);

        // Always make the referring document the PO for all PURAP docs except for CM against a vendor.
        // This is required for encumbrance entries. It's not required for actual/liability
        // entries, but it makes things easier to deal with. If vendor, leave referring stuff blank.
        if (ObjectUtils.isNotNull(referenceDocumentNumber)) {
            explicitEntry.setReferenceFinancialDocumentNumber(referenceDocumentNumber.toString());
            explicitEntry.setReferenceFinancialDocumentTypeCode(
                    PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT);
            explicitEntry.setReferenceFinancialSystemOriginationCode(PurapConstants.PURAP_ORIGIN_CODE);
        }

        // DEFAULT TO USE CURRENT; don't use FY on doc in case it's a prior year
        final UniversityDate uDate = universityDateService.getCurrentUniversityDate();
        explicitEntry.setUniversityFiscalYear(uDate.getUniversityFiscalYear());
        explicitEntry.setUniversityFiscalPeriodCode(uDate.getUniversityFiscalAccountingPeriod());

        if (PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT.equals(docType)) {
            if (purapDocument.getPostingYear().compareTo(uDate.getUniversityFiscalYear()) > 0) {
                // USE NEXT AS SET ON PO; POs can be forward dated to not encumber until next fiscal year
                explicitEntry.setUniversityFiscalYear(purapDocument.getPostingYear());
                explicitEntry.setUniversityFiscalPeriodCode(KFSConstants.MONTH1);
            }
        } else if (PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(docType)) {
            final PaymentRequestDocument preq = (PaymentRequestDocument) purapDocument;
            if (paymentRequestService.allowBackpost(preq)) {
                LOG.debug("createGlPendingTransaction() within range to allow backpost; posting entry to " +
                        "period 12 of previous FY");
                explicitEntry.setUniversityFiscalYear(uDate.getUniversityFiscalYear() - 1);
                explicitEntry.setUniversityFiscalPeriodCode(KFSConstants.MONTH12);
            }

            // if alternate payee is paid for non-primary vendor payment, send alternate vendor name in GL desc
            if (preq.getAlternateVendorHeaderGeneratedIdentifier() != null
                    && preq.getAlternateVendorDetailAssignedIdentifier() != null
                    && preq.getVendorHeaderGeneratedIdentifier().compareTo(
                            preq.getAlternateVendorHeaderGeneratedIdentifier()) == 0
                    && preq.getVendorDetailAssignedIdentifier().compareTo(
                            preq.getAlternateVendorDetailAssignedIdentifier()) == 0) {
                explicitEntry.setTransactionLedgerEntryDescription(entryDescription(
                        preq.getPurchaseOrderDocument().getAlternateVendorName()));
            }
        } else if (PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(docType)) {
            final VendorCreditMemoDocument cm = (VendorCreditMemoDocument) purapDocument;
            if (cm.isSourceDocumentPaymentRequest()) {
                // if CM is off of PREQ, use vendor name associated with PO (if alternate)
                final PaymentRequestDocument cmPR = cm.getPaymentRequestDocument();
                // if alternate payee is paid for non-primary vendor payment, send alternate vendor name in GL desc
                if (cmPR.getAlternateVendorHeaderGeneratedIdentifier() != null
                        && cmPR.getAlternateVendorDetailAssignedIdentifier() != null
                        && cmPR.getVendorHeaderGeneratedIdentifier().compareTo(
                                cmPR.getAlternateVendorHeaderGeneratedIdentifier()) == 0
                        && cmPR.getVendorDetailAssignedIdentifier().compareTo(
                                cmPR.getAlternateVendorDetailAssignedIdentifier()) == 0) {
                    final PurchaseOrderDocument cmPO = cm.getPurchaseOrderDocument();
                    explicitEntry.setTransactionLedgerEntryDescription(entryDescription(
                            cmPO.getAlternateVendorName()));
                }
            }
        } else {
            throw new IllegalArgumentException("purapDocument is invalid doc type: " +
                    purapDocument.getDocumentNumber());
        }

        final ObjectCode objectCode = objectCodeService.getByPrimaryId(explicitEntry.getUniversityFiscalYear(),
                explicitEntry.getChartOfAccountsCode(), explicitEntry.getFinancialObjectCode());
        if (ObjectUtils.isNotNull(objectCode)) {
            explicitEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        }

        if (isEncumbrance) {
            explicitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_EXTERNAL_ENCUMBRANCE);

            // D - means the encumbrance is based on the document number
            // R - means the encumbrance is based on the referring document number
            // All encumbrances should set the update code to 'R' regardless of if they were created by the PO, PREQ,
            // or CM
            explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.ENCUMB_UPDT_REFERENCE_DOCUMENT_CD);
        }

        // if the amount is negative, flip the D/C indicator
        if (accountingLine.getAmount().doubleValue() < 0) {
            if (KFSConstants.GL_CREDIT_CODE.equals(debitCreditCode)) {
                explicitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            } else {
                explicitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }
        } else {
            explicitEntry.setTransactionDebitCreditCode(debitCreditCode);
        }
    }

    @Override
    public void generateEntriesCancelAccountsPayableDocument(final AccountsPayableDocument apDocument) {
        LOG.debug("generateEntriesCancelAccountsPayableDocument() started");
        if (apDocument instanceof PaymentRequestDocument) {
            LOG.info("generateEntriesCancelAccountsPayableDocument() cancel PaymentRequestDocument");
            generateEntriesCancelPaymentRequest((PaymentRequestDocument) apDocument);
        } else if (apDocument instanceof VendorCreditMemoDocument) {
            LOG.info("generateEntriesCancelAccountsPayableDocument() cancel CreditMemoDocument");
            generateEntriesCancelCreditMemo((VendorCreditMemoDocument) apDocument);
        }
    }

    @Override
    public void generateEntriesCreatePaymentRequest(final PaymentRequestDocument preq) {
        LOG.debug("generateEntriesCreatePaymentRequest() started");
        final List<SourceAccountingLine> encumbrances = relieveEncumbrance(preq);
        final List<SummaryAccount> summaryAccounts = purapAccountingService
                .generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
        generateEntriesPaymentRequest(preq, encumbrances, summaryAccounts, CREATE_PAYMENT_REQUEST);
    }

    /**
     * Called from generateEntriesCancelAccountsPayableDocument() for Payment Request Document
     *
     * @param preq Payment Request document to cancel
     */
    protected void generateEntriesCancelPaymentRequest(final PaymentRequestDocument preq) {
        LOG.debug("generateEntriesCreatePaymentRequest() started");
        final List<SourceAccountingLine> encumbrances = reencumberEncumbrance(preq);
        final List<SummaryAccount> summaryAccounts = purapAccountingService
                .generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
        generateEntriesPaymentRequest(preq, encumbrances, summaryAccounts, CANCEL_PAYMENT_REQUEST);
    }

    @Override
    public void generateEntriesModifyPaymentRequest(final PaymentRequestDocument preq) {
        LOG.debug("generateEntriesModifyPaymentRequest() started");

        final Map<SourceAccountingLine, KualiDecimal> actualsPositive = new HashMap<>();
        final List<SourceAccountingLine> newAccountingLines =
                purapAccountingService.generateSummaryWithNoZeroTotalsNoUseTax(preq.getItems());
        for (final SourceAccountingLine newAccount : newAccountingLines) {
            actualsPositive.put(newAccount, newAccount.getAmount());
            LOG.debug(
                    "generateEntriesModifyPaymentRequest() actualsPositive: {} = {}",
                    newAccount::getAccountNumber,
                    newAccount::getAmount
            );
        }

        final Map<SourceAccountingLine, KualiDecimal> actualsNegative = new HashMap<>();
        final List<AccountsPayableSummaryAccount> oldAccountingLines =
                purapAccountingService.getAccountsPayableSummaryAccounts(preq.getPurapDocumentIdentifier(),
                        PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);

        for (final AccountsPayableSummaryAccount oldAccount : oldAccountingLines) {
            actualsNegative.put(oldAccount.generateSourceAccountingLine(), oldAccount.getAmount());
            LOG.debug(
                    "generateEntriesModifyPaymentRequest() actualsNegative: {} = {}",
                    oldAccount::getAccountNumber,
                    oldAccount::getAmount
            );
        }

        // Add the positive entries and subtract the negative entries

        // Combine the two maps (copy all the positive entries)
        LOG.debug("generateEntriesModifyPaymentRequest() Combine positive/negative entries");
        final Map<SourceAccountingLine, KualiDecimal> glEntries = new HashMap<>(actualsPositive);

        for (final SourceAccountingLine key : actualsNegative.keySet()) {
            KualiDecimal amt;
            if (glEntries.containsKey(key)) {
                amt = glEntries.get(key);
                amt = amt.subtract(actualsNegative.get(key));
            } else {
                amt = KualiDecimal.ZERO;
                amt = amt.subtract(actualsNegative.get(key));
            }
            glEntries.put(key, amt);
        }

        final List<SummaryAccount> summaryAccounts = new ArrayList<>();
        for (final SourceAccountingLine account : glEntries.keySet()) {
            final KualiDecimal amount = glEntries.get(account);
            if (KualiDecimal.ZERO.compareTo(amount) != 0) {
                account.setAmount(amount);
                final SummaryAccount sa = new SummaryAccount(account);
                summaryAccounts.add(sa);
            }
        }

        LOG.debug("generateEntriesModifyPaymentRequest() Generate GL entries");
        generateEntriesPaymentRequest(preq, null, summaryAccounts, MODIFY_PAYMENT_REQUEST);
    }

    @Override
    public void generateEntriesProcessedPaymentRequest(final PaymentRequestDocument preq) {
        LOG.debug("generateEntriesProcessedPaymentRequest(...) - Enter");
        final boolean isExternal =
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_EXTERNAL.equals(preq.getPaymentMethodCode());
        final boolean isWireTransfer =
                KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(preq.getPaymentMethodCode());
        if (!isExternal && !isWireTransfer) {
            LOG.debug(
                    "generateEntriesProcessedPaymentRequest(...) - Exit: payment method is {}, doing nothing",
                    preq::getPaymentMethodCode
            );
            return;
        }

        final List<SummaryAccount> summaryAccounts =
                purapAccountingService.generateSummaryAccountsWithNoZeroTotalsNoUseTax(preq);
        preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_CREDIT_CODE);
        LOG.debug("generateEntriesProcessedPaymentRequest(...) - credit {} accounts for wire transfer",
                summaryAccounts::size);
        generateEntriesPaymentRequest(preq, null, summaryAccounts, PROCESS_PAYMENT_REQUEST);

        preq.setDebitCreditCodeForGLEntries(KFSConstants.GL_DEBIT_CODE);
        preq.setGenerateExternalEntries(isExternal);
        preq.setGenerateWireTransferEntries(isWireTransfer);
        LOG.debug(
                "generateEntriesProcessedPaymentRequest(...) - debit {} accounts for {}",
                summaryAccounts::size,
                () -> isExternal ? "external" : "wire transfer"
        );
        generateEntriesPaymentRequest(preq, null, summaryAccounts, PROCESS_PAYMENT_REQUEST);
        preq.setGenerateExternalEntries(false);
        preq.setGenerateWireTransferEntries(false);
        LOG.debug("generateEntriesProcessedPaymentRequest(...) - Exit");
    }

    @Override
    public void generateEntriesCreateCreditMemo(final VendorCreditMemoDocument cm) {
        LOG.debug("generateEntriesCreateCreditMemo() started");
        generateEntriesCreditMemo(cm, CREATE_CREDIT_MEMO);
    }

    /**
     * Called from generateEntriesCancelAccountsPayableDocument() for Payment Request Document
     *
     * @param cm
     */
    protected void generateEntriesCancelCreditMemo(final VendorCreditMemoDocument cm) {
        LOG.debug("generateEntriesCancelCreditMemo() started");
        generateEntriesCreditMemo(cm, CANCEL_CREDIT_MEMO);
    }

    /**
     * Retrieves the next available sequence number from the general ledger pending entry table for this document
     *
     * @param documentNumber Document number to find next sequence number
     * @return Next available sequence number
     */
    protected int getNextAvailableSequence(final String documentNumber) {
        LOG.debug("getNextAvailableSequence() started");
        final Map<String, String> fieldValues = new HashMap<>();
        fieldValues.put("financialSystemOriginationCode", PurapConstants.PURAP_ORIGIN_CODE);
        fieldValues.put("documentNumber", documentNumber);
        final int count = businessObjectService.countMatching(GeneralLedgerPendingEntry.class, fieldValues);
        return count + 1;
    }

    /**
     * Creates the general ledger entries for Payment Request actions.
     *
     * @param preq            Payment Request document to create entries
     * @param encumbrances    List of encumbrance accounts if applies
     * @param summaryAccounts List of preq accounts to create entries
     * @param processType     Type of process (create, modify, cancel)
     * @return Boolean returned indicating whether entry creation succeeded
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
        } else if (MODIFY_PAYMENT_REQUEST.equals(processType) && itemsChanged(preq.getItems())) {
            purapAccountRevisionService.savePaymentRequestAccountRevisions(preq.getItems(),
                    preq.getPostingYearFromPendingGLEntries(), preq.getPostingPeriodCodeFromPendingGLEntries());
        }

        // Manually save GL entries for Payment Request and encumbrances
        saveGLEntries(preq.getGeneralLedgerPendingEntries());

        return success;
    }

    //CU customization: change method access from private to protected
    protected boolean itemsChanged(final List<? extends PaymentRequestItem> items) {
        final List<Integer> itemIdentifiers = items.stream()
                        .map(PaymentRequestItem::getItemIdentifier)
                        .collect(Collectors.toList());

        final int matchingAccountRevisions = businessObjectService.countMatching(
                PaymentRequestAccountRevision.class,
                Map.of("itemIdentifier", itemIdentifiers)
        );

        return items.size() != matchingAccountRevisions;
    }

    /*
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

    /*
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

    /**
     * Creates the general ledger entries for Credit Memo actions.
     *
     * @param cm       Credit Memo document to create entries
     * @param isCancel Indicates if request is a cancel or create
     * @return Boolean returned indicating whether entry creation succeeded
     */
    protected boolean generateEntriesCreditMemo(final VendorCreditMemoDocument cm, final boolean isCancel) {
        LOG.debug("generateEntriesCreditMemo() started");

        cm.setGeneralLedgerPendingEntries(new ArrayList<>());

        final GeneralLedgerPendingEntrySequenceHelper sequenceHelper = new GeneralLedgerPendingEntrySequenceHelper(
                getNextAvailableSequence(cm.getDocumentNumber()));

        if (!cm.isSourceVendor()) {
            LOG.debug("generateEntriesCreditMemo() create encumbrance entries for CM against a PO or PREQ (not " +
                    "vendor)");
            PurchaseOrderDocument po = null;
            if (cm.isSourceDocumentPurchaseOrder()) {
                LOG.debug("generateEntriesCreditMemo() PO type");
                po = purchaseOrderService.getCurrentPurchaseOrder(cm.getPurchaseOrderIdentifier());
            } else if (cm.isSourceDocumentPaymentRequest()) {
                LOG.debug("generateEntriesCreditMemo() PREQ type");
                po = purchaseOrderService.getCurrentPurchaseOrder(cm.getPaymentRequestDocument()
                        .getPurchaseOrderIdentifier());
            }

            // for CM cancel or create, do not book encumbrances if PO is CLOSED, but do update the amounts on the PO
            final List<SourceAccountingLine> encumbrances = getCreditMemoEncumbrance(cm, po, isCancel);
            if (!PurchaseOrderStatuses.APPDOC_CLOSED.equals(po.getApplicationDocumentStatus())) {
                if (encumbrances != null) {
                    cm.setGenerateEncumbranceEntries(true);

                    // even if generating encumbrance entries on cancel, call is the same because the method gets
                    // negative amounts from the map so Debits on negatives = a credit
                    cm.setDebitCreditCodeForGLEntries(KFSConstants.GL_DEBIT_CODE);

                    for (final SourceAccountingLine accountingLine : encumbrances) {
                        if (accountingLine.getAmount().compareTo(KualiDecimal.ZERO) != 0) {
                            cm.generateGeneralLedgerPendingEntries(accountingLine, sequenceHelper);
                            sequenceHelper.increment();
                        }
                    }
                }
            }
        }

        final List<SummaryAccount> summaryAccounts = purapAccountingService
                .generateSummaryAccountsWithNoZeroTotalsNoUseTax(cm);
        if (summaryAccounts != null) {
            LOG.debug("generateEntriesCreditMemo() now book the actuals");
            cm.setGenerateEncumbranceEntries(false);

            if (!isCancel) {
                // on create, use CREDIT code
                cm.setDebitCreditCodeForGLEntries(KFSConstants.GL_CREDIT_CODE);
            } else {
                // on cancel, use DEBIT code
                cm.setDebitCreditCodeForGLEntries(KFSConstants.GL_DEBIT_CODE);
            }

            for (final SummaryAccount summaryAccount : summaryAccounts) {
                cm.generateGeneralLedgerPendingEntries(summaryAccount.getAccount(), sequenceHelper);
                sequenceHelper.increment();
            }
            // generate offset accounts for use tax if it exists (useTaxContainers will be empty if not a use tax
            // document)
            final List<UseTaxContainer> useTaxContainers = purapAccountingService.generateUseTaxAccount(cm);
            for (final UseTaxContainer useTaxContainer : useTaxContainers) {
                final List<SourceAccountingLine> accounts = useTaxContainer.getAccounts();
                for (final SourceAccountingLine sourceAccountingLine : accounts) {
                    cm.generateGeneralLedgerPendingEntries(sourceAccountingLine, sequenceHelper,
                            useTaxContainer.getUseTax());
                    sequenceHelper.increment();
                }
            }

            // manually save cm account change tables (CAMS needs this)
            if (!isCancel) {
                purapAccountRevisionService.saveCreditMemoAccountRevisions(cm.getItems(),
                        cm.getPostingYearFromPendingGLEntries(), cm.getPostingPeriodCodeFromPendingGLEntries());
            } else {
                purapAccountRevisionService.cancelCreditMemoAccountRevisions(cm.getItems(),
                        cm.getPostingYearFromPendingGLEntries(), cm.getPostingPeriodCodeFromPendingGLEntries());
            }
        }

        saveGLEntries(cm.getGeneralLedgerPendingEntries());

        LOG.debug("generateEntriesCreditMemo() ended");
        return true;
    }

    @Override
    public void generateEntriesApproveAmendPurchaseOrder(final PurchaseOrderDocument po) {
        LOG.debug("generateEntriesApproveAmendPurchaseOrder() started");

        // Set outstanding encumbered quantity/amount on items
        for (final Object entry : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) entry;

            // if invoice fields are null (as would be for new items), set fields to zero
            item.setItemInvoicedTotalAmount(item.getItemInvoicedTotalAmount() == null ? KualiDecimal.ZERO :
                    item.getItemInvoicedTotalAmount());
            item.setItemInvoicedTotalQuantity(item.getItemInvoicedTotalQuantity() == null ? KualiDecimal.ZERO :
                    item.getItemInvoicedTotalQuantity());

            if (!item.isItemActiveIndicator()) {
                // set outstanding encumbrance amounts to zero for inactive items
                item.setItemOutstandingEncumberedQuantity(KualiDecimal.ZERO);
                item.setItemOutstandingEncumberedAmount(KualiDecimal.ZERO);

                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    account.setItemAccountOutstandingEncumbranceAmount(KualiDecimal.ZERO);
                    account.setAlternateAmountForGLEntryCreation(KualiDecimal.ZERO);
                }
            } else {
                // Set quantities
                if (item.getItemQuantity() != null) {
                    item.setItemOutstandingEncumberedQuantity(item.getItemQuantity()
                            .subtract(item.getItemInvoicedTotalQuantity()));
                } else {
                    // if order qty is null, outstanding encumbered qty should be null
                    item.setItemOutstandingEncumberedQuantity(null);
                }

                // Set amount
                if (item.getItemOutstandingEncumberedQuantity() != null) {
                    //do math as big decimal as doing it as a KualiDecimal will cause the item price to round to 2
                    // digits
                    KualiDecimal itemEncumber = new KualiDecimal(item.getItemOutstandingEncumberedQuantity()
                            .bigDecimalValue().multiply(item.getItemUnitPrice()));

                    //add tax for encumbrance
                    final KualiDecimal itemTaxAmount = item.getItemTaxAmount() == null ? KualiDecimal.ZERO :
                            item.getItemTaxAmount();
                    itemEncumber = itemEncumber.add(itemTaxAmount);

                    item.setItemOutstandingEncumberedAmount(itemEncumber);
                } else {
                    if (item.getItemUnitPrice() != null) {
                        item.setItemOutstandingEncumberedAmount(new KualiDecimal(item.getItemUnitPrice()
                                .subtract(item.getItemInvoicedTotalAmount().bigDecimalValue())));
                    }
                }

                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    final KualiDecimal itemOutstandingEncumbranceAmt = calculateOutstandingEncumbranceAmt(item, account);
                    account.setItemAccountOutstandingEncumbranceAmount(itemOutstandingEncumbranceAmt);
                    account.setAlternateAmountForGLEntryCreation(itemOutstandingEncumbranceAmt);
                }
            }
        }

        final PurchaseOrderDocument oldPO = purchaseOrderService.getCurrentPurchaseOrder(po.getPurapDocumentIdentifier());

        if (oldPO == null) {
            throw new IllegalArgumentException("Current Purchase Order not found - poId = " +
                    oldPO.getPurapDocumentIdentifier());
        }

        final List<SourceAccountingLine> newAccounts = purapAccountingService
                .generateSummaryWithNoZeroTotalsUsingAlternateAmount(po.getItemsActiveOnly());
        final List<SourceAccountingLine> oldAccounts = purapAccountingService
                .generateSummaryWithNoZeroTotalsUsingAlternateAmount(oldPO.getItemsActiveOnlySetupAlternateAmount());

        final Map<SourceAccountingLine, KualiDecimal> combination = new HashMap<>();

        // Add amounts from the new PO
        for (final SourceAccountingLine newAccount : newAccounts) {
            combination.put(newAccount, newAccount.getAmount());
        }

        LOG.info("generateEntriesApproveAmendPurchaseOrder() combination after the add");
        for (final Object combinationKey : combination.keySet()) {
            final SourceAccountingLine element = (SourceAccountingLine) combinationKey;
            LOG.info(
                    "generateEntriesApproveAmendPurchaseOrder() {} = {}",
                    () -> element,
                    () -> combination.get(element).floatValue()
            );
        }

        // Subtract the amounts from the old PO
        for (final SourceAccountingLine oldAccount : oldAccounts) {
            if (combination.containsKey(oldAccount)) {
                KualiDecimal amount = combination.get(oldAccount);
                amount = amount.subtract(oldAccount.getAmount());
                combination.put(oldAccount, amount);
            } else {
                combination.put(oldAccount, KualiDecimal.ZERO.subtract(oldAccount.getAmount()));
            }
        }

        LOG.debug("generateEntriesApproveAmendPurchaseOrder() combination after the subtract");
        for (final Object combinationKey : combination.keySet()) {
            final SourceAccountingLine element = (SourceAccountingLine) combinationKey;
            LOG.info(
                    "generateEntriesApproveAmendPurchaseOrder() {} = {}",
                    () -> element,
                    () -> combination.get(element).floatValue()
            );
        }

        final List<SourceAccountingLine> encumbranceAccounts = new ArrayList<>();
        for (final Object combinationKey : combination.keySet()) {
            final SourceAccountingLine account = (SourceAccountingLine) combinationKey;
            final KualiDecimal amount = combination.get(account);
            if (KualiDecimal.ZERO.compareTo(amount) != 0) {
                account.setAmount(amount);
                encumbranceAccounts.add(account);
            }
        }

        po.setGlOnlySourceAccountingLines(encumbranceAccounts);
        generalLedgerPendingEntryService.generateGeneralLedgerPendingEntries(po);
        saveGLEntries(po.getGeneralLedgerPendingEntries());
        LOG.debug("generateEntriesApproveAmendPo() gl entries created; exit method");
    }

    // scope loosened for test purposes
    KualiDecimal calculateOutstandingEncumbranceAmt(final PurchaseOrderItem item, final PurchaseOrderAccount account) {
        BigDecimal percent = new BigDecimal(account.getAccountLinePercent().toString());
        percent = percent.divide(new BigDecimal("100"));
        final BigDecimal itemOutstandingEncumbranceAmt = item.getItemOutstandingEncumberedAmount()
                .bigDecimalValue().multiply(percent);
        return new KualiDecimal(itemOutstandingEncumbranceAmt);
    }

    @Override
    public void generateEntriesClosePurchaseOrder(final PurchaseOrderDocument po) {
        LOG.debug("generateEntriesClosePurchaseOrder() started");

        // Set outstanding encumbered quantity/amount on items
        for (final Object poItem : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) poItem;
            final String logItmNbr = "Item # " + item.getItemLineNumber();

            if (!item.isItemActiveIndicator()) {
                continue;
            }

            final KualiDecimal itemAmount;
            LOG.debug("generateEntriesClosePurchaseOrder() {} Calculate based on amounts", logItmNbr);

            itemAmount = item.getItemOutstandingEncumberedAmount() == null ? KualiDecimal.ZERO :
                    item.getItemOutstandingEncumberedAmount();

            KualiDecimal accountTotal = KualiDecimal.ZERO;
            PurchaseOrderAccount lastAccount = null;
            if (itemAmount.compareTo(KualiDecimal.ZERO) != 0) {
                Collections.sort((List) item.getSourceAccountingLines());

                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount acct = (PurchaseOrderAccount) purApAccountingLine;
                    if (!acct.isEmpty()) {
                        final KualiDecimal acctAmount = itemAmount
                                .multiply(new KualiDecimal(acct.getAccountLinePercent().toString()))
                                .divide(PurapConstants.HUNDRED);
                        accountTotal = accountTotal.add(acctAmount);
                        acct.setAlternateAmountForGLEntryCreation(acctAmount);
                        lastAccount = acct;
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemAmount.subtract(accountTotal);
                    LOG.debug("generateEntriesClosePurchaseOrder() difference: {} {}", logItmNbr, difference);

                    final KualiDecimal amount = lastAccount.getAlternateAmountForGLEntryCreation();
                    if (ObjectUtils.isNotNull(amount)) {
                        lastAccount.setAlternateAmountForGLEntryCreation(amount.add(difference));
                    } else {
                        lastAccount.setAlternateAmountForGLEntryCreation(difference);
                    }
                }
            }
        }

        po.setGlOnlySourceAccountingLines(purapAccountingService.generateSummaryWithNoZeroTotalsUsingAlternateAmount(
                po.getItemsActiveOnly()));
        if (shouldGenerateGLPEForPurchaseOrder(po)) {
            generalLedgerPendingEntryService.generateGeneralLedgerPendingEntries(po);
            saveGLEntries(po.getGeneralLedgerPendingEntries());
            LOG.debug("generateEntriesClosePurchaseOrder() gl entries created; exit method");
        }

        // Set outstanding encumbered quantity/amount on items
        for (final Object poItem : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) poItem;
            if (item.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                item.setItemOutstandingEncumberedQuantity(KualiDecimal.ZERO);
            }
            item.setItemOutstandingEncumberedAmount(KualiDecimal.ZERO);
            final List<PurApAccountingLine> sourceAccountingLines = item.getSourceAccountingLines();
            for (final PurApAccountingLine purApAccountingLine : sourceAccountingLines) {
                final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                account.setItemAccountOutstandingEncumbranceAmount(KualiDecimal.ZERO);
            }
        }
    }

    /**
     * We should not generate general ledger pending entries for Purchase Order Close Document and Purchase Order
     * Reopen Document with $0 amount.
     *
     * @param po
     * @return
     */
    protected boolean shouldGenerateGLPEForPurchaseOrder(final PurchaseOrderDocument po) {
        for (final SourceAccountingLine acct : po.getGlOnlySourceAccountingLines()) {
            if (acct.getAmount().compareTo(KualiDecimal.ZERO) != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void generateEntriesReopenPurchaseOrder(final PurchaseOrderDocument po) {
        LOG.debug("generateEntriesReopenPurchaseOrder() started");

        // Set outstanding encumbered quantity/amount on items
        for (final Object poItem : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) poItem;
            if (item.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                item.getItemQuantity().subtract(item.getItemInvoicedTotalQuantity());
                item.setItemOutstandingEncumberedQuantity(
                        item.getItemQuantity().subtract(item.getItemInvoicedTotalQuantity()));
                item.setItemOutstandingEncumberedAmount(new KualiDecimal(
                        item.getItemOutstandingEncumberedQuantity().bigDecimalValue()
                                .multiply(item.getItemUnitPrice())));
            } else {
                item.setItemOutstandingEncumberedAmount(
                        item.getTotalAmount().subtract(item.getItemInvoicedTotalAmount()));
            }
            final List<PurApAccountingLine> sourceAccountingLines = item.getSourceAccountingLines();
            for (final PurApAccountingLine purApAccountingLine : sourceAccountingLines) {
                final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                account.setItemAccountOutstandingEncumbranceAmount(new KualiDecimal(
                        item.getItemOutstandingEncumberedAmount().bigDecimalValue()
                                .multiply(account.getAccountLinePercent())
                                .divide(KFSConstants.ONE_HUNDRED.bigDecimalValue())));
            }
        }

        // Set outstanding encumbered quantity/amount on items
        for (final Object poItem : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) poItem;
            final String logItmNbr = "Item # " + item.getItemLineNumber();

            if (!item.isItemActiveIndicator()) {
                continue;
            }

            final KualiDecimal itemAmount;
            if (item.getItemType().isAmountBasedGeneralLedgerIndicator()) {
                LOG.debug("generateEntriesReopenPurchaseOrder() {} Calculate based on amounts", logItmNbr);
                itemAmount = item.getItemOutstandingEncumberedAmount() == null ? KualiDecimal.ZERO :
                        item.getItemOutstandingEncumberedAmount();
            } else {
                LOG.debug("generateEntriesReopenPurchaseOrder() {} Calculate based on quantities", logItmNbr);
                //do math as big decimal as doing it as a KualiDecimal will cause the item price to round to 2 digits
                itemAmount = new KualiDecimal(item.getItemOutstandingEncumberedQuantity().bigDecimalValue()
                        .multiply(item.getItemUnitPrice()));
            }

            KualiDecimal accountTotal = KualiDecimal.ZERO;
            PurchaseOrderAccount lastAccount = null;
            if (itemAmount.compareTo(KualiDecimal.ZERO) != 0) {
                Collections.sort((List) item.getSourceAccountingLines());

                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount acct = (PurchaseOrderAccount) purApAccountingLine;
                    if (!acct.isEmpty()) {
                        final KualiDecimal acctAmount = itemAmount
                            .multiply(new KualiDecimal(acct.getAccountLinePercent().toString()))
                            .divide(PurapConstants.HUNDRED);
                        accountTotal = accountTotal.add(acctAmount);
                        acct.setAlternateAmountForGLEntryCreation(acctAmount);
                        lastAccount = acct;
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemAmount.subtract(accountTotal);
                    LOG.debug("generateEntriesReopenPurchaseOrder() difference: {} {}", logItmNbr, difference);

                    final KualiDecimal amount = lastAccount.getAlternateAmountForGLEntryCreation();
                    if (ObjectUtils.isNotNull(amount)) {
                        lastAccount.setAlternateAmountForGLEntryCreation(amount.add(difference));
                    } else {
                        lastAccount.setAlternateAmountForGLEntryCreation(difference);
                    }
                }
            }
        }

        po.setGlOnlySourceAccountingLines(purapAccountingService.generateSummaryWithNoZeroTotalsUsingAlternateAmount(
                po.getItemsActiveOnly()));
        if (shouldGenerateGLPEForPurchaseOrder(po)) {
            generalLedgerPendingEntryService.generateGeneralLedgerPendingEntries(po);
            saveGLEntries(po.getGeneralLedgerPendingEntries());
            LOG.debug("generateEntriesReopenPurchaseOrder() gl entries created; exit method");
        }
        LOG.debug("generateEntriesReopenPurchaseOrder() no gl entries created because the amount is 0; exit method");
    }

    @Override
    public void generateEntriesVoidPurchaseOrder(final PurchaseOrderDocument po) {
        LOG.debug("generateEntriesVoidPurchaseOrder() started");

        // Set outstanding encumbered quantity/amount on items
        for (final Object poItem : po.getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) poItem;
            final String logItmNbr = "Item # " + item.getItemLineNumber();

            if (!item.isItemActiveIndicator()) {
                continue;
            }

            // just use the outstanding amount as recalculating here, particularly the item tax will cause amounts to
            // be over or under encumbered and the remaining encumbered amount should be unencumbered during a close
            LOG.debug("generateEntriesVoidPurchaseOrder() {} Calculate based on amounts", logItmNbr);

            final KualiDecimal itemAmount = item.getItemOutstandingEncumberedAmount() == null ? KualiDecimal.ZERO :
                    item.getItemOutstandingEncumberedAmount();

            KualiDecimal accountTotal = KualiDecimal.ZERO;
            PurchaseOrderAccount lastAccount = null;
            if (itemAmount.compareTo(KualiDecimal.ZERO) != 0) {
                Collections.sort((List) item.getSourceAccountingLines());

                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount acct = (PurchaseOrderAccount) purApAccountingLine;
                    if (!acct.isEmpty()) {
                        final KualiDecimal acctAmount = itemAmount
                                .multiply(new KualiDecimal(acct.getAccountLinePercent().toString()))
                                .divide(PurapConstants.HUNDRED);
                        accountTotal = accountTotal.add(acctAmount);
                        acct.setAlternateAmountForGLEntryCreation(acctAmount);
                        lastAccount = acct;
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemAmount.subtract(accountTotal);
                    LOG.debug("generateEntriesVoidPurchaseOrder() difference: {} {}", logItmNbr, difference);

                    final KualiDecimal amount = lastAccount.getAlternateAmountForGLEntryCreation();
                    if (ObjectUtils.isNotNull(amount)) {
                        lastAccount.setAlternateAmountForGLEntryCreation(amount.add(difference));
                    } else {
                        lastAccount.setAlternateAmountForGLEntryCreation(difference);
                    }
                }
            }
        }

        po.setGlOnlySourceAccountingLines(purapAccountingService.generateSummaryWithNoZeroTotalsUsingAlternateAmount(
                po.getItemsActiveOnly()));
        generalLedgerPendingEntryService.generateGeneralLedgerPendingEntries(po);
        saveGLEntries(po.getGeneralLedgerPendingEntries());
        LOG.debug("generateEntriesVoidPurchaseOrder() gl entries created; exit method");
    }

    /**
     * Relieve the Encumbrance on a PO based on values in a PREQ. This is to be called when a PREQ is created. Note:
     * This modifies the encumbrance values on the PO and saves the PO
     *
     * @param preq PREQ for invoice
     * @return List of accounting lines to use to create the pending general ledger entries
     */
    protected List<SourceAccountingLine> relieveEncumbrance(final PaymentRequestDocument preq) {
        LOG.debug("relieveEncumbrance() started");

        final Map<SourceAccountingLine, KualiDecimal> encumbranceAccountMap = new HashMap<>();
        final PurchaseOrderDocument po = purchaseOrderService.getCurrentPurchaseOrder(preq.getPurchaseOrderIdentifier());

        for (final Object item : preq.getItems()) {
            final PaymentRequestItem preqItem = (PaymentRequestItem) item;
            final PurchaseOrderItem poItem = getPoItem(po, preqItem.getItemLineNumber(), preqItem.getItemType());

            // Set this true if we relieve the entire encumbrance
            boolean takeAll = false;
            // Amount to disencumber for this item
            KualiDecimal itemDisEncumber;

            final String logItmNbr = "Item # " + preqItem.getItemLineNumber();
            LOG.debug("relieveEncumbrance() {}", logItmNbr);

            // If there isn't a PO item or the extended price is 0, we don't need encumbrances
            if (poItem == null) {
                LOG.debug("relieveEncumbrance() {} No encumbrances required because po item is null", logItmNbr);
            } else {
                KualiDecimal preqItemTotalAmount = preqItem.getTotalAmount();
                if (preqItemTotalAmount == null) {
                    preqItemTotalAmount = KualiDecimal.ZERO;
                }
                if (KualiDecimal.ZERO.compareTo(preqItemTotalAmount) == 0) {
                    /*
                     * This is a specialized case where PREQ item being processed must adjust the PO item's
                     * outstanding encumbered quantity. This kind of scenario is mostly seen on warranty type items.
                     * The following must be true to do this: PREQ item Extended Price must be ZERO, PREQ item
                     * invoice quantity must be not empty and not ZERO, and PO item is quantity based PO item unit
                     * cost is ZERO
                     */
                    LOG.debug(
                            "relieveEncumbrance() {} No GL encumbrances required because extended price is ZERO",
                            logItmNbr
                    );

                    if (poItem.getItemQuantity() != null
                        && BigDecimal.ZERO.compareTo(poItem.getItemUnitPrice()) == 0) {
                        // po has order quantity and unit price is ZERO... reduce outstanding encumbered quantity
                        LOG.debug("relieveEncumbrance() {} Calculate po outstanding encumbrance", logItmNbr);

                        // Do encumbrance calculations based on quantity
                        if (preqItem.getItemQuantity() != null
                            && KualiDecimal.ZERO.compareTo(preqItem.getItemQuantity()) != 0) {
                            final KualiDecimal invoiceQuantity = preqItem.getItemQuantity();
                            final KualiDecimal outstandingEncumberedQuantity =
                                    poItem.getItemOutstandingEncumberedQuantity() == null ? KualiDecimal.ZERO :
                                            poItem.getItemOutstandingEncumberedQuantity();

                            final KualiDecimal encumbranceQuantity;
                            if (invoiceQuantity.compareTo(outstandingEncumberedQuantity) > 0) {
                                // We bought more than the quantity on the PO
                                LOG.debug("relieveEncumbrance() {} we bought more than the qty on the PO", logItmNbr);
                                poItem.setItemOutstandingEncumberedQuantity(KualiDecimal.ZERO);
                            } else {
                                encumbranceQuantity = invoiceQuantity;
                                poItem.setItemOutstandingEncumberedQuantity(
                                        outstandingEncumberedQuantity.subtract(encumbranceQuantity));
                                LOG.debug(
                                        "relieveEncumbrance() {} adjusting outstanding encumbrance qty - "
                                        + "encumbranceQty {} outstandingEncumberedQty {}",
                                        () -> logItmNbr,
                                        () -> encumbranceQuantity,
                                        poItem::getItemOutstandingEncumberedQuantity
                                );
                            }

                            if (poItem.getItemInvoicedTotalQuantity() == null) {
                                poItem.setItemInvoicedTotalQuantity(invoiceQuantity);
                            } else {
                                poItem.setItemInvoicedTotalQuantity(
                                        poItem.getItemInvoicedTotalQuantity().add(invoiceQuantity));
                            }
                        }
                    }
                } else {
                    LOG.debug("relieveEncumbrance() {} Calculate encumbrance GL entries", logItmNbr);

                    // Do we calculate the encumbrance amount based on quantity or amount?
                    if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                        LOG.debug("relieveEncumbrance() {} Calculate encumbrance based on quantity", logItmNbr);

                        // Do encumbrance calculations based on quantity
                        final KualiDecimal invoiceQuantity =
                                preqItem.getItemQuantity() == null ? KualiDecimal.ZERO : preqItem.getItemQuantity();
                        final KualiDecimal outstandingEncumberedQuantity =
                                poItem.getItemOutstandingEncumberedQuantity() == null ? KualiDecimal.ZERO :
                                        poItem.getItemOutstandingEncumberedQuantity();

                        final KualiDecimal encumbranceQuantity;

                        if (invoiceQuantity.compareTo(outstandingEncumberedQuantity) > 0) {
                            // We bought more than the quantity on the PO
                            LOG.debug("relieveEncumbrance() {} we bought more than the qty on the PO", logItmNbr);

                            encumbranceQuantity = outstandingEncumberedQuantity;
                            poItem.setItemOutstandingEncumberedQuantity(KualiDecimal.ZERO);
                            takeAll = true;
                        } else {
                            encumbranceQuantity = invoiceQuantity;
                            poItem.setItemOutstandingEncumberedQuantity(
                                    outstandingEncumberedQuantity.subtract(encumbranceQuantity));
                            if (KualiDecimal.ZERO.compareTo(poItem.getItemOutstandingEncumberedQuantity()) == 0) {
                                takeAll = true;
                            }
                            LOG.debug(
                                    "relieveEncumbrance() {} encumbranceQty {} outstandingEncumberedQty {}",
                                    () -> logItmNbr,
                                    () -> encumbranceQuantity,
                                    poItem::getItemOutstandingEncumberedQuantity
                            );
                        }

                        if (poItem.getItemInvoicedTotalQuantity() == null) {
                            poItem.setItemInvoicedTotalQuantity(invoiceQuantity);
                        } else {
                            poItem.setItemInvoicedTotalQuantity(
                                    poItem.getItemInvoicedTotalQuantity().add(invoiceQuantity));
                        }

                        itemDisEncumber = new KualiDecimal(encumbranceQuantity.bigDecimalValue()
                                .multiply(poItem.getItemUnitPrice()));

                        //add tax for encumbrance
                        final KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? KualiDecimal.ZERO :
                                poItem.getItemTaxAmount();
                        final KualiDecimal encumbranceTaxAmount =
                                encumbranceQuantity.divide(poItem.getItemQuantity()).multiply(itemTaxAmount);
                        itemDisEncumber = itemDisEncumber.add(encumbranceTaxAmount);
                    } else {
                        LOG.debug("relieveEncumbrance() {} Calculate encumbrance based on amount", logItmNbr);

                        // Do encumbrance calculations based on amount only
                        if (poItem.getItemOutstandingEncumberedAmount().bigDecimalValue().signum() == -1
                                && preqItemTotalAmount.bigDecimalValue().signum() == -1) {
                            LOG.debug(
                                    "relieveEncumbrance() {} Outstanding Encumbered amount is negative: {}",
                                    () -> logItmNbr,
                                    poItem::getItemOutstandingEncumberedAmount
                            );

                            if (preqItemTotalAmount.compareTo(poItem.getItemOutstandingEncumberedAmount()) >= 0) {
                                // extended price is equal to or greater than outstanding encumbered
                                itemDisEncumber = preqItemTotalAmount;
                            } else {
                                // extended price is less than outstanding encumbered
                                takeAll = true;
                                itemDisEncumber = poItem.getItemOutstandingEncumberedAmount();
                            }
                        } else {
                            LOG.debug(
                                    "relieveEncumbrance() {} Outstanding Encumbered amount is positive or ZERO: {}",
                                    () -> logItmNbr,
                                    poItem::getItemOutstandingEncumberedAmount
                            );

                            if (poItem.getItemOutstandingEncumberedAmount().compareTo(preqItemTotalAmount) >= 0) {
                                // outstanding amount is equal to or greater than extended price
                                itemDisEncumber = preqItemTotalAmount;
                            } else {
                                // outstanding amount is less than extended price
                                takeAll = true;
                                itemDisEncumber = poItem.getItemOutstandingEncumberedAmount();
                            }
                        }
                    }

                    LOG.debug("relieveEncumbrance() {} Amount to disencumber: {}", logItmNbr, itemDisEncumber);

                    final KualiDecimal newOutstandingEncumberedAmount = poItem.getItemOutstandingEncumberedAmount()
                            .subtract(itemDisEncumber);
                    LOG.debug(
                            "relieveEncumbrance() {} New Outstanding Encumbered amount is : {}",
                            logItmNbr,
                            newOutstandingEncumberedAmount
                    );

                    poItem.setItemOutstandingEncumberedAmount(newOutstandingEncumberedAmount);

                    final KualiDecimal newInvoicedTotalAmount = poItem.getItemInvoicedTotalAmount().add(preqItemTotalAmount);
                    LOG.debug(
                            "relieveEncumbrance() {} New Invoiced Total Amount is: {}",
                            logItmNbr,
                            newInvoicedTotalAmount
                    );

                    poItem.setItemInvoicedTotalAmount(newInvoicedTotalAmount);

                    Collections.sort((List) poItem.getSourceAccountingLines());

                    // make the list of accounts for the disencumbrance entry
                    PurchaseOrderAccount lastAccount = null;
                    KualiDecimal accountTotal = KualiDecimal.ZERO;
                    for (final PurApAccountingLine purApAccountingLine : poItem.getSourceAccountingLines()) {
                        final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                        if (!account.isEmpty()) {
                            final KualiDecimal encumbranceAmount;
                            final SourceAccountingLine acctString = account.generateSourceAccountingLine();
                            if (takeAll) {
                                // fully paid; remove remaining encumbrance
                                encumbranceAmount = account.getItemAccountOutstandingEncumbranceAmount();
                                account.setItemAccountOutstandingEncumbranceAmount(KualiDecimal.ZERO);
                                LOG.debug("relieveEncumbrance() {} take all", logItmNbr);
                            } else {
                                // amount = item disencumber * account percent / 100
                                encumbranceAmount = itemDisEncumber
                                        .multiply(new KualiDecimal(account.getAccountLinePercent().toString()))
                                        .divide(PurapConstants.HUNDRED);

                                account.setItemAccountOutstandingEncumbranceAmount(
                                        account.getItemAccountOutstandingEncumbranceAmount()
                                                .subtract(encumbranceAmount));

                                // For rounding check at the end
                                accountTotal = accountTotal.add(encumbranceAmount);

                                // If we are zeroing out the encumbrance, we don't need to adjust for rounding
                                if (!takeAll) {
                                    lastAccount = account;
                                }
                            }

                            LOG.debug("relieveEncumbrance() {} {} = {}", logItmNbr, acctString, encumbranceAmount);

                            if (ObjectUtils.isNull(encumbranceAccountMap.get(acctString))) {
                                encumbranceAccountMap.put(acctString, encumbranceAmount);
                            } else {
                                final KualiDecimal amt = encumbranceAccountMap.get(acctString);
                                encumbranceAccountMap.put(acctString, amt.add(encumbranceAmount));
                            }
                        }
                    }

                    // account for rounding by adjusting last account as needed
                    if (lastAccount != null) {
                        final KualiDecimal difference = itemDisEncumber.subtract(accountTotal);
                        LOG.debug("relieveEncumbrance() difference: {} {}", logItmNbr, difference);

                        final SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                        final KualiDecimal amount = encumbranceAccountMap.get(acctString);
                        if (ObjectUtils.isNull(amount)) {
                            encumbranceAccountMap.put(acctString, difference);
                        } else {
                            encumbranceAccountMap.put(acctString, amount.add(difference));
                        }

                        lastAccount.setItemAccountOutstandingEncumbranceAmount(
                                lastAccount.getItemAccountOutstandingEncumbranceAmount().subtract(difference));
                    }
                }
            }
        }

        final List<SourceAccountingLine> encumbranceAccounts = new ArrayList<>();
        for (final SourceAccountingLine acctString : encumbranceAccountMap.keySet()) {
            final KualiDecimal amount = encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        businessObjectService.save(po);
        return encumbranceAccounts;
    }

    /**
     * Re-encumber the Encumbrance on a PO based on values in a PREQ. This is used when a PREQ is cancelled.
     * Note: This modifies the encumbrance values on the PO and saves the PO
     *
     * @param preq PREQ for invoice
     * @return List of accounting lines to use to create the pending general ledger entries
     */
    protected List<SourceAccountingLine> reencumberEncumbrance(final PaymentRequestDocument preq) {
        LOG.debug("reencumberEncumbrance() started");

        final PurchaseOrderDocument po = purchaseOrderService.getCurrentPurchaseOrder(preq.getPurchaseOrderIdentifier());
        final Map<SourceAccountingLine, KualiDecimal> encumbranceAccountMap = new HashMap<>();

        for (final Object item : preq.getItems()) {
            final PaymentRequestItem payRequestItem = (PaymentRequestItem) item;
            final PurchaseOrderItem poItem = getPoItem(po, payRequestItem.getItemLineNumber(), payRequestItem.getItemType());

            // Amount to reencumber for this item
            KualiDecimal itemReEncumber;

            final String logItmNbr = "Item # " + payRequestItem.getItemLineNumber();
            LOG.debug("reencumberEncumbrance() {}", logItmNbr);

            // If there isn't a PO item or the total amount is 0, we don't need encumbrances
            final KualiDecimal preqItemTotalAmount = payRequestItem.getTotalAmount() == null ? KualiDecimal.ZERO :
                    payRequestItem.getTotalAmount();
            if (poItem == null || preqItemTotalAmount.doubleValue() == 0) {
                LOG.debug("reencumberEncumbrance() {} No encumbrances required", logItmNbr);
            } else {
                LOG.debug("reencumberEncumbrance() {} Calculate encumbrance GL entries", logItmNbr);

                // Do we calculate the encumbrance amount based on quantity or amount?
                if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    LOG.debug("reencumberEncumbrance() {} Calculate encumbrance based on quantity", logItmNbr);

                    // Do disencumbrance calculations based on quantity
                    final KualiDecimal preqQuantity = payRequestItem.getItemQuantity() == null ? KualiDecimal.ZERO :
                            payRequestItem.getItemQuantity();
                    final KualiDecimal outstandingEncumberedQuantity =
                            poItem.getItemOutstandingEncumberedQuantity() == null ? KualiDecimal.ZERO :
                                    poItem.getItemOutstandingEncumberedQuantity();
                    final KualiDecimal invoicedTotal = poItem.getItemInvoicedTotalQuantity() == null ? KualiDecimal.ZERO :
                            poItem.getItemInvoicedTotalQuantity();

                    poItem.setItemInvoicedTotalQuantity(invoicedTotal.subtract(preqQuantity));
                    poItem.setItemOutstandingEncumberedQuantity(outstandingEncumberedQuantity.add(preqQuantity));

                    //do math as big decimal as doing it as a KualiDecimal will cause the item price to round to 2
                    // digits
                    itemReEncumber = new KualiDecimal(preqQuantity.bigDecimalValue()
                            .multiply(poItem.getItemUnitPrice()));

                    //add tax for encumbrance
                    final KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? KualiDecimal.ZERO :
                            poItem.getItemTaxAmount();
                    final KualiDecimal encumbranceTaxAmount = preqQuantity.divide(poItem.getItemQuantity())
                            .multiply(itemTaxAmount);
                    itemReEncumber = itemReEncumber.add(encumbranceTaxAmount);
                } else {
                    LOG.debug("reencumberEncumbrance() {} Calculate encumbrance based on amount", logItmNbr);

                    itemReEncumber = preqItemTotalAmount;
                    // if re-encumber amount is more than original PO ordered amount... do not exceed ordered amount
                    // this prevents negative encumbrance
                    if (poItem.getTotalAmount() != null && poItem.getTotalAmount().bigDecimalValue().signum() < 0) {
                        // po item extended cost is negative
                        if (poItem.getTotalAmount().compareTo(itemReEncumber) > 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    } else if (poItem.getTotalAmount() != null
                            && poItem.getTotalAmount().bigDecimalValue().signum() >= 0) {
                        // po item extended cost is positive
                        if (poItem.getTotalAmount().compareTo(itemReEncumber) < 0) {
                            itemReEncumber = poItem.getTotalAmount();
                        }
                    }
                }

                LOG.debug("reencumberEncumbrance() {} Amount to reencumber: {}", logItmNbr, itemReEncumber);

                final KualiDecimal outstandingEncumberedAmount = poItem.getItemOutstandingEncumberedAmount() == null ?
                        KualiDecimal.ZERO : poItem.getItemOutstandingEncumberedAmount();
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

                final KualiDecimal invoicedTotalAmount = poItem.getItemInvoicedTotalAmount() == null ? KualiDecimal.ZERO :
                        poItem.getItemInvoicedTotalAmount();
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
                KualiDecimal accountTotal = KualiDecimal.ZERO;

                Collections.sort((List) poItem.getSourceAccountingLines());

                for (final PurApAccountingLine purApAccountingLine : poItem.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    if (!account.isEmpty()) {
                        final SourceAccountingLine acctString = account.generateSourceAccountingLine();

                        // amount = item reencumber * account percent / 100
                        final KualiDecimal reencumbranceAmount = itemReEncumber
                                .multiply(new KualiDecimal(account.getAccountLinePercent().toString()))
                                .divide(PurapConstants.HUNDRED);

                        account.setItemAccountOutstandingEncumbranceAmount(
                                account.getItemAccountOutstandingEncumbranceAmount().add(reencumbranceAmount));

                        // For rounding check at the end
                        accountTotal = accountTotal.add(reencumbranceAmount);

                        lastAccount = account;

                        LOG.debug("reencumberEncumbrance() {} {} = {}", logItmNbr, acctString, reencumbranceAmount);

                        if (encumbranceAccountMap.containsKey(acctString)) {
                            final KualiDecimal currentAmount = encumbranceAccountMap.get(acctString);
                            encumbranceAccountMap.put(acctString, reencumbranceAmount.add(currentAmount));
                        } else {
                            encumbranceAccountMap.put(acctString, reencumbranceAmount);
                        }
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemReEncumber.subtract(accountTotal);
                    LOG.debug("reencumberEncumbrance() difference: {} {}", logItmNbr, difference);

                    final SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                    final KualiDecimal amount = encumbranceAccountMap.get(acctString);
                    if (amount == null) {
                        encumbranceAccountMap.put(acctString, difference);
                    } else {
                        encumbranceAccountMap.put(acctString, amount.add(difference));
                    }
                    lastAccount.setItemAccountOutstandingEncumbranceAmount(
                            lastAccount.getItemAccountOutstandingEncumbranceAmount().add(difference));
                }
            }
        }

        businessObjectService.save(po);

        final List<SourceAccountingLine> encumbranceAccounts = new ArrayList<>();
        for (final SourceAccountingLine acctString : encumbranceAccountMap.keySet()) {
            final KualiDecimal amount = encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        return encumbranceAccounts;
    }

    /**
     * Re-encumber the Encumbrance on a PO based on values in a PREQ. This is used when a PREQ is cancelled.
     * Note: This modifies the encumbrance values on the PO and saves the PO
     *
     * @param cm Credit Memo document
     * @param po Purchase Order document modify encumbrances
     * @return List of accounting lines to use to create the pending general ledger entries
     */
    protected List<SourceAccountingLine> getCreditMemoEncumbrance(
            final VendorCreditMemoDocument cm,
            final PurchaseOrderDocument po, final boolean cancel) {
        LOG.debug("getCreditMemoEncumbrance() started");

        if (ObjectUtils.isNull(po)) {
            return null;
        }

        if (cancel) {
            LOG.debug("getCreditMemoEncumbrance() Receiving items back from vendor (cancelled CM)");
        } else {
            LOG.debug("getCreditMemoEncumbrance() Returning items to vendor");
        }

        final Map<SourceAccountingLine, KualiDecimal> encumbranceAccountMap = new HashMap<>();

        // Get each item one by one
        for (final Object item : cm.getItems()) {
            final CreditMemoItem cmItem = (CreditMemoItem) item;
            final PurchaseOrderItem poItem = getPoItem(po, cmItem.getItemLineNumber(), cmItem.getItemType());

            // Amount to disencumber for this item
            KualiDecimal itemDisEncumber;
            // Amount to alter the invoicedAmt on the PO item
            KualiDecimal itemAlterInvoiceAmt;

            final String logItmNbr = "Item # " + cmItem.getItemLineNumber();
            LOG.debug("getCreditMemoEncumbrance() {}", logItmNbr);

            final KualiDecimal cmItemTotalAmount =
                    cmItem.getTotalAmount() == null ? KualiDecimal.ZERO : cmItem.getTotalAmount();
            // If there isn't a PO item or the total amount is 0, we don't need encumbrances
            if (poItem == null || cmItemTotalAmount == null || cmItemTotalAmount.doubleValue() == 0) {
                LOG.debug("getCreditMemoEncumbrance() {} No encumbrances required", logItmNbr);
            } else {
                LOG.debug("getCreditMemoEncumbrance() {} Calculate encumbrance GL entries", logItmNbr);

                // Do we calculate the encumbrance amount based on quantity or amount?
                if (poItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    LOG.debug("getCreditMemoEncumbrance() {} Calculate encumbrance based on quantity", logItmNbr);

                    // Do encumbrance calculations based on quantity
                    final KualiDecimal cmQuantity = cmItem.getItemQuantity() == null ? KualiDecimal.ZERO :
                            cmItem.getItemQuantity();

                    final KualiDecimal encumbranceQuantityChange = calculateQuantityChange(cancel, poItem, cmQuantity);

                    LOG.debug(
                            "getCreditMemoEncumbrance() {} encumbranceQtyChange {} outstandingEncumberedQty {} invoicedTotalQuantity {}",
                            () -> logItmNbr,
                            () -> encumbranceQuantityChange,
                            poItem::getItemOutstandingEncumberedQuantity,
                            poItem::getItemInvoicedTotalQuantity
                    );

                    //do math as big decimal as doing it as a KualiDecimal will cause the item price to round to 2
                    // digits
                    itemDisEncumber = new KualiDecimal(encumbranceQuantityChange.bigDecimalValue()
                            .multiply(poItem.getItemUnitPrice()));

                    //add tax for encumbrance
                    final KualiDecimal itemTaxAmount = poItem.getItemTaxAmount() == null ? KualiDecimal.ZERO :
                            poItem.getItemTaxAmount();
                    final KualiDecimal encumbranceTaxAmount = encumbranceQuantityChange.divide(poItem.getItemQuantity())
                            .multiply(itemTaxAmount);
                    itemDisEncumber = itemDisEncumber.add(encumbranceTaxAmount);

                    itemAlterInvoiceAmt = cmItemTotalAmount;
                    if (cancel) {
                        itemAlterInvoiceAmt = itemAlterInvoiceAmt.multiply(new KualiDecimal("-1"));
                    }
                } else {
                    LOG.debug("getCreditMemoEncumbrance() {} Calculate encumbrance based on amount", logItmNbr);

                    // Do encumbrance calculations based on amount only
                    if (cancel) {
                        // Decrease encumbrance
                        itemDisEncumber = cmItemTotalAmount.multiply(new KualiDecimal("-1"));

                        if (poItem.getItemOutstandingEncumberedAmount().add(itemDisEncumber).doubleValue() < 0) {
                            LOG.debug("getCreditMemoEncumbrance() Cancel overflow");
                            itemDisEncumber = poItem.getItemOutstandingEncumberedAmount();
                        }
                    } else {
                        // Increase encumbrance
                        itemDisEncumber = cmItemTotalAmount;

                        if (poItem.getItemOutstandingEncumberedAmount().add(itemDisEncumber).doubleValue() >
                                poItem.getTotalAmount().doubleValue()) {
                            LOG.debug("getCreditMemoEncumbrance() Create overflow");

                            itemDisEncumber = poItem.getTotalAmount()
                                    .subtract(poItem.getItemOutstandingEncumberedAmount());
                        }
                    }
                    itemAlterInvoiceAmt = itemDisEncumber;
                }

                // alter the encumbrance based on what was originally encumbered
                poItem.setItemOutstandingEncumberedAmount(poItem.getItemOutstandingEncumberedAmount()
                        .add(itemDisEncumber));

                // alter the invoiced amt based on what was actually credited on the credit memo
                poItem.setItemInvoicedTotalAmount(poItem.getItemInvoicedTotalAmount().subtract(itemAlterInvoiceAmt));
                if (poItem.getItemInvoicedTotalAmount().compareTo(KualiDecimal.ZERO) < 0) {
                    poItem.setItemInvoicedTotalAmount(KualiDecimal.ZERO);
                }

                LOG.debug("getCreditMemoEncumbrance() {} Amount to disencumber: {}", logItmNbr, itemDisEncumber);

                Collections.sort((List) poItem.getSourceAccountingLines());

                // make the list of accounts for the disencumbrance entry
                PurchaseOrderAccount lastAccount = null;
                KualiDecimal accountTotal = KualiDecimal.ZERO;

                for (final PurApAccountingLine purApAccountingLine : poItem.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    if (!account.isEmpty()) {
                        final KualiDecimal encumbranceAmount;

                        final SourceAccountingLine acctString = account.generateSourceAccountingLine();
                        // amount = item disencumber * account percent / 100
                        encumbranceAmount = itemDisEncumber
                                .multiply(new KualiDecimal(account.getAccountLinePercent().toString()))
                                .divide(new KualiDecimal(100));

                        account.setItemAccountOutstandingEncumbranceAmount(
                                account.getItemAccountOutstandingEncumbranceAmount().add(encumbranceAmount));

                        // For rounding check at the end
                        accountTotal = accountTotal.add(encumbranceAmount);

                        lastAccount = account;

                        LOG.debug("getCreditMemoEncumbrance() {} {} = {}", logItmNbr, acctString, encumbranceAmount);

                        if (encumbranceAccountMap.get(acctString) == null) {
                            encumbranceAccountMap.put(acctString, encumbranceAmount);
                        } else {
                            final KualiDecimal amt = encumbranceAccountMap.get(acctString);
                            encumbranceAccountMap.put(acctString, amt.add(encumbranceAmount));
                        }
                    }
                }

                // account for rounding by adjusting last account as needed
                if (lastAccount != null) {
                    final KualiDecimal difference = itemDisEncumber.subtract(accountTotal);
                    LOG.debug("getCreditMemoEncumbrance() difference: {} {}", logItmNbr, difference);

                    final SourceAccountingLine acctString = lastAccount.generateSourceAccountingLine();
                    final KualiDecimal amount = encumbranceAccountMap.get(acctString);
                    if (amount == null) {
                        encumbranceAccountMap.put(acctString, difference);
                    } else {
                        encumbranceAccountMap.put(acctString, amount.add(difference));
                    }
                    lastAccount.setItemAccountOutstandingEncumbranceAmount(
                            lastAccount.getItemAccountOutstandingEncumbranceAmount().add(difference));
                }
            }
        }

        final List<SourceAccountingLine> encumbranceAccounts = new ArrayList<>();
        for (final SourceAccountingLine acctString : encumbranceAccountMap.keySet()) {
            final KualiDecimal amount = encumbranceAccountMap.get(acctString);
            if (amount.doubleValue() != 0) {
                acctString.setAmount(amount);
                encumbranceAccounts.add(acctString);
            }
        }

        businessObjectService.save(po);
        return encumbranceAccounts;
    }

    /**
     * Save the given general ledger entries
     *
     * @param glEntries List of GeneralLedgerPendingEntries to be saved
     */
    protected void saveGLEntries(final List<GeneralLedgerPendingEntry> glEntries) {
        LOG.debug("saveGLEntries() started");
        businessObjectService.save(glEntries);
    }

    /**
     * Save the given accounts for the given document.
     *
     * @param summaryAccounts         Accounts to be saved
     * @param purapDocumentIdentifier Purap document id for accounts
     */
    protected void saveAccountsPayableSummaryAccounts(
            final List<SummaryAccount> summaryAccounts,
            final Integer purapDocumentIdentifier, final String docType) {
        LOG.debug("saveAccountsPayableSummaryAccounts() started");
        purapAccountingService.deleteSummaryAccounts(purapDocumentIdentifier, docType);
        final List<AccountsPayableSummaryAccount> apSummaryAccounts = new ArrayList<>();
        for (final SummaryAccount summaryAccount : summaryAccounts) {
            apSummaryAccounts.add(new AccountsPayableSummaryAccount(
                    summaryAccount.getAccount(),
                    purapDocumentIdentifier,
                    docType,
                    dateTimeService.getCurrentTimestamp()));
        }
        businessObjectService.save(apSummaryAccounts);
    }

    /**
     * Find item in PO based on given parameters. Must send either the line # or item type.
     *
     * @param po       Purchase Order containing list of items
     * @param nbr      Line # of desired item (could be null)
     * @param itemType Item type of desired item
     * @return PurchaseOrderItem found matching given criteria
     */
    protected PurchaseOrderItem getPoItem(final PurchaseOrderDocument po, final Integer nbr, final ItemType itemType) {
        LOG.debug("getPoItem() started");
        for (final Object item : po.getItems()) {
            final PurchaseOrderItem element = (PurchaseOrderItem) item;
            if (itemType.isLineItemIndicator()) {
                if (ObjectUtils.isNotNull(nbr) && ObjectUtils.isNotNull(element.getItemLineNumber())
                        && nbr.compareTo(element.getItemLineNumber()) == 0) {
                    return element;
                }
            } else {
                if (element.getItemTypeCode().equals(itemType.getItemTypeCode())) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Format description for general ledger entry. Currently making sure length is less than 40 char.
     *
     * @param description String to be formatted
     * @return Formatted String
     */
    protected String entryDescription(final String description) {
        if (description != null && description.length() > 40) {
            return description.substring(0, 39);
        } else {
            return description;
        }
    }

    /**
     * Calculate quantity change for creating Credit Memo entries
     *
     * @param cancel     Boolean indicating whether entries are for creation or cancellation of credit memo
     * @param poItem     Purchase Order Item
     * @param cmQuantity Quantity on credit memo item
     * @return Calculated change
     */
    protected KualiDecimal calculateQuantityChange(final boolean cancel, final PurchaseOrderItem poItem, final KualiDecimal cmQuantity) {
        LOG.debug("calculateQuantityChange() started");

        // Calculate quantity change & adjust invoiced quantity & outstanding encumbered quantity
        KualiDecimal encumbranceQuantityChange;
        if (cancel) {
            encumbranceQuantityChange = cmQuantity.multiply(new KualiDecimal("-1"));
        } else {
            encumbranceQuantityChange = cmQuantity;
        }
        poItem.setItemInvoicedTotalQuantity(poItem.getItemInvoicedTotalQuantity()
                .subtract(encumbranceQuantityChange));
        poItem.setItemOutstandingEncumberedQuantity(poItem.getItemOutstandingEncumberedQuantity()
                .add(encumbranceQuantityChange));

        // Check for overflows
        if (cancel) {
            if (poItem.getItemOutstandingEncumberedQuantity().doubleValue() < 0) {
                LOG.debug("calculateQuantityChange() Cancel overflow");
                final KualiDecimal difference = poItem.getItemOutstandingEncumberedQuantity().abs();
                poItem.setItemOutstandingEncumberedQuantity(KualiDecimal.ZERO);
                poItem.setItemInvoicedTotalQuantity(poItem.getItemQuantity());
                encumbranceQuantityChange = encumbranceQuantityChange.add(difference);
            }
        } else {
            if (poItem.getItemInvoicedTotalQuantity().doubleValue() < 0) {
                LOG.debug("calculateQuantityChange() Create overflow");
                final KualiDecimal difference = poItem.getItemInvoicedTotalQuantity().abs();
                poItem.setItemOutstandingEncumberedQuantity(poItem.getItemQuantity());
                poItem.setItemInvoicedTotalQuantity(KualiDecimal.ZERO);
                encumbranceQuantityChange = encumbranceQuantityChange.add(difference);
            }
        }
        return encumbranceQuantityChange;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setGeneralLedgerPendingEntryService(final GeneralLedgerPendingEntryService generalLedgerPendingEntryService) {
        this.generalLedgerPendingEntryService = generalLedgerPendingEntryService;
    }

    public void setPurapAccountingService(final PurapAccountingService purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
    }

    public void setUniversityDateService(final UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setPurchaseOrderService(final PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setObjectCodeService(final ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public void setPaymentRequestService(final PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    public void setPurapAccountRevisionService(final PurapAccountRevisionService purapAccountRevisionService) {
        this.purapAccountRevisionService = purapAccountRevisionService;
    }
}
