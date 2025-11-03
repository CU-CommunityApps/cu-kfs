package edu.cornell.kfs.module.cam.batch.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.batch.ExtractProcessLog;
import org.kuali.kfs.module.cam.batch.service.ReconciliationService;
import org.kuali.kfs.module.cam.batch.service.impl.BatchExtractServiceImpl;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.businessobject.GlAccountLineGroup;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableItemAsset;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableLineAssetAccount;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Transactional;

public class CuBatchExtractServiceImpl extends BatchExtractServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    protected DataDictionaryService dataDictionaryService;

    /**
     * Retrieves a credit memo document for a specific document number
     * 
     * @param entry GL Line
     * @return CreditMemoDocument
     */
    @Override
    protected VendorCreditMemoDocument findCreditMemoDocument(final Entry entry) {
        VendorCreditMemoDocument creditMemoDocument = null;
        final Map<String, String> keys = new LinkedHashMap<>();
        keys.put(CamsPropertyConstants.DOCUMENT_NUMBER, entry.getDocumentNumber());
        final Class<? extends Document> docClass = dataDictionaryService.getDocumentClassByTypeName(PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT);
        
        final Collection<? extends Document> matchingCms = businessObjectService.findMatching(docClass, keys);
        if (matchingCms != null && matchingCms.size() == 1) {
            creditMemoDocument = (VendorCreditMemoDocument) matchingCms.iterator().next();
        }
        return creditMemoDocument;
    }

    /**
     * Retrieves a payment request document for a specific document number
     * 
     * @param entry GL Line
     * @return PaymentRequestDocument
     */
    @Override
    protected PaymentRequestDocument findPaymentRequestDocument(final Entry entry) {
        PaymentRequestDocument paymentRequestDocument = null;
        final Map<String, String> keys = new LinkedHashMap<>();
        keys.put(CamsPropertyConstants.DOCUMENT_NUMBER, entry.getDocumentNumber());
        final Class<? extends Document> docClass = dataDictionaryService.getDocumentClassByTypeName(PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
        
        final Collection<? extends Document> matchingPreqs = businessObjectService.findMatching(docClass, keys);
        if (matchingPreqs != null && matchingPreqs.size() == 1) {
            paymentRequestDocument = (PaymentRequestDocument) matchingPreqs.iterator().next();
        }
        return paymentRequestDocument;
    }

    /**
     * @see org.kuali.kfs.module.cam.batch.service.BatchExtractService#separatePOLines(java.util.List, java.util.List,
     *      java.util.Collection)
     */
    @Override
    public void separatePOLines(List<Entry> fpLines, List<Entry> purapLines, Collection<Entry> elgibleGLEntries) {
        for (Entry entry : elgibleGLEntries) {
            if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT_TYPES
                    .contains(entry.getFinancialDocumentTypeCode())) {
                purapLines.add(entry);
            } else if (!PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT
                    .equals(entry.getFinancialDocumentTypeCode())) {
                fpLines.add(entry);
            } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT
                    .equals(entry.getFinancialDocumentTypeCode())) {
                Map<String, String> fieldValues = new HashMap<>();
                fieldValues.put(CamsPropertyConstants.GeneralLedgerEntry.DOCUMENT_NUMBER, entry.getDocumentNumber());
                Class<? extends Document> docClass = dataDictionaryService.getDocumentClassByTypeName(PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT);
                // check if vendor credit memo, then include as FP line
                Collection<? extends Document> matchingCreditMemos = businessObjectService.findMatching(docClass, fieldValues);
                for (Document document : matchingCreditMemos) {
                    VendorCreditMemoDocument creditMemoDocument = (VendorCreditMemoDocument) document;
                    if (creditMemoDocument.getPurchaseOrderIdentifier() == null) {
                        fpLines.add(entry);
                    } else {
                        purapLines.add(entry);
                    }
                }
            }
        }
    }

    /**
     * @see org.kuali.kfs.module.cam.batch.service.BatchExtractService#savePOLines(List, ExtractProcessLog)
     */
    @Transactional
    @Override
    public HashSet<PurchasingAccountsPayableDocument> savePOLines(final List<Entry> poLines, final ExtractProcessLog processLog) {
        final HashSet<PurchasingAccountsPayableDocument> purApDocuments = new HashSet<>();

        // This is a list of pending GL entries created after last GL process and Cab Batch extract
        // PurAp Account Line history comes from PURAP module
        final Collection<PurApAccountingLineBase> purapAcctLines = findPurapAccountRevisions();

        final ReconciliationService reconciliationService = getReconciliationService();
        
        // Pass the records to reconciliation service method
        reconciliationService.reconcile(poLines, purapAcctLines);

        // for each valid GL entry there is a collection of valid PO Doc and Account Lines
        final Collection<GlAccountLineGroup> matchedGroups = reconciliationService.getMatchedGroups();

        // Keep track of unique item lines
        final HashMap<String, PurchasingAccountsPayableItemAsset> assetItems = new HashMap<>();

        // Keep track of unique account lines
        final HashMap<String, PurchasingAccountsPayableLineAssetAccount> assetAcctLines = new HashMap<>();

        // Keep track of asset lock
        final HashMap<String, Object> assetLockMap = new HashMap<>();

        // Keep track of purchaseOrderDocument
        final HashMap<Integer, PurchaseOrderDocument> poDocMap = new HashMap<>();

        // KFSMI-7214, add document map for processing multiple items from the same AP doc
        final HashMap<String, PurchasingAccountsPayableDocument> papdMap = new HashMap<>();

        for (final GlAccountLineGroup group : matchedGroups) {
            final Entry entry = group.getTargetEntry();
            GeneralLedgerEntry generalLedgerEntry = new GeneralLedgerEntry(entry);
            GeneralLedgerEntry debitEntry = null;
            GeneralLedgerEntry creditEntry = null;
            final KualiDecimal transactionLedgerEntryAmount = generalLedgerEntry.getTransactionLedgerEntryAmount();
            final List<PurApAccountingLineBase> matchedPurApAcctLines = group.getMatchedPurApAcctLines();
            final boolean hasPositiveAndNegative = hasPositiveAndNegative(matchedPurApAcctLines);
            final boolean nonZero = ObjectUtils.isNotNull(transactionLedgerEntryAmount) 
                    && transactionLedgerEntryAmount.isNonZero();

            // generally for non-zero transaction ledger amount we should create a single GL entry with that amount,
            if (nonZero && !hasPositiveAndNegative) {
                businessObjectService.save(generalLedgerEntry);
            } else {
                // but if there is FO revision or negative amount lines such as discount, create and save the set of
                // debit(positive) and credit(negative) entries initialized with zero transaction amounts
                debitEntry = createPositiveGlEntry(entry);
                businessObjectService.save(debitEntry);
                creditEntry = createNegativeGlEntry(entry);
                businessObjectService.save(creditEntry);
            }

            // KFSMI-7214, create an active document reference map
            boolean newApDoc = false;
            // KFSMI-7214, find from active document reference map first
            PurchasingAccountsPayableDocument cabPurapDoc = papdMap.get(entry.getDocumentNumber());
            if (ObjectUtils.isNull(cabPurapDoc)) {
                // find from DB
                cabPurapDoc = findPurchasingAccountsPayableDocument(entry);
            }

            // if document is found already, update the active flag
            if (ObjectUtils.isNull(cabPurapDoc)) {
                cabPurapDoc = createPurchasingAccountsPayableDocument(entry);
                newApDoc = true;
            }

            if (cabPurapDoc != null) {
                // KFSMI-7214, add to the cached document map
                papdMap.put(entry.getDocumentNumber(), cabPurapDoc);

                // we only deal with PREQ or CM, so isPREQ = !isCM, isCM = !PREQ
                final boolean isPREQ = PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT_TYPES.contains(entry.getFinancialDocumentTypeCode());
                final boolean hasRevisionWithMixedLines = isPREQ && hasRevisionWithMixedLines(matchedPurApAcctLines);

                for (final PurApAccountingLineBase purApAccountingLine : matchedPurApAcctLines) {
                    // KFSMI-7214,tracking down changes on CAB item.
                    boolean newAssetItem = false;

                    final PurApItem purapItem = purApAccountingLine.getPurapItem();
                    final String itemAssetKey = cabPurapDoc.getDocumentNumber() + "-" + purapItem.getItemIdentifier();

                    // KFSMI-7214, search CAB item from active object reference map first
                    PurchasingAccountsPayableItemAsset itemAsset = assetItems.get(itemAssetKey);

                    if (ObjectUtils.isNull(itemAsset)) {
                        itemAsset = findMatchingPurapAssetItem(cabPurapDoc, purapItem);
                    }

                    // if new item, create and add to the list
                    if (ObjectUtils.isNull(itemAsset)) {
                        itemAsset = createPurchasingAccountsPayableItemAsset(cabPurapDoc, purapItem);
                        cabPurapDoc.getPurchasingAccountsPayableItemAssets().add(itemAsset);
                        newAssetItem = true;
                    }

                    assetItems.put(itemAssetKey, itemAsset);
                    
                    Long generalLedgerAccountIdentifier = generalLedgerEntry.getGeneralLedgerAccountIdentifier();
                    final KualiDecimal purapAmount = purApAccountingLine.getAmount();
                    
                    // note that PurAp Doc accounting lines won't have zero amount, so !isPositive = isNegative
                    final boolean isPositive = purapAmount.isPositive();
                    // trade-in and discount items on PREQ usually have negative amount (unless it's a revision)
                    final boolean usuallyNegative = isItemTypeUsuallyOfNegativeAmount(purapItem.getItemTypeCode());

                    // decide if current accounting line should be consolidated into debit or credit entry based 
                    // on the above criteria
                    final boolean isDebitEntry = hasRevisionWithMixedLines ?
                            // case 2.2
                            usuallyNegative ? !isPositive : isPositive :
                            // case 1.1/1.2/2.1
                            isPREQ ? isPositive : !isPositive;
                    final GeneralLedgerEntry currentEntry = isDebitEntry ? debitEntry : creditEntry;
                    
                    if (ObjectUtils.isNull(generalLedgerAccountIdentifier)) {
                        generalLedgerAccountIdentifier = currentEntry.getGeneralLedgerAccountIdentifier();
                    }
                    
                    final String acctLineKey = cabPurapDoc.getDocumentNumber() + "-" + itemAsset.getAccountsPayableLineItemIdentifier() + "-" + itemAsset.getCapitalAssetBuilderLineNumber() + "-" + generalLedgerAccountIdentifier;
                    PurchasingAccountsPayableLineAssetAccount assetAccount = assetAcctLines.get(acctLineKey);

                    if (ObjectUtils.isNull(assetAccount) && nonZero && !hasPositiveAndNegative) {
                        // if new unique account line within GL, then create a new account line
                        assetAccount = createPurchasingAccountsPayableLineAssetAccount(generalLedgerEntry, cabPurapDoc, purApAccountingLine, itemAsset);
                        assetAcctLines.put(acctLineKey, assetAccount);
                        itemAsset.getPurchasingAccountsPayableLineAssetAccounts().add(assetAccount);
                    } else if (!nonZero || hasPositiveAndNegative) {
                        // if amount is zero, means canceled doc, then create a copy and retain the account line

                        /*
                         * KFSMI-9760 / KFSCNTRB-???(FSKD-5097)
                         * 1.   Usually, we consolidate matched accounting lines (for the same account) based on positive/negative amount, i.e.
                         * 1.1  For PREQ, positive -> debit, negative -> credit;
                         *      That means charges (positive amount) are debit, trade-ins/discounts (negative amount) are credit.
                         * 1.2. For CM, the opposite, positive -> credit, negative -> debit
                         *      That means payments (positive amount) are credit, Less Restocking Fees (negative amount) are debit.
                         * 2.   However when there is a FO revision on PREQ (CMs don't have revisions), it's more complicated:
                         * 2.1  If the matched accounting lines are either all for non trade-in/discount items, or all for trade-in/discount items,
                         *      then we still could base the debit/credit on positive/negative amount;
                         *      That means reverse of charges (negative amount) are credit, reverse of trade-ins/discounts (positive amount) are debit.
                         * 2.2  Otherwise, i.e. the matched accounting lines cover both non trade-in/discount items and trade-in/discount items,
                         *      In this case we prefer to consolidate based on revision,
                         *      that means the original charges and trade-in/discounts are combined together,
                         *      while the reversed charges and trade-in/discounts are combined together;
                         *      So: original charge + original trade-in/discount -> debit, reversed charge + reversed trade-in/discount -> credit
                         * 3.   On top of these, we ensure that the final capital asset GL entries created is a debit if the consolidated amount is positive, and vice versa.
                         *      Note: In general, the consolidated amount for debit entry should already be positive, and vice versa. But there could be special cases,
                         *      for ex, in the case of 2.2, if the revision is only on discount, then the credit entry for the reverse would come out as positive, so we need
                         *      to swap it into a debit entry. This means, we will have 2 debit entries, one for the original lines, the other for the reversed discount line.
                         */

                        // during calculation, regard D/C code as a +/- sign in front of the amount
                        KualiDecimal oldAmount = currentEntry.getTransactionLedgerEntryAmount();
                        oldAmount = isDebitEntry ? oldAmount : oldAmount.negated();
                        KualiDecimal newAmount = oldAmount.add(purapAmount);
                        newAmount = isDebitEntry ? newAmount : newAmount.negated();
                        currentEntry.setTransactionLedgerEntryAmount(newAmount);
                        
                        if (ObjectUtils.isNotNull(assetAccount)) {
                            // if account line key matches within same GL Entry, combine the amount
                            assetAccount.setItemAccountTotalAmount(assetAccount.getItemAccountTotalAmount()
                                    .add(purApAccountingLine.getAmount()));
                        } else {
                            assetAccount = createPurchasingAccountsPayableLineAssetAccount(currentEntry, cabPurapDoc,
                                    purApAccountingLine, itemAsset);
                            final int oldNumAssetAccounts =
                                    itemAsset.getPurchasingAccountsPayableLineAssetAccounts().size();
                            addOrUpdateAssetAccount(itemAsset, assetAccount);
                            final int newNumAssetAccounts =
                                    itemAsset.getPurchasingAccountsPayableLineAssetAccounts().size();
                            if (newNumAssetAccounts > oldNumAssetAccounts) {
                                assetAcctLines.put(acctLineKey, assetAccount);
                            }
                        }
                    } else if (ObjectUtils.isNotNull(assetAccount)) {
                        // if account line key matches within same GL Entry, combine the amount
                        assetAccount.setItemAccountTotalAmount(assetAccount.getItemAccountTotalAmount().add(purApAccountingLine.getAmount()));
                    }

                    // KFSMI-7214: fixed OJB auto-update object issue.
                    if (!newAssetItem) {
                        businessObjectService.save(itemAsset);
                    }

                    businessObjectService.save(cabPurapDoc);

                    // Add to the asset lock table if purap has asset number information
                    addAssetLocks(assetLockMap, cabPurapDoc, purapItem, 
                            itemAsset.getAccountsPayableLineItemIdentifier(), poDocMap);
                }

                // Update and save the debit/credit entry if needed;
                // Ensure that the entry always carries a positive TransactionLedgerEntryAmount,
                // otherwise need to swap the D/C code, (see item #3 in the above KFSMI-9760 / KFSCNTRB-???(FSKD-5097) comment)
                // since the real amount being positive/negative shall be solely indicated by the D/C code.
                if (debitEntry != null) {
                    final KualiDecimal amount = debitEntry.getTransactionLedgerEntryAmount();
                    if (amount.isNegative()) {
                        debitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                        debitEntry.setTransactionLedgerEntryAmount(amount.negated());
                    }
                    businessObjectService.save(debitEntry);
                }
                if (creditEntry != null) {
                    final KualiDecimal amount = creditEntry.getTransactionLedgerEntryAmount();
                    if (amount.isNegative()) {
                        creditEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                        creditEntry.setTransactionLedgerEntryAmount(amount.negated());
                    }
                    businessObjectService.save(creditEntry);
                }

                // Add to the doc collection which will be used for additional charge allocating. This will be the next step during
                // batch.
                if (newApDoc) {
                    purApDocuments.add(cabPurapDoc);
                }
            } else {
                LOG.error(
                        "Could not create a valid PurchasingAccountsPayableDocument object for document number {}",
                        entry::getDocumentNumber
                );
            }
        }
        updateProcessLog(processLog, getReconciliationService());
        return purApDocuments;
    }

    /*
     * Copied this private KualiCo method into our custom subclass.
     */
    private static void addOrUpdateAssetAccount(
            final PurchasingAccountsPayableItemAsset itemAsset,
            final PurchasingAccountsPayableLineAssetAccount assetAccount
    ) {
        final Optional<PurchasingAccountsPayableLineAssetAccount> matchingAssetAccount =
                itemAsset.getPurchasingAccountsPayableLineAssetAccounts()
                        .stream()
                        .filter(existingAssetAccount -> Objects.equals(existingAssetAccount.getDocumentNumber(),
                                assetAccount.getDocumentNumber()
                        ))
                        .filter(existingAssetAccount ->
                                Objects.equals(existingAssetAccount.getAccountsPayableLineItemIdentifier(),
                                assetAccount.getAccountsPayableLineItemIdentifier()
                        ))
                        .filter(existingAssetAccount ->
                                Objects.equals(existingAssetAccount.getCapitalAssetBuilderLineNumber(),
                                assetAccount.getCapitalAssetBuilderLineNumber()
                        ))
                        .filter(existingAssetAccount ->
                                Objects.equals(existingAssetAccount.getGeneralLedgerAccountIdentifier(),
                                assetAccount.getGeneralLedgerAccountIdentifier()
                        ))
                        .filter(existingAssetAccount ->
                                Objects.equals(existingAssetAccount.getItemAccountTotalAmount(),
                                assetAccount.getItemAccountTotalAmount()
                        ))
                        .findFirst();

        // Under rare circumstances (e.g. adding then removing sub-account on PREQ line), we may end up with a duplicate
        // assetAccount. Adding it to the collection will result in an OptimisticLockException (OLE) when the
        // itemAsset is saved. In order to avoid the OLE and get the correct amount, we update the existing assetAccount
        // instead of adding it again.
        if (matchingAssetAccount.isPresent()) {
            final PurchasingAccountsPayableLineAssetAccount purchasingAccountsPayableLineAssetAccount =
                    matchingAssetAccount.get();
            purchasingAccountsPayableLineAssetAccount.setItemAccountTotalAmount(
                    purchasingAccountsPayableLineAssetAccount.getItemAccountTotalAmount()
                            .add(assetAccount.getItemAccountTotalAmount()));
        } else {
            itemAsset.getPurchasingAccountsPayableLineAssetAccounts().add(assetAccount);
        }
    }

    /**
     * Returns true if the item type code is trade-in or discount, since items with these types usually have negative amounts.
     */
    private boolean isItemTypeUsuallyOfNegativeAmount(final String itemTypeCode) {
        return PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(itemTypeCode) ||
        PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(itemTypeCode) ||
        //TODO remove the following logic about MISC item when bug in KFSMI-10170 is fixed
        //MISC is included here temporarily for testing, since it's used as TRDI and ORDS, which don't work due to bug
        PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE.equals(itemTypeCode);
    }

    /**
     * Determines if the matched PurAp accounting lines have revisions and the account is used in both line items and trade-in/discount items.
     * If so, the trade-in/discount accounting lines need to be consolidated differently than simply by positive/negative amount.
     * Note: This method only applies to PREQ, since no revision could happen to CM.
     *
     * @param matchedPurApAcctLines List of matched PurAp accounting lines to check for multiple discount items
     * @return true if multiple discount items, false otherwise
     */
    private boolean hasRevisionWithMixedLines(final List<PurApAccountingLineBase> matchedPurApAcctLines) {
        boolean hasItemsUsuallyNegative = false;
        boolean hasOthers = false;
        boolean hasRevision = false;
        final HashSet<Integer> itemIdentifiers = new HashSet<>();

        for (final PurApAccountingLineBase purApAccountingLine : matchedPurApAcctLines) {
            final PurApItem purapItem = purApAccountingLine.getPurapItem();
            if (isItemTypeUsuallyOfNegativeAmount(purapItem.getItemTypeCode())) {
                hasItemsUsuallyNegative = true;
            } else {
                hasOthers = true;
            }
            // when we hit the same item twice within the matched lines, which share the same account, then we find a revision
            if (itemIdentifiers.contains(purApAccountingLine.getItemIdentifier())) {
                hasRevision = true;
            } else {
                itemIdentifiers.add(purApAccountingLine.getItemIdentifier());
            }
            if (hasRevision && hasItemsUsuallyNegative && hasOthers) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if there are both positive amounts AND negative amounts among the matched PurAp accounting lines.
     * This usually could happen in the following cases:
     * 1. The account was revised by Financial Officer: positive amount is for charge, negative amount is for refund;
     * 2. The account is for Trade-in or Discount item for PREQ, or Less Restocking Fee on Credit Memo.
     * 3. Both 1 and 2 happens at same time (for PREQ only; as for CM no revision could happen).
     *
     * @param matchedPurApAcctLines
     * @return
     */
    private boolean hasPositiveAndNegative(final List<PurApAccountingLineBase> matchedPurApAcctLines) {
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (final PurApAccountingLineBase line : matchedPurApAcctLines) {
            hasPositive = hasPositive || line.getAmount().isPositive();
            hasNegative = hasNegative || line.getAmount().isNegative();
            if (hasPositive && hasNegative) {
                return true;
            }
        }

        return false;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
    
    /**
     * The ReconciliationService bean is marked as a prototype because it has class variables that maintain state.
     * This is the easiest way to ensure that a new instance is obtained every time we need one. Ultimately, the
     * ReconsiliationService should probably be rewritten.
     *
     * @return a new instance of the ReconciliationService
     */
    private ReconciliationService getReconciliationService() {
        return SpringContext.getBean(ReconciliationService.class);
    }

}
