/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.module.cam.batch.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.integration.cam.CapitalAssetManagementModuleService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsParameterConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.batch.ExtractProcessLog;
import org.kuali.kfs.module.cam.batch.PreAssetTaggingStep;
import org.kuali.kfs.module.cam.batch.dataaccess.ExtractDao;
import org.kuali.kfs.module.cam.batch.dataaccess.PurchasingAccountsPayableItemAssetDao;
import org.kuali.kfs.module.cam.batch.service.BatchExtractService;
import org.kuali.kfs.module.cam.batch.service.ReconciliationService;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.BatchParameters;
import org.kuali.kfs.module.cam.businessobject.GeneralLedgerEntry;
import org.kuali.kfs.module.cam.businessobject.GlAccountLineGroup;
import org.kuali.kfs.module.cam.businessobject.Pretag;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableActionHistory;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableItemAsset;
import org.kuali.kfs.module.cam.businessobject.PurchasingAccountsPayableLineAssetAccount;
import org.kuali.kfs.module.cam.document.service.PurApInfoService;
import org.kuali.kfs.module.cam.document.service.PurApLineService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.CreditMemoAccountRevision;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccountRevision;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchExtractServiceImpl implements BatchExtractService {

    protected static final Logger LOG = LogManager.getLogger(BatchExtractServiceImpl.class);
    protected BusinessObjectService businessObjectService;
    protected ExtractDao extractDao;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected PurApLineService purApLineService;
    protected PurApInfoService purApInfoService;
    protected PurchasingAccountsPayableItemAssetDao purchasingAccountsPayableItemAssetDao;
    protected FinancialSystemDocumentService financialSystemDocumentService;
    protected CapitalAssetManagementModuleService capitalAssetManagementModuleService;

    @Override
    @Transactional
    public void performExtract(ExtractProcessLog processLog) {
        Collection<Entry> elgibleGLEntries = findElgibleGLEntries(processLog);

        if (elgibleGLEntries != null && !elgibleGLEntries.isEmpty()) {
            List<Entry> fpLines = new ArrayList<>();
            List<Entry> purapLines = new ArrayList<>();
            separatePOLines(fpLines, purapLines, elgibleGLEntries);
            // Save the Non-PO lines
            saveFPLines(fpLines, processLog);
            HashSet<PurchasingAccountsPayableDocument> purApDocuments = savePOLines(purapLines, processLog);

            // call allocate additional charges( not including trade-in lines) during batch. if comment out this line,
            // CAB users will see them on the screen and they will have to manually allocate the additional charge
            // lines.
            allocateAdditionalCharges(purApDocuments);

            // Set the log values
            processLog.setTotalGlCount(elgibleGLEntries.size());
            processLog.setNonPurApGlCount(fpLines.size());
            processLog.setPurApGlCount(purapLines.size());
            // Update the last extract time stamp
            updateLastExtractTime(processLog.getStartTime());
            if (LOG.isDebugEnabled()) {
                LOG.debug("CAB batch finished at " + dateTimeService.getCurrentTimestamp());
            }
            processLog.setFinishTime(dateTimeService.getCurrentTimestamp());
            processLog.setSuccess(true);
        } else {
            LOG.warn("****** No records processed during CAB Extract *******");
            processLog.setSuccess(false);
            processLog.setErrorMessage("No GL records were found for CAB processing.");
        }
    }

    @Override
    public void allocateAdditionalCharges(HashSet<PurchasingAccountsPayableDocument> purApDocuments) {
        List<PurchasingAccountsPayableActionHistory> actionsTakenHistory = new ArrayList<>();
        List<PurchasingAccountsPayableDocument> candidateDocs = new ArrayList<>();
        List<PurchasingAccountsPayableItemAsset> allocateTargetLines;
        List<PurchasingAccountsPayableItemAsset> initialItems = new ArrayList<>();
        boolean documentUpdated;

        for (PurchasingAccountsPayableDocument purApDoc : purApDocuments) {
            documentUpdated = false;
            // Refresh to get the referenced GLEntry BO. This is required to call purApLineService.processAllocate().
            Map<String, String> primaryKeys = new HashMap<>();
            primaryKeys.put(CamsPropertyConstants.PurchasingAccountsPayableDocument.DOCUMENT_NUMBER,
                    purApDoc.getDocumentNumber());
            // check if doc is already in CAB
            PurchasingAccountsPayableDocument cabPurapDoc = businessObjectService.findByPrimaryKey(
                    PurchasingAccountsPayableDocument.class, primaryKeys);

            candidateDocs.add(cabPurapDoc);
            // keep the original list of items for iteration since purApLineService.processAllocate() may remove the
            // line item when it's additional charges line.
            initialItems.addAll(cabPurapDoc.getPurchasingAccountsPayableItemAssets());

            // set additional charge indicator for each line item from PurAp. we need to set them before hand for use in
            // purApLineService.getAllocateTargetLines(), in which method to select line items as the target allocating lines.
            for (PurchasingAccountsPayableItemAsset initialItem : initialItems) {
                purApInfoService.setAccountsPayableItemsFromPurAp(initialItem, cabPurapDoc.getDocumentTypeCode());
            }

            // allocate additional charge lines if it's active line
            for (PurchasingAccountsPayableItemAsset allocateSourceLine : initialItems) {
                if (allocateSourceLine.isAdditionalChargeNonTradeInIndicator() && allocateSourceLine.isActive()) {
                    // get the allocate additional charge target lines.
                    allocateTargetLines = purApLineService.getAllocateTargetLines(allocateSourceLine, candidateDocs);
                    if (allocateTargetLines != null && !allocateTargetLines.isEmpty()) {
                        // setup the bidirectional relationship between objects.
                        setupObjectRelationship(candidateDocs);
                        purApLineService.processAllocate(allocateSourceLine, allocateTargetLines, actionsTakenHistory,
                                candidateDocs, true);
                        documentUpdated = true;
                    }
                }
            }

            if (documentUpdated) {
                businessObjectService.save(cabPurapDoc);
            }
            candidateDocs.clear();
            initialItems.clear();
        }
    }

    /**
     * Setup relationship from account to item and item to doc. In this way, we keep all working objects in the same
     * view as form.
     *
     * @param purApDocs
     */
    protected void setupObjectRelationship(List<PurchasingAccountsPayableDocument> purApDocs) {
        for (PurchasingAccountsPayableDocument purApDoc : purApDocs) {
            for (PurchasingAccountsPayableItemAsset item : purApDoc.getPurchasingAccountsPayableItemAssets()) {
                item.setPurchasingAccountsPayableDocument(purApDoc);
                for (PurchasingAccountsPayableLineAssetAccount account :
                        item.getPurchasingAccountsPayableLineAssetAccounts()) {
                    account.setPurchasingAccountsPayableItemAsset(item);
                }
            }
        }
    }

    /**
     * Creates a batch parameters object reading values from configured system parameters for CAB Extract
     *
     * @return BatchParameters
     */
    protected BatchParameters createCabBatchParameters() {
        BatchParameters parameters = new BatchParameters();
        parameters.setLastRunTime(getCabLastRunTimestamp());
        parameters.setIncludedFinancialBalanceTypeCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.BALANCE_TYPES));
        parameters.setIncludedFinancialObjectSubTypeCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.OBJECT_SUB_TYPES));
        parameters.setExcludedChartCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.CHARTS));
        parameters.setExcludedDocTypeCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.DOCUMENT_TYPES));
        parameters.setExcludedFiscalPeriods(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.FISCAL_PERIODS));
        parameters.setExcludedSubFundCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.SUB_FUND_GROUPS));
        return parameters;
    }

    /**
     * Creates a batch parameters object reading values from configured system parameters for Pre Tagging Extract
     *
     * @return BatchParameters
     */
    protected BatchParameters createPreTagBatchParameters() {
        BatchParameters parameters = new BatchParameters();
        parameters.setLastRunDate(getPreTagLastRunDate());
        parameters.setIncludedFinancialObjectSubTypeCodes(parameterService
                .getParameterValuesAsString(PreAssetTaggingStep.class, CamsParameterConstants.OBJECT_SUB_TYPES));
        parameters.setExcludedChartCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.CHARTS));
        parameters.setExcludedSubFundCodes(parameterService
                .getParameterValuesAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                        CamsParameterConstants.SUB_FUND_GROUPS));
        parameters.setCapitalizationLimitAmount(new BigDecimal(parameterService
                .getParameterValueAsString(AssetGlobal.class, CamsParameterConstants.CAPITALIZATION_LIMIT_AMOUNT)));
        return parameters;
    }

    /**
     * @param entry GL Line
     * @return CreditMemoDocument for a specific document number
     */
    protected VendorCreditMemoDocument findCreditMemoDocument(Entry entry) {
        VendorCreditMemoDocument creditMemoDocument = null;
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put(CamsPropertyConstants.DOCUMENT_NUMBER, entry.getDocumentNumber());
        Collection<VendorCreditMemoDocument> matchingCms = businessObjectService.findMatching(
                VendorCreditMemoDocument.class, keys);
        if (matchingCms != null && matchingCms.size() == 1) {
            creditMemoDocument = matchingCms.iterator().next();
        }
        return creditMemoDocument;
    }

    @Override
    public Collection<Entry> findElgibleGLEntries(ExtractProcessLog processLog) {
        BatchParameters parameters = createCabBatchParameters();
        processLog.setLastExtractTime(parameters.getLastRunTime());
        return extractDao.findMatchingGLEntries(parameters);
    }

    @Override
    public Collection<PurchaseOrderAccount> findPreTaggablePOAccounts() {
        BatchParameters parameters = createPreTagBatchParameters();
        return extractDao.findPreTaggablePOAccounts(parameters, getDocumentsNumbersAwaitingPurchaseOrderOpenStatus());
    }

    /**
     * @param entry GL Line
     * @return PaymentRequestDocument for a specific document number
     */
    protected PaymentRequestDocument findPaymentRequestDocument(Entry entry) {
        PaymentRequestDocument paymentRequestDocument = null;
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put(CamsPropertyConstants.DOCUMENT_NUMBER, entry.getDocumentNumber());
        Collection<PaymentRequestDocument> matchingPreqs = businessObjectService.findMatching(
                PaymentRequestDocument.class, keys);
        if (matchingPreqs != null && matchingPreqs.size() == 1) {
            paymentRequestDocument = matchingPreqs.iterator().next();
        }
        return paymentRequestDocument;
    }

    /**
     * @return Last run time stamp, if null then it returns yesterday
     */
    protected Timestamp getCabLastRunTimestamp() {
        Timestamp lastRunTime;
        String lastRunTS = parameterService.getParameterValueAsString(KfsParameterConstants.CAPITAL_ASSETS_BATCH.class,
                CamsParameterConstants.LAST_EXTRACT_TIME);

        java.util.Date yesterday = DateUtils.addDays(dateTimeService.getCurrentDate(), -1);
        try {
            lastRunTime = lastRunTS == null ? new Timestamp(yesterday.getTime()) :
                    new Timestamp(DateUtils.parseDate(lastRunTS,
                    CamsConstants.DateFormats.MONTH_DAY_YEAR + " " +
                            CamsConstants.DateFormats.MILITARY_TIME).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return lastRunTime;
    }

    /**
     * @return the last pre tag extract run date from system parameter
     */
    protected Date getPreTagLastRunDate() {
        Date lastRunDt;
        String lastRunTS = parameterService.getParameterValueAsString(PreAssetTaggingStep.class,
                CamsParameterConstants.LAST_EXTRACT_DATE);
        java.util.Date yesterday = DateUtils.addDays(dateTimeService.getCurrentDate(), -1);
        try {
            lastRunDt = lastRunTS == null ? new Date(yesterday.getTime()) :
                    new Date(DateUtils.parseDate(lastRunTS,
                    CamsConstants.DateFormats.MONTH_DAY_YEAR).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return lastRunDt;
    }

    @Override
    @Transactional
    public void saveFPLines(List<Entry> fpLines, ExtractProcessLog processLog) {
        for (Entry fpLine : fpLines) {
            // If entry is not duplicate, non-null and non-zero, then insert into CAB
            if (fpLine.getTransactionLedgerEntryAmount() == null || fpLine.getTransactionLedgerEntryAmount().isZero()) {
                // amount is zero or null
                processLog.addIgnoredGLEntry(fpLine);
            } else if (getReconciliationService().isDuplicateEntry(fpLine)) {
                // GL is duplicate
                processLog.addDuplicateGLEntry(fpLine);
            } else {
                GeneralLedgerEntry glEntry = new GeneralLedgerEntry(fpLine);
                businessObjectService.save(glEntry);
            }
        }
    }

    @Transactional
    @Override
    public HashSet<PurchasingAccountsPayableDocument> savePOLines(List<Entry> poLines, ExtractProcessLog processLog) {
        HashSet<PurchasingAccountsPayableDocument> purApDocuments = new HashSet<>();

        // This is a list of pending GL entries created after last GL process and Cab Batch extract
        // PurAp Account Line history comes from PURAP module
        Collection<PurApAccountingLineBase> purapAcctLines = findPurapAccountRevisions();

        ReconciliationService reconciliationService = getReconciliationService();

        // Pass the records to reconciliation service method
        reconciliationService.reconcile(poLines, purapAcctLines);

        // for each valid GL entry there is a collection of valid PO Doc and Account Lines
        Collection<GlAccountLineGroup> matchedGroups = reconciliationService.getMatchedGroups();

        // Keep track of unique item lines
        HashMap<String, PurchasingAccountsPayableItemAsset> assetItems = new HashMap<>();

        // Keep track of unique account lines
        HashMap<String, PurchasingAccountsPayableLineAssetAccount> assetAcctLines = new HashMap<>();

        // Keep track of asset lock
        HashMap<String, Object> assetLockMap = new HashMap<>();

        // Keep track of purchaseOrderDocument
        HashMap<Integer, PurchaseOrderDocument> poDocMap = new HashMap<>();

        // KFSMI-7214, add document map for processing multiple items from the same AP doc
        HashMap<String, PurchasingAccountsPayableDocument> papdMap = new HashMap<>();

        for (GlAccountLineGroup group : matchedGroups) {
            Entry entry = group.getTargetEntry();
            GeneralLedgerEntry generalLedgerEntry = new GeneralLedgerEntry(entry);
            GeneralLedgerEntry debitEntry = null;
            GeneralLedgerEntry creditEntry = null;
            KualiDecimal transactionLedgerEntryAmount = generalLedgerEntry.getTransactionLedgerEntryAmount();
            List<PurApAccountingLineBase> matchedPurApAcctLines = group.getMatchedPurApAcctLines();
            boolean hasPositiveAndNegative = hasPositiveAndNegative(matchedPurApAcctLines);
            boolean nonZero = ObjectUtils.isNotNull(transactionLedgerEntryAmount)
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
                boolean isPREQ = CamsConstants.PREQ.equals(entry.getFinancialDocumentTypeCode());
                boolean hasRevisionWithMixedLines = isPREQ && hasRevisionWithMixedLines(matchedPurApAcctLines);

                for (PurApAccountingLineBase purApAccountingLine : matchedPurApAcctLines) {
                    // KFSMI-7214,tracking down changes on CAB item.
                    boolean newAssetItem = false;

                    PurApItem purapItem = purApAccountingLine.getPurapItem();
                    String itemAssetKey = cabPurapDoc.getDocumentNumber() + "-" + purapItem.getItemIdentifier();

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
                    String acctLineKey = cabPurapDoc.getDocumentNumber() + "-" +
                            itemAsset.getAccountsPayableLineItemIdentifier() + "-" +
                            itemAsset.getCapitalAssetBuilderLineNumber() + "-" +
                            generalLedgerEntry.getGeneralLedgerAccountIdentifier();
                    PurchasingAccountsPayableLineAssetAccount assetAccount = assetAcctLines.get(acctLineKey);

                    if (ObjectUtils.isNull(assetAccount) && nonZero && !hasPositiveAndNegative) {
                        // if new unique account line within GL, then create a new account line
                        assetAccount = createPurchasingAccountsPayableLineAssetAccount(generalLedgerEntry,
                                cabPurapDoc, purApAccountingLine, itemAsset);
                        assetAcctLines.put(acctLineKey, assetAccount);
                        itemAsset.getPurchasingAccountsPayableLineAssetAccounts().add(assetAccount);
                    } else if (!nonZero || hasPositiveAndNegative) {
                        // if amount is zero, means canceled doc, then create a copy and retain the account line
                        KualiDecimal purapAmount = purApAccountingLine.getAmount();

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
                         * 3.   On top of these, we ensure that the final cab GL entries created is a debit if the consolidated amount is positive, and vice versa.
                         *      Note: In general, the consolidated amount for debit entry should already be positive, and vice versa. But there could be special cases,
                         *      for ex, in the case of 2.2, if the revision is only on discount, then the credit entry for the reverse would come out as positive, so we need
                         *      to swap it into a debit entry. This means, we will have 2 debit entries, one for the original lines, the other for the reversed discount line.
                         */

                        // note that PurAp Doc accounting lines won't have zero amount, so !isPositive = isNegative
                        boolean isPositive = purapAmount.isPositive();
                        // trade-in and discount items on PREQ usually have negative amount (unless it's a revision)
                        boolean usuallyNegative = isItemTypeUsuallyOfNegativeAmount(purapItem.getItemTypeCode());

                        // decide if current accounting line should be consolidated into debit or credit entry based
                        // on the above criteria
                        boolean isDebitEntry = hasRevisionWithMixedLines ?
                            // case 2.2
                            (usuallyNegative ? !isPositive : isPositive) :
                            // case 1.1/1.2/2.1
                            (isPREQ ? isPositive : !isPositive);
                        GeneralLedgerEntry currentEntry = isDebitEntry ? debitEntry : creditEntry;

                        // during calculation, regard D/C code as a +/- sign in front of the amount
                        KualiDecimal oldAmount = currentEntry.getTransactionLedgerEntryAmount();
                        oldAmount = isDebitEntry ? oldAmount : oldAmount.negated();
                        KualiDecimal newAmount = oldAmount.add(purapAmount);
                        newAmount = isDebitEntry ? newAmount : newAmount.negated();
                        currentEntry.setTransactionLedgerEntryAmount(newAmount);

                        assetAccount = createPurchasingAccountsPayableLineAssetAccount(currentEntry, cabPurapDoc,
                                purApAccountingLine, itemAsset);
                        itemAsset.getPurchasingAccountsPayableLineAssetAccounts().add(assetAccount);
                    } else if (ObjectUtils.isNotNull(assetAccount)) {
                        // if account line key matches within same GL Entry, combine the amount
                        assetAccount.setItemAccountTotalAmount(assetAccount.getItemAccountTotalAmount()
                                .add(purApAccountingLine.getAmount()));
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
                    KualiDecimal amount = debitEntry.getTransactionLedgerEntryAmount();
                    if (amount.isNegative()) {
                        debitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                        debitEntry.setTransactionLedgerEntryAmount(amount.negated());
                    }
                    businessObjectService.save(debitEntry);
                }
                if (creditEntry != null) {
                    KualiDecimal amount = creditEntry.getTransactionLedgerEntryAmount();
                    if (amount.isNegative()) {
                        creditEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                        creditEntry.setTransactionLedgerEntryAmount(amount.negated());
                    }
                    businessObjectService.save(creditEntry);
                }

                // Add to the doc collection which will be used for additional charge allocating. This will be the
                // next step during batch.
                if (newApDoc) {
                    purApDocuments.add(cabPurapDoc);
                }
            } else {
                LOG.error("Could not create a valid PurchasingAccountsPayableDocument object for document number " +
                        entry.getDocumentNumber());
            }
        }
        updateProcessLog(processLog, reconciliationService);
        return purApDocuments;
    }

    /**
     * @return true if the item type code is trade-in or discount, since items with these types usually have negative
     *         amounts.
     */
    private boolean isItemTypeUsuallyOfNegativeAmount(String itemTypeCode) {
        return PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(itemTypeCode) ||
            PurapConstants.ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(itemTypeCode) ||
            //TODO remove the following logic about MISC item when bug in KFSMI-10170 is fixed
            //MISC is included here temporarily for testing, since it's used as TRDI and ORDS, which don't work due to bug
            PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE.equals(itemTypeCode);
    }

    /**
     * Determines if the matched PurAp accounting lines have revisions and the account is used in both line items and
     * trade-in/discount items. If so, the trade-in/discount accounting lines need to be consolidated differently than
     * simply by positive/negative amount.
     * Note: This method only applies to PREQ, since no revision could happen to CM.
     *
     * @param matchedPurApAcctLines List of matched PurAp accounting lines to check for multiple discount items
     * @return true if multiple discount items, false otherwise
     */
    private boolean hasRevisionWithMixedLines(List<PurApAccountingLineBase> matchedPurApAcctLines) {
        boolean hasItemsUsuallyNegative = false;
        boolean hasOthers = false;
        boolean hasRevision = false;
        HashSet<Integer> itemIdentifiers = new HashSet<>();

        for (PurApAccountingLineBase purApAccountingLine : matchedPurApAcctLines) {
            PurApItem purapItem = purApAccountingLine.getPurapItem();
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
    private boolean hasPositiveAndNegative(List<PurApAccountingLineBase> matchedPurApAcctLines) {
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (PurApAccountingLineBase line : matchedPurApAcctLines) {
            hasPositive = hasPositive || line.getAmount().isPositive();
            hasNegative = hasNegative || line.getAmount().isNegative();
            if (hasPositive && hasNegative) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add asset lock to prohibit CAMS user modify the asset payment line.
     *
     * @param assetLockMap
     * @param cabPurapDoc
     * @param purapItem
     * @param accountsPaymentItemId
     * @param poDocMap
     */
    protected void addAssetLocks(HashMap<String, Object> assetLockMap, PurchasingAccountsPayableDocument cabPurapDoc,
            PurApItem purapItem, Integer accountsPaymentItemId, HashMap<Integer, PurchaseOrderDocument> poDocMap) {
        PurchaseOrderDocument purApdocument;
        if (poDocMap.containsKey(cabPurapDoc.getPurchaseOrderIdentifier())) {
            purApdocument = poDocMap.get(cabPurapDoc.getPurchaseOrderIdentifier());
        } else {
            purApdocument = purApInfoService.getCurrentDocumentForPurchaseOrderIdentifier(
                    cabPurapDoc.getPurchaseOrderIdentifier());
            poDocMap.put(cabPurapDoc.getPurchaseOrderIdentifier(), purApdocument);
        }
        String assetLockKey = cabPurapDoc.getDocumentNumber();
        // Only individual system will lock on item line number. other system will using preq/cm doc nbr as the locking
        // key
        String lockingInformation = null;
        if (PurapConstants.CapitalAssetTabStrings.INDIVIDUAL_ASSETS.equalsIgnoreCase(
                purApdocument.getCapitalAssetSystemTypeCode())) {
            lockingInformation = accountsPaymentItemId.toString();
            assetLockKey = cabPurapDoc.getDocumentNumber() + "-" + lockingInformation;
        }
        // set asset locks if the locks does not exist in HashMap and not in asset lock table either.
        if (!assetLockMap.containsKey(assetLockKey)
                && !capitalAssetManagementModuleService.isAssetLockedByCurrentDocument(cabPurapDoc.getDocumentNumber(),
                lockingInformation)) {
            // the below method need several PurAp service calls which may take long time to run.
            List capitalAssetNumbers = getAssetNumbersForLocking(purApdocument, purapItem);
            if (capitalAssetNumbers != null && !capitalAssetNumbers.isEmpty()) {
                boolean lockingResult = capitalAssetManagementModuleService.storeAssetLocks(capitalAssetNumbers,
                        cabPurapDoc.getDocumentNumber(), cabPurapDoc.getDocumentTypeCode(), lockingInformation);
                // add into cache
                assetLockMap.put(assetLockKey, lockingResult);
            } else {
                // remember the decision...
                assetLockMap.put(assetLockKey, false);
            }
        }
    }

    protected List getAssetNumbersForLocking(PurchaseOrderDocument purApdocument, PurApItem purapItem) {
        if (!PurapConstants.CapitalAssetSystemStates.MODIFY.equalsIgnoreCase(
                purApdocument.getCapitalAssetSystemStateCode())) {
            return null;
        }
        return purApInfoService.retrieveValidAssetNumberForLocking(purApdocument.getPurapDocumentIdentifier(),
                purApdocument.getCapitalAssetSystemTypeCode(), purapItem);
    }

    /**
     * Force created entry with zero transaction amount
     *
     * @param entry
     * @return
     */
    protected GeneralLedgerEntry createPositiveGlEntry(Entry entry) {
        GeneralLedgerEntry copyEntry = new GeneralLedgerEntry(entry);
        copyEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        copyEntry.setTransactionLedgerEntryAmount(KualiDecimal.ZERO);
        return copyEntry;
    }

    /**
     * Force created entry with zero transaction amount
     *
     * @param entry
     * @return
     */
    protected GeneralLedgerEntry createNegativeGlEntry(Entry entry) {
        GeneralLedgerEntry copyEntry = new GeneralLedgerEntry(entry);
        copyEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        copyEntry.setTransactionLedgerEntryAmount(KualiDecimal.ZERO);
        return copyEntry;
    }

    /**
     * Retrieves Payment Request Account History and Credit Memo account history, combines them into a single list
     */
    @Override
    public Collection<PurApAccountingLineBase> findPurapAccountRevisions() {
        Collection<PurApAccountingLineBase> purapAcctLines = new ArrayList<>();
        Collection<CreditMemoAccountRevision> cmAccountHistory =
                extractDao.findCreditMemoAccountRevisions(createCabBatchParameters());
        Collection<PaymentRequestAccountRevision> preqAccountHistory =
                extractDao.findPaymentRequestAccountRevisions(createCabBatchParameters());
        if (cmAccountHistory != null) {
            purapAcctLines.addAll(cmAccountHistory);
        }
        if (preqAccountHistory != null) {
            purapAcctLines.addAll(preqAccountHistory);
        }
        return purapAcctLines;
    }

    /**
     * Creates a new instance of PurchasingAccountsPayableLineAssetAccount using values provided from dependent objects
     *
     * @param generalLedgerEntry  General Ledger Entry record
     * @param cabPurapDoc         CAB PurAp Document
     * @param purApAccountingLine PurAp accounting line
     * @param itemAsset           CAB PurAp Item Asset
     * @return New PurchasingAccountsPayableLineAssetAccount
     */
    protected PurchasingAccountsPayableLineAssetAccount createPurchasingAccountsPayableLineAssetAccount(
            GeneralLedgerEntry generalLedgerEntry, PurchasingAccountsPayableDocument cabPurapDoc,
            PurApAccountingLineBase purApAccountingLine, PurchasingAccountsPayableItemAsset itemAsset) {
        PurchasingAccountsPayableLineAssetAccount assetAccount = new PurchasingAccountsPayableLineAssetAccount();
        assetAccount.setDocumentNumber(cabPurapDoc.getDocumentNumber());
        assetAccount.setAccountsPayableLineItemIdentifier(itemAsset.getAccountsPayableLineItemIdentifier());
        assetAccount.setCapitalAssetBuilderLineNumber(itemAsset.getCapitalAssetBuilderLineNumber());
        assetAccount.setGeneralLedgerAccountIdentifier(generalLedgerEntry.getGeneralLedgerAccountIdentifier());
        if (CamsConstants.CM.equals(generalLedgerEntry.getFinancialDocumentTypeCode())) {
            assetAccount.setItemAccountTotalAmount(purApAccountingLine.getAmount().negated());
        } else {
            assetAccount.setItemAccountTotalAmount(purApAccountingLine.getAmount());
        }
        assetAccount.setActivityStatusCode(CamsConstants.ActivityStatusCode.NEW);
        assetAccount.setVersionNumber(0L);
        return assetAccount;
    }

    /**
     * Updates the entries into process log
     *
     * @param processLog            Extract Process Log
     * @param reconciliationService Reconciliation Service data
     */
    protected void updateProcessLog(ExtractProcessLog processLog, ReconciliationService reconciliationService) {
        processLog.addIgnoredGLEntries(reconciliationService.getIgnoredEntries());
        processLog.addDuplicateGLEntries(reconciliationService.getDuplicateEntries());
        Collection<GlAccountLineGroup> misMatchedGroups = reconciliationService.getMisMatchedGroups();
        for (GlAccountLineGroup glAccountLineGroup : misMatchedGroups) {
            processLog.addMismatchedGLEntries(glAccountLineGroup.getSourceEntries());
        }
    }

    /**
     * Finds PurchasingAccountsPayableDocument using document number
     *
     * @param entry GL Entry
     * @return PurchasingAccountsPayableDocument
     */
    protected PurchasingAccountsPayableDocument findPurchasingAccountsPayableDocument(Entry entry) {
        Map<String, String> primaryKeys = new HashMap<>();
        primaryKeys.put(CamsPropertyConstants.PurchasingAccountsPayableDocument.DOCUMENT_NUMBER,
                entry.getDocumentNumber());
        // check if doc is already in CAB
        return businessObjectService.findByPrimaryKey(PurchasingAccountsPayableDocument.class, primaryKeys);
    }

    /**
     * Creates a new PurchasingAccountsPayableItemAsset using Purchasing Accounts payable item
     *
     * @param cabPurapDoc Cab Purap Document
     * @param apItem      Accounts Payable Item
     * @return PurchasingAccountsPayableItemAsset
     */
    protected PurchasingAccountsPayableItemAsset createPurchasingAccountsPayableItemAsset(
            PurchasingAccountsPayableDocument cabPurapDoc, PurApItem apItem) {
        PurchasingAccountsPayableItemAsset itemAsset = new PurchasingAccountsPayableItemAsset();
        itemAsset.setDocumentNumber(cabPurapDoc.getDocumentNumber());
        itemAsset.setAccountsPayableLineItemIdentifier(apItem.getItemIdentifier());
        // KFSMI-5337
        itemAsset.setCapitalAssetBuilderLineNumber(purchasingAccountsPayableItemAssetDao.findMaxCabLineNumber(
                cabPurapDoc.getDocumentNumber(), apItem.getItemIdentifier()) + 1);
        // replacing the above line by following code which can populate item description from PO for normal line item
        if (ObjectUtils.isNotNull(cabPurapDoc) && ObjectUtils.isNotNull(apItem.getItemType())
                && apItem.getItemType().isLineItemIndicator()) {
            PurchaseOrderDocument poDoc = purApInfoService.getCurrentDocumentForPurchaseOrderIdentifier(
                    cabPurapDoc.getPurchaseOrderIdentifier());
            // get corresponding poItem
            if (ObjectUtils.isNotNull(poDoc)) {
                PurApItem poItem = poDoc.getItemByLineNumber(apItem.getItemLineNumber());
                if (ObjectUtils.isNotNull(poItem)) {
                    itemAsset.setAccountsPayableLineItemDescription(poItem.getItemDescription());
                }
            }
        }

        itemAsset.setAccountsPayableItemQuantity(apItem.getItemQuantity() == null ? new KualiDecimal(1) :
                apItem.getItemQuantity());
        itemAsset.setActivityStatusCode(CamsConstants.ActivityStatusCode.NEW);
        itemAsset.setVersionNumber(0L);
        return itemAsset;
    }

    /**
     * This method creates PurchasingAccountsPayableDocument from a GL Entry and AP Document
     *
     * @param entry GL Entry
     * @return PurchasingAccountsPayableDocument
     */
    protected PurchasingAccountsPayableDocument createPurchasingAccountsPayableDocument(Entry entry) {
        AccountsPayableDocumentBase apDoc = null;
        PurchasingAccountsPayableDocument cabPurapDoc = null;
        // If document is not in CAB, create a new document to save in CAB
        if (CamsConstants.PREQ.equals(entry.getFinancialDocumentTypeCode())) {
            // find PREQ
            apDoc = findPaymentRequestDocument(entry);
        } else if (CamsConstants.CM.equals(entry.getFinancialDocumentTypeCode())) {
            // find CM
            apDoc = findCreditMemoDocument(entry);
        }
        if (apDoc == null) {
            LOG.error("A valid Purchasing Document (PREQ or CM) could not be found for this document number " +
                    entry.getDocumentNumber());
        } else {
            cabPurapDoc = new PurchasingAccountsPayableDocument();
            cabPurapDoc.setDocumentNumber(entry.getDocumentNumber());
            cabPurapDoc.setPurapDocumentIdentifier(apDoc.getPurapDocumentIdentifier());
            cabPurapDoc.setPurchaseOrderIdentifier(apDoc.getPurchaseOrderIdentifier());
            cabPurapDoc.setDocumentTypeCode(entry.getFinancialDocumentTypeCode());
            cabPurapDoc.setActivityStatusCode(CamsConstants.ActivityStatusCode.NEW);
            cabPurapDoc.setVersionNumber(0L);
        }
        return cabPurapDoc;
    }

    /**
     * Finds out the active CAB Asset Item matching the line from PurAP.
     *
     * @param cabPurapDoc CAB PurAp document
     * @param apItem      AP Item
     * @return PurchasingAccountsPayableItemAsset
     */
    protected PurchasingAccountsPayableItemAsset findMatchingPurapAssetItem(
            PurchasingAccountsPayableDocument cabPurapDoc, PurApItem apItem) {
        // KFSMI-7214: fixed OJB proxy object issue. Retrieve object from OJB cache instead of from table.
        if (ObjectUtils.isNotNull(cabPurapDoc)) {
            for (PurchasingAccountsPayableItemAsset assetItem : cabPurapDoc.getPurchasingAccountsPayableItemAssets()) {
                if (assetItem.getAccountsPayableLineItemIdentifier() != null
                        && assetItem.getAccountsPayableLineItemIdentifier().equals(apItem.getItemIdentifier())) {
                    // if still active and never split or submitted to CAMS
                    if (ObjectUtils.isNotNull(assetItem)
                            && CamsConstants.ActivityStatusCode.NEW.equalsIgnoreCase(
                                    assetItem.getActivityStatusCode())) {
                        // KFSMI-7214: return the proxy object if it's already loaded.
                        return assetItem;
                    }
                }
            }
        } else {
            LOG.error("expecting the CAB AP document not null");
        }

        return null;
    }

    @Override
    public void separatePOLines(List<Entry> fpLines, List<Entry> purapLines, Collection<Entry> eligibleGLEntries) {
        for (Entry entry : eligibleGLEntries) {
            if (CamsConstants.PREQ.equals(entry.getFinancialDocumentTypeCode())) {
                purapLines.add(entry);
            } else if (!CamsConstants.CM.equals(entry.getFinancialDocumentTypeCode())) {
                fpLines.add(entry);
            } else if (CamsConstants.CM.equals(entry.getFinancialDocumentTypeCode())) {
                Map<String, String> fieldValues = new HashMap<>();
                fieldValues.put(CamsPropertyConstants.GeneralLedgerEntry.DOCUMENT_NUMBER, entry.getDocumentNumber());
                // check if vendor credit memo, then include as FP line
                Collection<VendorCreditMemoDocument> matchingCreditMemos = businessObjectService.findMatching(
                        VendorCreditMemoDocument.class, fieldValues);
                for (VendorCreditMemoDocument creditMemoDocument : matchingCreditMemos) {
                    if (creditMemoDocument.getPurchaseOrderIdentifier() == null) {
                        fpLines.add(entry);
                    } else {
                        purapLines.add(entry);
                    }
                }
            }
        }
    }

    @Override
    public void updateLastExtractTime(Timestamp time) {
        Parameter parameter = parameterService.getParameter(CamsParameterConstants.NAMESPACE,
                CamsParameterConstants.DETAIL_TYPE_BATCH, CamsParameterConstants.LAST_EXTRACT_TIME);

        if (parameter != null) {
            SimpleDateFormat format = new SimpleDateFormat(CamsConstants.DateFormats.MONTH_DAY_YEAR + " " +
                    CamsConstants.DateFormats.MILITARY_TIME);

            Parameter.Builder updatedParameter = Parameter.Builder.create(parameter);
            updatedParameter.setValue(format.format(time));
            parameterService.updateParameter(updatedParameter.build());
        }
    }

    @Override
    @Transactional
    public void savePreTagLines(Collection<PurchaseOrderAccount> preTaggablePOAccounts) {
        HashSet<String> savedLines = new HashSet<>();
        for (PurchaseOrderAccount purchaseOrderAccount : preTaggablePOAccounts) {
            purchaseOrderAccount.refresh();
            PurchaseOrderItem purapItem = purchaseOrderAccount.getPurapItem();
            PurchaseOrderDocument purchaseOrder = purapItem.getPurchaseOrder();
            if (ObjectUtils.isNotNull(purchaseOrder)) {
                Integer poId = purchaseOrder.getPurapDocumentIdentifier();
                Integer itemLineNumber = purapItem.getItemLineNumber();
                if (poId != null && itemLineNumber != null) {
                    Map<String, Object> primaryKeys = new HashMap<>();
                    primaryKeys.put(CamsPropertyConstants.Pretag.PURCHASE_ORDER_NUMBER, poId);
                    primaryKeys.put(CamsPropertyConstants.Pretag.ITEM_LINE_NUMBER, itemLineNumber);
                    // check if already in pre-tag table
                    Pretag pretag = businessObjectService.findByPrimaryKey(Pretag.class, primaryKeys);
                    if (ObjectUtils.isNull(pretag) && savedLines.add("" + poId + "-" + itemLineNumber)) {
                        pretag = new Pretag();
                        pretag.setPurchaseOrderNumber(poId.toString());
                        pretag.setItemLineNumber(itemLineNumber);
                        KualiDecimal quantity = purapItem.getItemQuantity();
                        pretag.setQuantityInvoiced(quantity != null ? quantity : new KualiDecimal(1));
                        pretag.setVendorName(purchaseOrder.getVendorName());
                        pretag.setAssetTopsDescription(purapItem.getItemDescription());
                        pretag.setPretagCreateDate(new Date(purchaseOrder.getPurchaseOrderInitialOpenTimestamp()
                                .getTime()));
                        pretag.setChartOfAccountsCode(purchaseOrder.getChartOfAccountsCode());
                        pretag.setOrganizationCode(purchaseOrder.getOrganizationCode());
                        pretag.setActive(true);
                        businessObjectService.save(pretag);
                    }
                }
            }
        }
    }

    protected List<String> getDocumentsNumbersAwaitingPurchaseOrderOpenStatus() {
        List<String> poDocumentNumbers = new ArrayList<>();
        List<PurchaseOrderDocument> poDocuments;
        try {
            // This should pick up all types of POs (Amendments, Voids, etc)
            poDocuments = (List<PurchaseOrderDocument>) financialSystemDocumentService.findByApplicationDocumentStatus(
                PurchaseOrderDocument.class, CamsConstants.PO_STATUS_CODE_OPEN);
        } catch (WorkflowException we) {
            throw new RuntimeException(we);
        }
        for (PurchaseOrderDocument poDocument : poDocuments) {
            poDocumentNumbers.add(poDocument.getDocumentNumber());
        }
        return poDocumentNumbers;
    }

    @Override
    public void updateLastExtractDate(Date dt) {
        Parameter parameter = parameterService.getParameter(CamsParameterConstants.NAMESPACE,
                CamsParameterConstants.DETAIL_TYPE_PRE_ASSET_TAGGING_STEP, CamsParameterConstants.LAST_EXTRACT_DATE);

        if (parameter != null) {
            SimpleDateFormat format = new SimpleDateFormat(CamsConstants.DateFormats.MONTH_DAY_YEAR);

            Parameter.Builder updatedParameter = Parameter.Builder.create(parameter);
            updatedParameter.setValue(format.format(dt));
            parameterService.updateParameter(updatedParameter.build());
        }
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setExtractDao(ExtractDao extractDao) {
        this.extractDao = extractDao;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPurchasingAccountsPayableItemAssetDao(
            PurchasingAccountsPayableItemAssetDao purchasingAccountsPayableItemAssetDao) {
        this.purchasingAccountsPayableItemAssetDao = purchasingAccountsPayableItemAssetDao;
    }

    public void setPurApLineService(PurApLineService purApLineService) {
        this.purApLineService = purApLineService;
    }

    public void setPurApInfoService(PurApInfoService purApInfoService) {
        this.purApInfoService = purApInfoService;
    }

    /**
     * The ReconciliationService bean is marked as a prototype because it has class variables that maintain state.
     * This is the easiest way to ensure that a new instance is obtained every time we need one. Ultimately, the
     * ReconsiliationService should probably be rewritten.
     *
     * @return a new instance of the ReconciliationService
     */
    protected ReconciliationService getReconciliationService() {
        return SpringContext.getBean(ReconciliationService.class);
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setCapitalAssetManagementModuleService(
            CapitalAssetManagementModuleService capitalAssetManagementModuleService) {
        this.capitalAssetManagementModuleService = capitalAssetManagementModuleService;
    }
}
