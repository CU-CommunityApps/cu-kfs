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
package org.kuali.kfs.module.purap.document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.AccountsPayableItem;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.service.AccountsPayableDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.CampusParameter;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteLevelChange;

import java.sql.Timestamp;
import java.util.List;

public abstract class AccountsPayableDocumentBase extends PurchasingAccountsPayableDocumentBase implements
        AccountsPayableDocument {

    private static final Logger LOG = LogManager.getLogger();

    // SHARED FIELDS BETWEEN PAYMENT REQUEST AND CREDIT MEMO
    protected Timestamp accountsPayableApprovalTimestamp;
    protected String lastActionPerformedByPersonId;
    protected String accountsPayableProcessorIdentifier;
    protected boolean holdIndicator;
    protected Timestamp extractedTimestamp;
    protected Integer purchaseOrderIdentifier;
    protected String processingCampusCode;
    protected String noteLine1Text;
    protected String noteLine2Text;
    protected String noteLine3Text;
    protected boolean continuationAccountIndicator;
    protected boolean closePurchaseOrderIndicator;
    protected boolean reopenPurchaseOrderIndicator;
    protected String bankCode;

    // not persisted
    protected boolean unmatchedOverride;

    // NOT PERSISTED IN DB
    // BELOW USED BY ROUTING
    protected String chartOfAccountsCode;
    protected String organizationCode;

    // NOT PERSISTED IN DB
    // BELOW USED BY GL ENTRY CREATION
    protected boolean generateEncumbranceEntries;
    protected String debitCreditCodeForGLEntries;
    protected PurApItemUseTax offsetUseTax;

    // REFERENCE OBJECTS
    protected CampusParameter processingCampus;
    protected transient PurchaseOrderDocument purchaseOrderDocument;
    protected Bank bank;

    public AccountsPayableDocumentBase() {
        super();
        setUnmatchedOverride(false);
    }

    public void setLineItemTotal(final KualiDecimal total) {
        // do nothing, this is so that the jsp won't complain about lineItemTotal have no setter method.
    }

    public void setGrandTotal(final KualiDecimal total) {
        // do nothing, this is so that the jsp won't complain about grandTotal have no setter method.
    }

    /**
     * Overriding to stop the deleting of general ledger entries.
     */
    @Override
    protected void removeGeneralLedgerPendingEntries() {
        // do not delete entries for PREQ or CM (hjs)
    }

    @Override
    public boolean requiresAccountsPayableReviewRouting() {
        return !approvalAtAccountsPayableReviewAllowed();
    }

    @Override
    public boolean approvalAtAccountsPayableReviewAllowed() {
        return !(isAttachmentRequired() && documentHasNoImagesAttached());
    }

    /**
     * @return true if attachment is required, otherwise false
     */
    protected abstract boolean isAttachmentRequired();

    /**
     * Checks all documents notes for attachments and to be overridden by sub class
     *
     * @return true if document does not have an image attached, false otherwise
     */
    public abstract boolean documentHasNoImagesAttached();

    @Override
    public void populateDocumentForRouting() {
        if (ObjectUtils.isNotNull(getPurchaseOrderDocument())) {
            setChartOfAccountsCode(getPurchaseOrderDocument().getChartOfAccountsCode());
            setOrganizationCode(getPurchaseOrderDocument().getOrganizationCode());
            if (ObjectUtils.isNull(getPurchaseOrderDocument().getDocumentHeader().getDocumentNumber())) {
                getPurchaseOrderDocument().refreshReferenceObject(KFSPropertyConstants.DOCUMENT_HEADER);
            }
        }
        super.populateDocumentForRouting();
    }

    /**
     * Calls a custom prepare for save method, as the super class does GL entry creation that causes problems with AP
     * documents.
     */
    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        // copied from super because we can't call super for AP docs
        customPrepareForSave(event);

        // DO NOT CALL SUPER HERE!! Cannot call super because it will mess up the GL entry creation process (hjs)
        // super.prepareForSave(event);
    }

    /**
     * Helper method to be called from custom prepare for save and to be overridden by sub class.
     *
     * @return Po Document Type
     */
    public abstract String getPoDocumentTypeForAccountsPayableDocumentCancel();

    @Override
    public void doRouteLevelChange(final DocumentRouteLevelChange levelChangeEvent) {
        LOG.debug("handleRouteLevelChange() started");
        super.doRouteLevelChange(levelChangeEvent);

        //process node change for documents
        final String newNodeName = levelChangeEvent.getNewNodeName();
        processNodeChange(newNodeName, levelChangeEvent.getOldNodeName());

        // KFSMI-9715 - need to call this after processNodeChange, otherwise if PO is closed while processing PREQ
        // it gets saved before encumbrance is relieved, and the Total Encumbrance Amount Relieved and TotalPaidAmount
        // on the PREQ didn't reflect the invoice amount, and the amount paid on the PO wasn't being set correctly.
        saveDocumentFromPostProcessing();
    }

    /**
     * Hook to allow processing after a route level is passed.
     *
     * @param newNodeName current route level
     * @param oldNodeName previous route level
     * @return true if process completes to valid state
     */
    public abstract boolean processNodeChange(String newNodeName, String oldNodeName);

    /**
     * Hook point to allow processing after a save.
     */
    public abstract void saveDocumentFromPostProcessing();

    @Override
    public Integer getPurchaseOrderIdentifier() {
        return purchaseOrderIdentifier;
    }

    @Override
    public void setPurchaseOrderIdentifier(final Integer purchaseOrderIdentifier) {
        this.purchaseOrderIdentifier = purchaseOrderIdentifier;
    }

    @Override
    public String getAccountsPayableProcessorIdentifier() {
        return accountsPayableProcessorIdentifier;
    }

    @Override
    public void setAccountsPayableProcessorIdentifier(final String accountsPayableProcessorIdentifier) {
        this.accountsPayableProcessorIdentifier = accountsPayableProcessorIdentifier;
    }

    @Override
    public String getLastActionPerformedByPersonId() {
        return lastActionPerformedByPersonId;
    }

    @Override
    public void setLastActionPerformedByPersonId(final String lastActionPerformedByPersonId) {
        this.lastActionPerformedByPersonId = lastActionPerformedByPersonId;
    }

    @Override
    public String getProcessingCampusCode() {
        return processingCampusCode;
    }

    @Override
    public void setProcessingCampusCode(final String processingCampusCode) {
        this.processingCampusCode = processingCampusCode;
    }

    @Override
    public Timestamp getAccountsPayableApprovalTimestamp() {
        return accountsPayableApprovalTimestamp;
    }

    @Override
    public void setAccountsPayableApprovalTimestamp(final Timestamp accountsPayableApprovalTimestamp) {
        this.accountsPayableApprovalTimestamp = accountsPayableApprovalTimestamp;
    }

    @Override
    public Timestamp getExtractedTimestamp() {
        return extractedTimestamp;
    }

    @Override
    public void setExtractedTimestamp(final Timestamp extractedTimestamp) {
        this.extractedTimestamp = extractedTimestamp;
    }

    @Override
    public boolean isHoldIndicator() {
        return holdIndicator;
    }

    @Override
    public void setHoldIndicator(final boolean holdIndicator) {
        this.holdIndicator = holdIndicator;
    }

    @Override
    public String getNoteLine1Text() {
        return noteLine1Text;
    }

    @Override
    public void setNoteLine1Text(final String noteLine1Text) {
        this.noteLine1Text = noteLine1Text;
    }

    @Override
    public String getNoteLine2Text() {
        return noteLine2Text;
    }

    @Override
    public void setNoteLine2Text(final String noteLine2Text) {
        this.noteLine2Text = noteLine2Text;
    }

    @Override
    public String getNoteLine3Text() {
        return noteLine3Text;
    }

    @Override
    public void setNoteLine3Text(final String noteLine3Text) {
        this.noteLine3Text = noteLine3Text;
    }

    @Override
    public CampusParameter getProcessingCampus() {
        return processingCampus;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(final String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public boolean isGenerateEncumbranceEntries() {
        return generateEncumbranceEntries;
    }

    public void setGenerateEncumbranceEntries(final boolean generateEncumbranceEntries) {
        this.generateEncumbranceEntries = generateEncumbranceEntries;
    }

    @Override
    public PurchaseOrderDocument getPurchaseOrderDocument() {
        if ((ObjectUtils.isNull(purchaseOrderDocument)
                || ObjectUtils.isNull(purchaseOrderDocument.getPurapDocumentIdentifier()))
                && ObjectUtils.isNotNull(getPurchaseOrderIdentifier())) {
            setPurchaseOrderDocument(SpringContext.getBean(PurchaseOrderService.class).getCurrentPurchaseOrder(
                    getPurchaseOrderIdentifier()));
        }
        return purchaseOrderDocument;
    }

    @Override
    public void setPurchaseOrderDocument(final PurchaseOrderDocument purchaseOrderDocument) {
        if (ObjectUtils.isNull(purchaseOrderDocument)) {
            this.purchaseOrderDocument = null;
        } else {
            if (ObjectUtils.isNotNull(purchaseOrderDocument.getPurapDocumentIdentifier())) {
                setPurchaseOrderIdentifier(purchaseOrderDocument.getPurapDocumentIdentifier());
            }
            this.purchaseOrderDocument = purchaseOrderDocument;
        }
    }

    public boolean isClosePurchaseOrderIndicator() {
        return closePurchaseOrderIndicator;
    }

    public void setClosePurchaseOrderIndicator(final boolean closePurchaseOrderIndicator) {
        this.closePurchaseOrderIndicator = closePurchaseOrderIndicator;
    }

    public boolean isReopenPurchaseOrderIndicator() {
        return reopenPurchaseOrderIndicator;
    }

    public void setReopenPurchaseOrderIndicator(final boolean reopenPurchaseOrderIndicator) {
        this.reopenPurchaseOrderIndicator = reopenPurchaseOrderIndicator;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(final String bankCode) {
        this.bankCode = bankCode;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(final Bank bank) {
        this.bank = bank;
    }

    @Deprecated
    public void setProcessingCampus(final CampusParameter processingCampus) {
        this.processingCampus = processingCampus;
    }

    /**
     * Retrieves the universal user object for the last person to perform an action on the document.
     */
    public Person getLastActionPerformedByUser() {
        return SpringContext.getBean(org.kuali.kfs.kim.api.identity.PersonService.class).getPerson(
                getLastActionPerformedByPersonId());
    }

    /**
     * @return the person's name who last performed an action on the document.
     */
    public String getLastActionPerformedByPersonName() {
        final Person user = getLastActionPerformedByUser();
        if (ObjectUtils.isNull(user)) {
            return "";
        } else {
            return user.getName();
        }
    }

    public String getDebitCreditCodeForGLEntries() {
        return debitCreditCodeForGLEntries;
    }

    public void setDebitCreditCodeForGLEntries(final String debitCreditCodeForGLEntries) {
        this.debitCreditCodeForGLEntries = debitCreditCodeForGLEntries;
    }

    @Override
    public boolean isUnmatchedOverride() {
        return unmatchedOverride;
    }

    @Override
    public void setUnmatchedOverride(final boolean unmatchedOverride) {
        this.unmatchedOverride = unmatchedOverride;
    }

    public boolean getExtractedIndicatorForSearching() {
        return extractedTimestamp != null;
    }

    public boolean isHoldIndicatorForSearching() {
        return holdIndicator;
    }

    @Override
    public abstract KualiDecimal getGrandTotal();

    @Override
    public abstract KualiDecimal getInitialAmount();

    @Override
    public boolean isContinuationAccountIndicator() {
        return continuationAccountIndicator;
    }

    @Override
    public void setContinuationAccountIndicator(final boolean continuationAccountIndicator) {
        this.continuationAccountIndicator = continuationAccountIndicator;
    }

    @Override
    public boolean isExtracted() {
        return ObjectUtils.isNotNull(getExtractedTimestamp());
    }

    @Override
    public abstract AccountsPayableDocumentSpecificService getDocumentSpecificService();

    @Override
    public AccountsPayableItem getAPItemFromPOItem(final PurchaseOrderItem poi) {
        for (final AccountsPayableItem preqItem : (List<AccountsPayableItem>) getItems()) {
            preqItem.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);

            if (preqItem.getItemType().isLineItemIndicator()) {
                if (preqItem.getItemLineNumber().compareTo(poi.getItemLineNumber()) == 0) {
                    return preqItem;
                }
            } else {
                return (AccountsPayableItem) SpringContext.getBean(PurapService.class).getBelowTheLineByType(this,
                        poi.getItemType());
            }
        }
        return null;
    }

    @Override
    public Class getItemClass() {
        return null;
    }

    @Override
    public PurchasingAccountsPayableDocument getPurApSourceDocumentIfPossible() {
        return null;
    }

    @Override
    public String getPurApSourceDocumentLabelIfPossible() {
        return null;
    }

    /*
     * KFSUPGRADE-1124 / KFSPTS-16954: Fixed an issue that prevented extended prices from being calculated properly.
     */
    public void updateExtendedPriceOnItems() {
        for (final AccountsPayableItem item : (List<AccountsPayableItem>) getItems()) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);

            if (ObjectUtils.isNotNull(item.getItemType())) {
                if (item.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                    final KualiDecimal newExtendedPrice = item.calculateExtendedPrice();
                    item.setExtendedPrice(newExtendedPrice);
                }
            }
        }
    }

    @Override
    public KualiDecimal getTotalRemitTax() {
        if (!isUseTaxIndicator()) {
            return KualiDecimal.ZERO.equals(getTotalTaxAmount()) ? null : getTotalTaxAmount();
        }
        return null;
    }

    @Override
    public boolean customizeOffsetGeneralLedgerPendingEntry(
            final GeneralLedgerPendingEntrySourceDetail accountingLine,
            final GeneralLedgerPendingEntry explicitEntry, final GeneralLedgerPendingEntry offsetEntry) {
        boolean value = super.customizeOffsetGeneralLedgerPendingEntry(accountingLine, explicitEntry, offsetEntry);
        if (offsetEntry != null && offsetUseTax != null) {
            offsetEntry.setChartOfAccountsCode(offsetUseTax.getChartOfAccountsCode());
            offsetEntry.refreshReferenceObject(KFSPropertyConstants.CHART);
            offsetEntry.setAccountNumber(offsetUseTax.getAccountNumber());
            offsetEntry.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            offsetEntry.setFinancialObjectCode(offsetUseTax.getFinancialObjectCode());
            offsetEntry.refreshReferenceObject(KFSPropertyConstants.FINANCIAL_OBJECT);
        } else {
            value = false;
        }
        return value;
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PurApItemUseTax offsetUseTax) {
        this.offsetUseTax = offsetUseTax;
        boolean value = this.generateGeneralLedgerPendingEntries(glpeSourceDetail, sequenceHelper);
        this.offsetUseTax = null;
        return value;
    }

    public String getHoldIndicatorForResult() {
        return isHoldIndicator() ? "Yes" : "No";
    }

    public String getProcessingCampusCodeForSearch() {
        return getProcessingCampusCode();
    }

    public String getDocumentChartOfAccountsCodeForSearching() {
        return getPurchaseOrderDocument().getChartOfAccountsCode();
    }

    public String getDocumentOrganizationCodeForSearching() {
        return getPurchaseOrderDocument().getOrganizationCode();
    }

    /**
     * @return workflow document type for the purap document
     */
    public String getDocumentType() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(getClass());
    }

    @Override
    public boolean shouldGiveErrorForEmptyAccountsProration() {
        return true;
    }
    
    /*
     * KFSUPGRADE-507
     * check to open attachment tab.  isAttachmentRequired is 'protected', so add this one
     * or increase the visibility of isAttachmentRequired to 'public' for this class and child classes
     */
    public boolean isOpenAttachmentTab() {
    	return isAttachmentRequired();
    }

}

