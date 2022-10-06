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
package org.kuali.kfs.module.purap.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.TransactionalDocument;
import org.kuali.kfs.krad.rules.rule.event.ApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurApItemBase;
import org.kuali.kfs.module.purap.businessobject.PurApItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.businessobject.SensitiveData;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.impl.PurapServiceImpl;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.SensitiveDataService;
import org.kuali.kfs.module.purap.util.PurApRelatedViews;
import org.kuali.kfs.module.purap.util.PurapAccountingLineComparator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.AdHocPaymentIndicator;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.validation.event.AddAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.DeleteAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.ReviewAccountingLineEvent;
import org.kuali.kfs.sys.document.validation.event.UpdateAccountingLineEvent;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for Purchasing-Accounts Payable Documents.
 */
public abstract class PurchasingAccountsPayableDocumentBase extends AccountingDocumentBase implements
        PurchasingAccountsPayableDocument, AmountTotaling {

    private static final Logger LOG = LogManager.getLogger();

    // SHARED FIELDS BETWEEN REQUISITION, PURCHASE ORDER, PAYMENT REQUEST, AND CREDIT MEMO
    protected Integer purapDocumentIdentifier;
    protected Integer vendorHeaderGeneratedIdentifier;
    protected Integer vendorDetailAssignedIdentifier;
    protected String vendorCustomerNumber;
    protected String vendorName;
    protected String vendorLine1Address;
    protected String vendorLine2Address;
    protected String vendorCityName;
    protected String vendorStateCode;
    protected String vendorAddressInternationalProvinceName;
    protected String vendorPostalCode;
    protected String vendorCountryCode;
    protected Integer accountsPayablePurchasingDocumentLinkIdentifier;
    protected boolean useTaxIndicator;
    protected String vendorAttentionName;
    //code for account distribution method
    protected String accountDistributionMethod;

    // NOT PERSISTED IN DB
    protected String vendorNumber;
    protected Integer vendorAddressGeneratedIdentifier;
    protected Boolean overrideWorkflowButtons = null;
    protected transient PurApRelatedViews relatedViews;
    protected boolean sensitive;

    protected boolean calculated;

    // COLLECTIONS
    protected List<PurApItem> items;
    // don't use me for anything else!!
    protected List<SourceAccountingLine> accountsForRouting;

    // REFERENCE OBJECTS
    protected VendorDetail vendorDetail;
    protected Country vendorCountry;

    // STATIC
    public transient String[] belowTheLineTypes;

    // workaround for purapOjbCollectionHelper - remove when merged into rice
    public boolean allowDeleteAwareCollection = true;

    // CU Enhancement KFSPTS-1639
    protected String vendorEmailAddress;   

    /**
     * Default constructor to be overridden.
     */
    public PurchasingAccountsPayableDocumentBase() {
        items = new ArrayList<>();
    }

    protected GeneralLedgerPendingEntry getFirstPendingGLEntry() {
        if (ObjectUtils.isNotNull(getGeneralLedgerPendingEntries()) && !getGeneralLedgerPendingEntries().isEmpty()) {
            return getGeneralLedgerPendingEntries().get(0);
        }
        return null;
    }

    public Integer getPostingYearFromPendingGLEntries() {
        GeneralLedgerPendingEntry glpe = getFirstPendingGLEntry();
        if (ObjectUtils.isNotNull(glpe)) {
            return glpe.getUniversityFiscalYear();
        }
        return null;
    }

    public String getPostingPeriodCodeFromPendingGLEntries() {
        GeneralLedgerPendingEntry glpe = getFirstPendingGLEntry();
        if (ObjectUtils.isNotNull(glpe)) {
            return glpe.getUniversityFiscalPeriodCode();
        }
        return null;
    }

    public List<SourceAccountingLine> getAccountsForRouting() {
        if (accountsForRouting == null) {
            populateAccountsForRouting();
        }
        return accountsForRouting;
    }

    public void setAccountsForRouting(List<SourceAccountingLine> accountsForRouting) {
        this.accountsForRouting = accountsForRouting;
    }

    /**
     * Makes sure that accounts for routing has been generated, so that other information can be retrieved from that
     */
    protected void populateAccountsForRouting() {
        SpringContext.getBean(PurapAccountingService.class).updateAccountAmounts(this);
        setAccountsForRouting(SpringContext.getBean(PurapAccountingService.class).generateSummary(getItems()));
        // need to refresh to get the references for the searchable attributes (ie status) and for invoking route
        // levels (ie account objects) -hjs
        refreshNonUpdateableReferences();
        for (SourceAccountingLine sourceLine : getAccountsForRouting()) {
            sourceLine.refreshNonUpdateableReferences();
        }
    }

    public boolean isSensitive() {
        List<SensitiveData> sensitiveData = SpringContext.getBean(SensitiveDataService.class)
                .getSensitiveDatasAssignedByRelatedDocId(getAccountsPayablePurchasingDocumentLinkIdentifier());
        return ObjectUtils.isNotNull(sensitiveData) && !sensitiveData.isEmpty();
    }

    @Override
    public boolean isInquiryRendered() {
        return isPostingYearPrior();
    }

    @Override
    public boolean isPostingYearNext() {
        Integer currentFY = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        return getPostingYear().compareTo(currentFY) > 0;
    }

    @Override
    public boolean isPostingYearPrior() {
        Integer currentFY = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        return getPostingYear().compareTo(currentFY) < 0;
    }

    @Override
    public Integer getPostingYearNextOrCurrent() {
        if (isPostingYearNext()) {
            //FY is set to next; use it
            return getPostingYear();
        }
        //FY is NOT set to next; use CURRENT
        return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public abstract Class getItemClass();

    @SuppressWarnings("rawtypes")
    public abstract Class getItemUseTaxClass();

    @Override
    public abstract PurchasingAccountsPayableDocument getPurApSourceDocumentIfPossible();

    @Override
    public abstract String getPurApSourceDocumentLabelIfPossible();

    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        customPrepareForSave(event);
        super.prepareForSave(event);
        fixItemReferences();
    }

    /**
     * PURAP documents are all overriding this method to return false because sufficient funds checking should not be
     * performed on route of any PURAP documents. Only the Purchase Order performs a sufficient funds check and it is
     * manually forced during routing.
     */
    @Override
    public boolean documentPerformsSufficientFundsCheck() {
        return false;
    }

    // for app doc status
    @Override
    public boolean isDocumentStoppedInRouteNode(String nodeName) {
        WorkflowDocument workflowDocument = this.getDocumentHeader().getWorkflowDocument();

        Set<String> names = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(names)) {
            List<String> currentRouteLevels = new ArrayList<String>(names);                    
            for (String routeLevel  : currentRouteLevels) {    
                if (routeLevel.contains(nodeName) && workflowDocument.isApprovalRequested()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Records the specified error message into the Log file and throws a runtime exception.
     *
     * @param errorMessage the error message to be logged.
     */
    protected void logAndThrowRuntimeException(String errorMessage) {
        this.logAndThrowRuntimeException(errorMessage, null);
    }

    /**
     * Records the specified error message into the Log file and throws the specified runtime exception.
     *
     * @param errorMessage the specified error message.
     * @param e            the specified runtime exception.
     */
    protected void logAndThrowRuntimeException(String errorMessage, Exception e) {
        if (ObjectUtils.isNotNull(e)) {
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } else {
            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Allows child PO classes to customize the prepareForSave method. Most of the subclasses need to call the super's
     * method to get the GL entry creation, but they each need to do different things to prepare for those entries to
     * be created. This is only for PO since it has children classes that need different prep work for GL creation.
     *
     * @param event the event involved in this action.
     */
    public void customPrepareForSave(KualiDocumentEvent event) {
        // Need this here so that it happens before the GL work is done
        SpringContext.getBean(PurapAccountingService.class).updateAccountAmounts(this);

        if (event instanceof RouteDocumentEvent || event instanceof ApproveDocumentEvent) {
            if (this instanceof VendorCreditMemoDocument && ((VendorCreditMemoDocument) this).isSourceVendor()) {
                return;
            }
            SpringContext.getBean(PurapServiceImpl.class).calculateTax(this);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = new ArrayList<List>();
        managedLists.add(getDeletionAwareAccountingLines());
        if (allowDeleteAwareCollection) {
            //From now on, the list of accounting lines would have been added when the
            //super.buildListOfDeletionAwareLists() is executed when it calls getSourceAccountingLines().
            //So we can remove the old codes that used to exist here to add the accounts to the
            //managedLists and just use the one from the super.buildListOfDeletionAwareLists()
            managedLists.add(this.getItems());
            managedLists.add(getDeletionAwareUseTaxItems());
        }
        return managedLists;
    }

    /**
     * Build deletion list of accounting lines for PurAp generic use.
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected List getDeletionAwareAccountingLines() {
        List<PurApAccountingLine> deletionAwareAccountingLines = new ArrayList<>();
        for (Object itemAsObject : this.getItems()) {
            final PurApItem item = (PurApItem) itemAsObject;
            deletionAwareAccountingLines.addAll(item.getSourceAccountingLines());
        }
        return deletionAwareAccountingLines;
    }

    /**
     * Build deletion list of use tax items for PurAp generic use.
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected List getDeletionAwareUseTaxItems() {
        List<PurApItemUseTax> deletionAwareUseTaxItems = new ArrayList<>();

        List<PurApItemBase> subManageList = this.getItems();
        for (PurApItemBase subManage : subManageList) {
            deletionAwareUseTaxItems.addAll(subManage.getUseTaxItems());
        }

        return deletionAwareUseTaxItems;
    }

    /**
     * @Override public List buildListOfDeletionAwareLists() {
     * List managedLists = new ArrayList();
     * if (allowDeleteAwareCollection) {
     * List<PurApAccountingLine> purapAccountsList = new ArrayList<PurApAccountingLine>();
     * for (Object itemAsObject : this.getItems()) {
     * final PurApItem item = (PurApItem)itemAsObject;
     * purapAccountsList.addAll(item.getSourceAccountingLines());
     * }
     * managedLists.add(purapAccountsList);
     * managedLists.add(this.getItems());
     * }
     * return managedLists;
     * }
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#buildListOfDeletionAwareLists()
     */
    @Override
    public void processAfterRetrieve() {
        super.processAfterRetrieve();
        refreshNonUpdateableReferences();
    }

    @Override
    public void addItem(PurApItem item) {
        int itemLinePosition = getItemLinePosition();
        if (ObjectUtils.isNotNull(item.getItemLineNumber()) && item.getItemLineNumber() > 0
                && item.getItemLineNumber() <= itemLinePosition) {
            itemLinePosition = item.getItemLineNumber() - 1;
        }

        item.setPurapDocumentIdentifier(this.purapDocumentIdentifier);
        item.setPurapDocument(this);

        items.add(itemLinePosition, item);
        renumberItems(itemLinePosition);
    }

    @Override
    public void deleteItem(int lineNum) {
        items.remove(lineNum);
        renumberItems(lineNum);
    }

    @Override
    public void renumberItems(int start) {
        for (int i = start; i < items.size(); i++) {
            PurApItem item = items.get(i);
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);

            // only set the item line number for above the line items
            if (item.getItemType().isLineItemIndicator()) {
                // KFSPTS-1719/KFSUPGRADE-485 :  skip this for non-qty order
                if (!(item instanceof PaymentRequestItem && ((PaymentRequestItem) item).getPurchaseOrderItem() != null && ((PaymentRequestItem) item).getPurchaseOrderItem().isNoQtyItem())) {
                	    item.setItemLineNumber(i + 1);
                }
            }
        }
    }

    @Override
    public void itemSwap(int positionFrom, int positionTo) {
        // if out of range do nothing
        if (positionTo < 0 || positionTo >= getItemLinePosition()) {
            return;
        }
        PurApItem item1 = this.getItem(positionFrom);
        PurApItem item2 = this.getItem(positionTo);
        Integer oldFirstPos = item1.getItemLineNumber();
        // swap line numbers
        item1.setItemLineNumber(item2.getItemLineNumber());
        item2.setItemLineNumber(oldFirstPos);
        // fix ordering in list
        items.remove(positionFrom);
        items.add(positionTo, item1);
    }

    @Override
    public int getItemLinePosition() {
        int belowTheLineCount = 0;
        for (PurApItem item : items) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
            if (item.getItemType().isAdditionalChargeIndicator()) {
                belowTheLineCount++;
            }
        }
        return items.size() - belowTheLineCount;
    }

    @Override
    public PurApItem getItem(int pos) {
        return items.get(pos);
    }

    /**
     * Iterates through the items of the document and returns the item with the line number equal to the number given,
     * or null if a match is not found.
     *
     * @param lineNumber line number to match on.
     * @return the PurchasingAp Item if a match is found, else null.
     */
    @SuppressWarnings("rawtypes")
    public PurApItem getItemByLineNumber(int lineNumber) {
        for (PurApItem item : items) {
            if (item.getItemLineNumber() != null && item.getItemLineNumber() == lineNumber) {
                return item;
            }
        }
        return null;
    }

    /**
     * Find the item in the document via its string identifier.
     *
     * @param itemStrID the string identifier of the item being searched for
     * @return the item being searched for
     */
    @SuppressWarnings("rawtypes")
    public PurApItem getItemByStringIdentifier(String itemStrID) {
        for (PurApItem item : items) {
            if (StringUtils.equalsIgnoreCase(item.getItemIdentifierString(), itemStrID)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Find the item in the document via its identifier.
     *
     * @param itemID the string identifier of the item being searched for
     * @return the item being searched for
     */
    @SuppressWarnings("rawtypes")
    public PurApItem getItemByItemIdentifier(Integer itemID) {
        for (PurApItem item : items) {
            if (item.getItemIdentifier() == itemID) {
                return item;
            }
        }
        return null;
    }

    /**
     * Overriding the parent method so that we can just set the posting year without the other stuff that the parent
     * does to the accounting period. We only store the posting year on the doc and don't want the other stuff.
     */
    @Override
    public void setPostingYear(Integer postingYear) {
        this.postingYear = postingYear;
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        return getTotalDollarAmountAllItems(null);
    }

    @Override
    public void setTotalDollarAmount(KualiDecimal amount) {
        // do nothing, this is so that the jsp won't complain about totalDollarAmount have no setter method.
    }

    @Override
    public KualiDecimal getTotalDollarAmountAllItems(String[] excludedTypes) {
        return getTotalDollarAmountWithExclusions(excludedTypes, true);
    }

    /**
     * @return the total dollar amount of all above the line items.
     */
    @Override
    public KualiDecimal getTotalDollarAmountAboveLineItems() {
        return getTotalDollarAmountAboveLineItems(null);
    }

    /**
     * @param excludedTypes the types of items to be excluded.
     * @return the total dollar amount of all above the line items with the specified item types excluded..
     */
    public KualiDecimal getTotalDollarAmountAboveLineItems(String[] excludedTypes) {
        return getTotalDollarAmountWithExclusions(excludedTypes, false);
    }

    /**
     * @param excludedTypes       the types of items to be excluded.
     * @param includeBelowTheLine indicates whether below the line items shall be included.
     * @return the total dollar amount with the specified item types excluded.
     */
    public KualiDecimal getTotalDollarAmountWithExclusions(String[] excludedTypes, boolean includeBelowTheLine) {
        List<PurApItem> itemsForTotal = getItems();

        return getTotalDollarAmountWithExclusionsSubsetItems(excludedTypes, includeBelowTheLine, itemsForTotal);
    }

    /**
     * @param excludedTypes
     * @param includeBelowTheLine
     * @param itemsForTotal
     * @return
     */
    protected KualiDecimal getTotalDollarAmountWithExclusionsSubsetItems(String[] excludedTypes,
            boolean includeBelowTheLine, List<PurApItem> itemsForTotal) {
        if (excludedTypes == null) {
            excludedTypes = new String[]{};
        }

        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (PurApItem item : itemsForTotal) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
            ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator())
                    && !ArrayUtils.contains(excludedTypes, it.getItemTypeCode())) {
                KualiDecimal totalAmount = item.getTotalAmount();
                KualiDecimal itemTotal = totalAmount != null ? totalAmount : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public KualiDecimal getTotalDollarAmountForTradeIn() {
        List<PurApItem> tradeInItems = getTradeInItems();
        return getTotalDollarAmountWithExclusionsSubsetItems(null, false, tradeInItems);
    }

    @Override
    public List<PurApItem> getTradeInItems() {
        List<PurApItem> tradeInItems = new ArrayList<>();
        for (PurApItem purApItem : (List<PurApItem>) getItems()) {
            if (purApItem.getItemAssignedToTradeInIndicator()) {
                tradeInItems.add(purApItem);
            }
        }
        return tradeInItems;
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmount() {
        return getTotalPreTaxDollarAmountAllItems(null);
    }

    @Override
    public void setTotalPreTaxDollarAmount(KualiDecimal amount) {
        // do nothing, this is so that the jsp won't complain about totalDollarAmount have no setter method.
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmountAllItems(String[] excludedTypes) {
        return getTotalPreTaxDollarAmountWithExclusions(excludedTypes, true);
    }

    /**
     * @return the total dollar amount of all above the line items.
     */
    public KualiDecimal getTotalPreTaxDollarAmountAboveLineItems() {
        return getTotalPreTaxDollarAmountAboveLineItems(null);
    }

    /**
     * Computes the total dollar amount of all above the line items with the specified item types excluded.
     *
     * @param excludedTypes the types of items to be excluded.
     * @return the total dollar amount of all above the line items with the specified item types excluded..
     */
    public KualiDecimal getTotalPreTaxDollarAmountAboveLineItems(String[] excludedTypes) {
        return getTotalPreTaxDollarAmountWithExclusions(excludedTypes, false);
    }

    /**
     * Computes the total dollar amount with the specified item types and possibly below the line items excluded.
     *
     * @param excludedTypes       the types of items to be excluded.
     * @param includeBelowTheLine indicates whether below the line items shall be included.
     * @return the total dollar amount with the specified item types excluded.
     */
    public KualiDecimal getTotalPreTaxDollarAmountWithExclusions(String[] excludedTypes, boolean includeBelowTheLine) {
        if (excludedTypes == null) {
            excludedTypes = new String[]{};
        }

        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (PurApItem item : (List<PurApItem>) getItems()) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
            ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator())
                    && !ArrayUtils.contains(excludedTypes, it.getItemTypeCode())) {
                KualiDecimal extendedPrice = item.getExtendedPrice();
                KualiDecimal itemTotal = extendedPrice != null ? extendedPrice : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public KualiDecimal getTotalTaxAmount() {
        return getTotalTaxAmountAllItems(null);
    }

    @Override
    public void setTotalTaxAmount(KualiDecimal amount) {
        // do nothing, this is so that the jsp won't complain about totalTaxAmount have no setter method.
    }

    @Override
    public KualiDecimal getTotalTaxAmountAllItems(String[] excludedTypes) {
        return getTotalTaxAmountWithExclusions(excludedTypes, true);
    }

    @Override
    public KualiDecimal getTotalTaxAmountAboveLineItems() {
        return getTotalTaxAmountAboveLineItems(null);
    }

    @Override
    public KualiDecimal getTotalTaxAmountAboveLineItems(String[] excludedTypes) {
        return getTotalTaxAmountWithExclusions(excludedTypes, false);
    }

    @Override
    public KualiDecimal getTotalTaxAmountWithExclusions(String[] excludedTypes, boolean includeBelowTheLine) {
        if (excludedTypes == null) {
            excludedTypes = new String[]{};
        }

        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (PurApItem item : (List<PurApItem>) getItems()) {
            item.refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
            ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator())
                    && !ArrayUtils.contains(excludedTypes, it.getItemTypeCode())) {
                KualiDecimal taxAmount = item.getItemTaxAmount();
                KualiDecimal itemTotal = taxAmount != null ? taxAmount : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public boolean isUseTaxIndicator() {
        return useTaxIndicator;
    }

    @Override
    public void setUseTaxIndicator(boolean useTaxIndicator) {
        this.useTaxIndicator = useTaxIndicator;
    }

    @Override
    public void templateVendorAddress(VendorAddress vendorAddress) {
        if (vendorAddress == null) {
            return;
        }
        this.setVendorLine1Address(vendorAddress.getVendorLine1Address());
        this.setVendorLine2Address(vendorAddress.getVendorLine2Address());
        this.setVendorCityName(vendorAddress.getVendorCityName());
        this.setVendorStateCode(vendorAddress.getVendorStateCode());
        this.setVendorPostalCode(vendorAddress.getVendorZipCode());
        this.setVendorCountryCode(vendorAddress.getVendorCountryCode());
        //KFSPTS-1639
        this.setVendorEmailAddress(vendorAddress.getVendorAddressEmailAddress());
  }

    /**
     * @return the vendor number for this document.
     */
    @Override
    public String getVendorNumber() {
        if (StringUtils.isNotEmpty(vendorNumber)) {
            return vendorNumber;
        } else if (ObjectUtils.isNotNull(vendorDetail)) {
            return vendorDetail.getVendorNumber();
        } else {
            return "";
        }
    }

    @Override
    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public Boolean getOverrideWorkflowButtons() {
        return overrideWorkflowButtons;
    }

    public void setOverrideWorkflowButtons(Boolean overrideWorkflowButtons) {
        this.overrideWorkflowButtons = overrideWorkflowButtons;
    }

    @Override
    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    @Override
    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    @Override
    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    @Override
    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    @Override
    public String getVendorCustomerNumber() {
        return vendorCustomerNumber;
    }

    @Override
    public void setVendorCustomerNumber(String vendorCustomerNumber) {
        this.vendorCustomerNumber = vendorCustomerNumber;
    }

    @Override
    public Integer getPurapDocumentIdentifier() {
        return purapDocumentIdentifier;
    }

    @Override
    public void setPurapDocumentIdentifier(Integer identifier) {
        this.purapDocumentIdentifier = identifier;
    }

    @Override
    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }

    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getItems() {
        return items;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setItems(List items) {
        this.items = items;
    }

    @Override
    public String getVendorCityName() {
        return vendorCityName;
    }

    @Override
    public void setVendorCityName(String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    @Override
    public String getVendorCountryCode() {
        return vendorCountryCode;
    }

    @Override
    public void setVendorCountryCode(String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
    }

    @Override
    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    @Override
    public void setVendorLine1Address(String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    @Override
    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    @Override
    public void setVendorLine2Address(String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    @Override
    public String getVendorName() {
        return vendorName;
    }

    @Override
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @Override
    public String getVendorPostalCode() {
        return vendorPostalCode;
    }

    @Override
    public void setVendorPostalCode(String vendorPostalCode) {
        this.vendorPostalCode = vendorPostalCode;
    }

    @Override
    public String getVendorStateCode() {
        return vendorStateCode;
    }

    @Override
    public void setVendorStateCode(String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    @Override
    public String getVendorAddressInternationalProvinceName() {
        return vendorAddressInternationalProvinceName;
    }

    @Override
    public void setVendorAddressInternationalProvinceName(String vendorAddressInternationalProvinceName) {
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
    }

    @Override
    public Integer getVendorAddressGeneratedIdentifier() {
        return vendorAddressGeneratedIdentifier;
    }

    @Override
    public void setVendorAddressGeneratedIdentifier(Integer vendorAddressGeneratedIdentifier) {
        this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
    }

    @Override
    public Integer getAccountsPayablePurchasingDocumentLinkIdentifier() {
        return accountsPayablePurchasingDocumentLinkIdentifier;
    }

    @Override
    public void setAccountsPayablePurchasingDocumentLinkIdentifier(
            Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        this.accountsPayablePurchasingDocumentLinkIdentifier = accountsPayablePurchasingDocumentLinkIdentifier;
    }

    @Override
    public String[] getBelowTheLineTypes() {
        if (this.belowTheLineTypes == null) {
            this.belowTheLineTypes = SpringContext.getBean(PurapService.class).getBelowTheLineForDocument(this);
        }
        return belowTheLineTypes;
    }

    @Override
    public Country getVendorCountry() {
        return vendorCountry;
    }

    /**
     * Added only to allow for {@link org.kuali.kfs.module.purap.util.PurApObjectUtils} class to work correctly.
     */
    @Deprecated
    public void setVendorCountry(Country vendorCountry) {
        this.vendorCountry = vendorCountry;
    }

    public String getVendorAttentionName() {
        return vendorAttentionName;
    }

    public void setVendorAttentionName(String vendorAttentionName) {
        this.vendorAttentionName = vendorAttentionName;
    }

    public String getAccountDistributionMethod() {
        return accountDistributionMethod;
    }

    public void setAccountDistributionMethod(String accountDistributionMethod) {
        this.accountDistributionMethod = accountDistributionMethod;
    }

    /**
     * Determines whether the account is debit. It always returns false.
     *
     * @param postable
     * @return boolean false.
     */
    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        return false;
    }

    public PurApRelatedViews getRelatedViews() {
        if (relatedViews == null) {
            relatedViews = new PurApRelatedViews(this.documentNumber,
                    this.accountsPayablePurchasingDocumentLinkIdentifier);
        }
        return relatedViews;
    }

    public void setRelatedViews(PurApRelatedViews relatedViews) {
        this.relatedViews = relatedViews;
    }

    @Override
    public void refreshNonUpdateableReferences() {
        super.refreshNonUpdateableReferences();

        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            //refresh the accounts if they do exist...
            for (PurApAccountingLine account : item.getSourceAccountingLines()) {
                account.refreshNonUpdateableReferences();
            }
        }

        fixItemReferences();
    }

    /**
     * This method fixes the item references in this document if it's new
     */
    @Override
    public void fixItemReferences() {
        //fix item and account references in case this is a new doc (since they will be lost)
        if (ObjectUtils.isNull(this.purapDocumentIdentifier)) {
            for (PurApItem item : (List<PurApItem>) this.getItems()) {
                item.setPurapDocument(this);
                item.fixAccountReferences();
            }
        }
    }

    /**
     * @return the trade in item of the document.
     */
    @Override
    public PurApItem getTradeInItem() {
        for (PurApItem item : (List<PurApItem>) getItems()) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean getIsATypeOfPurAPRecDoc() {
        return true;
    }

    @Override
    public boolean getIsATypeOfPurDoc() {
        return this instanceof PurchasingDocumentBase;
    }

    @Override
    public boolean getIsATypeOfPODoc() {
        return this instanceof PurchaseOrderDocument;
    }

    @Override
    public boolean getIsPODoc() {
        return this instanceof PurchaseOrderDocument
                && !(this instanceof PurchaseOrderAmendmentDocument)
                && !(this instanceof PurchaseOrderCloseDocument)
                && !(this instanceof PurchaseOrderPaymentHoldDocument)
                && !(this instanceof PurchaseOrderRemoveHoldDocument)
                && !(this instanceof PurchaseOrderReopenDocument)
                && !(this instanceof PurchaseOrderRetransmitDocument)
                && !(this instanceof PurchaseOrderSplitDocument)
                && !(this instanceof PurchaseOrderVoidDocument);
    }

    @Override
    public boolean getIsReqsDoc() {
        return this instanceof RequisitionDocument;
    }

    /**
     * build document title based on the properties of current document
     *
     * @param title the default document title
     * @return the combine information of the given title and additional payment indicators
     */
    protected String buildDocumentTitle(String title) {
        if (this.getVendorDetail() == null) {
            return title;
        }

        Integer vendorHeaderGeneratedIdentifier = this.getVendorDetail().getVendorHeaderGeneratedIdentifier();
        VendorService vendorService = SpringContext.getBean(VendorService.class);

        Object[] indicators = new String[2];

        boolean isEmployeeVendor = vendorService.isVendorInstitutionEmployee(vendorHeaderGeneratedIdentifier);
        indicators[0] = isEmployeeVendor ? AdHocPaymentIndicator.EMPLOYEE_VENDOR : AdHocPaymentIndicator.OTHER;

        boolean isVendorForeign = vendorService.isVendorForeign(vendorHeaderGeneratedIdentifier);
        indicators[1] = isVendorForeign ? AdHocPaymentIndicator.NONRESIDENT_VENDOR : AdHocPaymentIndicator.OTHER;

        for (Object indicator : indicators) {
            if (!AdHocPaymentIndicator.OTHER.equals(indicator)) {
                String titlePattern = title + " [{0}:{1}]";
                return MessageFormat.format(titlePattern, indicators);
            }
        }

        return title;
    }

    /**
     * Overridden to return the source lines of all of the items
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSourceAccountingLines() {
        if (ObjectUtils.isNotNull(sourceAccountingLines) && !sourceAccountingLines.isEmpty()) {
            // do nothing because acct lines have already been set
            return sourceAccountingLines;
        } else {
            List<AccountingLine> sourceAccountingLines = new ArrayList<>();
            for (Object itemAsObject : this.getItems()) {
                final PurApItem item = (PurApItem) itemAsObject;
                for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                    //KFSMI-9053: check if the accounting line does not already exist in the list
                    //and if so then add to the list.  Preventing duplicates
                    if (!isDuplicateAccountingLine(sourceAccountingLines, accountingLine)) {
                        sourceAccountingLines.add(accountingLine);
                    }
                }
            }
            return sourceAccountingLines;
        }
    }

    /**
     * Helper method to check if the source accounting line is already in the list and if so return true
     *
     * @param sourceAccountingLines
     * @param accountingLine
     * @return true if it is a duplicate else return false.
     */
    protected boolean isDuplicateAccountingLine(List<AccountingLine> sourceAccountingLines,
            PurApAccountingLine accountingLine) {
        for (AccountingLine sourceLine : sourceAccountingLines) {
            PurApAccountingLine purapAccountLine = (PurApAccountingLine) sourceLine;

            if (purapAccountLine.accountStringsAreEqual(accountingLine)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to find the matching accountingLines in the list of sourceAccountingLines and sum up the
     * lines amounts.
     *
     * @param matchingAccountingLine
     * @return accountTotalGLEntryAmount
     */
    protected KualiDecimal getAccountTotalGLEntryAmount(AccountingLine matchingAccountingLine) {
        KualiDecimal accountTotalGLEntryAmount = KualiDecimal.ZERO;

        for (Object itemAsObject : this.getItems()) {
            final PurApItem item = (PurApItem) itemAsObject;
            for (PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                //KFSMI-9053: check if the accounting line is a duplicate then add the total
                if (accountingLine.accountStringsAreEqual((SourceAccountingLine) matchingAccountingLine)) {
                    accountTotalGLEntryAmount = accountTotalGLEntryAmount.add(accountingLine.getAmount());
                }
            }
        }

        return accountTotalGLEntryAmount;
    }

    /**
     * Checks whether the related purchase order views need a warning to be displayed, i.e. if at least one of the
     * purchase orders has never been opened.
     *
     * @return true if at least one related purchase order needs a warning; false otherwise
     */
    public boolean getNeedWarningRelatedPOs() {
        List<PurchaseOrderView> poViews = getRelatedViews().getRelatedPurchaseOrderViews();
        for (PurchaseOrderView poView : poViews) {
            if (poView.getNeedWarning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Accounting lines that are read-only should skip validation
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected List getPersistedSourceAccountingLinesForComparison() {
        LOG.info("Checking persisted source accounting lines for read-only fields");
        List<String> restrictedItemTypesList = new ArrayList<>();
        try {
            restrictedItemTypesList = new ArrayList<>(SpringContext.getBean(ParameterService.class)
                    .getParameterValuesAsString(this.getClass(),
                            PurapParameterConstants.PURAP_ITEM_TYPES_RESTRICTING_ACCOUNT_EDIT));
        } catch (IllegalArgumentException iae) {
            // do nothing, not a problem if no restricted types are defined
        }

        PurapAccountingService purApAccountingService = SpringContext.getBean(PurapAccountingService.class);
        List persistedSourceLines = new ArrayList();

        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            // only check items that already have been persisted since last save
            if (ObjectUtils.isNotNull(item.getItemIdentifier())) {
                // Disable validation if the item is read-only
                final boolean isNotReadOnly = !(restrictedItemTypesList != null
                                                && restrictedItemTypesList.contains(item.getItemTypeCode()));
                if (isNotReadOnly) {
                    persistedSourceLines.addAll(purApAccountingService.getAccountsFromItem(item));
                }
            }
        }
        return persistedSourceLines;
    }

    /**
     * Accounting lines that are read-only should skip validation
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected List getSourceAccountingLinesForComparison() {
        LOG.info("Checking source accounting lines for read-only fields");
        List<String> restrictedItemTypesList = new ArrayList<>();
        try {
            restrictedItemTypesList = new ArrayList<>(SpringContext.getBean(ParameterService.class)
                    .getParameterValuesAsString(this.getClass(),
                            PurapParameterConstants.PURAP_ITEM_TYPES_RESTRICTING_ACCOUNT_EDIT));
        } catch (IllegalArgumentException iae) {
            // do nothing, not a problem if no restricted types are defined
        }
        List currentSourceLines = new ArrayList();
        for (PurApItem item : (List<PurApItem>) this.getItems()) {
            // Disable validation if the item is read-only
            final boolean isNotReadOnly = !(restrictedItemTypesList != null
                    && restrictedItemTypesList.contains(item.getItemTypeCode()));
            if (isNotReadOnly) {
                currentSourceLines.addAll(item.getSourceAccountingLines());
            }
        }
        return currentSourceLines;
    }

    @Override
    public boolean isCalculated() {
        return calculated;
    }

    @Override
    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }
    
    @Override
    public PersistableBusinessObject getNoteTarget() {
        return this;
    }

    @Override
    public NoteType getNoteType() {
        return NoteType.BUSINESS_OBJECT;
    }

	public String getVendorEmailAddress() {
		return vendorEmailAddress;
	}

	public void setVendorEmailAddress(String vendorEmailAddress) {
		this.vendorEmailAddress = vendorEmailAddress;
	}

	// KFSUPGRADE-503 copied from AccountingDocumentBase and change
    protected List generateEvents(List persistedLines, List currentLines, String errorPathPrefix, TransactionalDocument document) {
        List addEvents = new ArrayList();
        List updateEvents = new ArrayList();
        List reviewEvents = new ArrayList();
        List deleteEvents = new ArrayList();
        errorPathPrefix = KFSConstants.DOCUMENT_PROPERTY_NAME + ".item["; 

        //
        // generate events
        Map persistedLineMap = buildAccountingLineMap(persistedLines);

        // (iterate through current lines to detect additions and updates, removing affected lines from persistedLineMap as we go
        // so deletions can be detected by looking at whatever remains in persistedLineMap)
        int index = 0;
        for (Iterator i = currentLines.iterator(); i.hasNext(); index++) {
      //      String indexedErrorPathPrefix = errorPathPrefix + "[" + index + "]";
            AccountingLine currentLine = (AccountingLine) i.next();
            Integer key = currentLine.getSequenceNumber();
            String indexedErrorPathPrefix = getIndexedErrorPathPrefix(errorPathPrefix, currentLine);

            AccountingLine persistedLine = (AccountingLine) persistedLineMap.get(key);
            // if line is both current and persisted...
            if (persistedLine != null) {
                // ...check for updates
                if (!currentLine.isLike(persistedLine)) {
                    UpdateAccountingLineEvent updateEvent = new UpdateAccountingLineEvent(indexedErrorPathPrefix, document, persistedLine, currentLine);
                    updateEvents.add(updateEvent);
                }
                else {
                    ReviewAccountingLineEvent reviewEvent = new ReviewAccountingLineEvent(indexedErrorPathPrefix, document, currentLine);
                    reviewEvents.add(reviewEvent);
                }

                persistedLineMap.remove(key);
            }
            else {
                // it must be a new addition
                AddAccountingLineEvent addEvent = new AddAccountingLineEvent(indexedErrorPathPrefix, document, currentLine);
                addEvents.add(addEvent);
            }
        }

        // detect deletions
        for (Iterator i = persistedLineMap.entrySet().iterator(); i.hasNext();) {
            // the deleted line is not displayed on the page, so associate the error with the whole group
            String groupErrorPathPrefix = errorPathPrefix + KFSConstants.ACCOUNTING_LINE_GROUP_SUFFIX;
            Map.Entry e = (Map.Entry) i.next();
            AccountingLine persistedLine = (AccountingLine) e.getValue();
            DeleteAccountingLineEvent deleteEvent = new DeleteAccountingLineEvent(groupErrorPathPrefix, document, persistedLine, true);
            deleteEvents.add(deleteEvent);
        }


        //
        // merge the lists
        List lineEvents = new ArrayList();
        lineEvents.addAll(reviewEvents);
        lineEvents.addAll(updateEvents);
        lineEvents.addAll(addEvents);
        lineEvents.addAll(deleteEvents);

        return lineEvents;
    }

    private String getIndexedErrorPathPrefix(String errorPathPrefix, AccountingLine currentLine) {
        int idx = 0;
        int i = 0;
        for (PurApItem item : (List<PurApItem>)this.getItems()) {
            int j = 0;
            // KFSPTS-1273 Note : The accountinglinegrouptag will sort this before the acctlines are rendered.  If we don't sort here, then
            // the error icon may be placed in wrong line.
            if (CollectionUtils.isNotEmpty(item.getSourceAccountingLines())) {
                Collections.sort(item.getSourceAccountingLines(), new PurapAccountingLineComparator());
            }
            for (PurApAccountingLine acctLine : item.getSourceAccountingLines()) {
                if (acctLine == currentLine) {
                    return errorPathPrefix +  i + "]."+ KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "["+j+"]";
                } else {
                    j++;
                }
            }
            i++;
        }
        return errorPathPrefix +  0 + "]."+ KFSConstants.EXISTING_SOURCE_ACCT_LINE_PROPERTY_NAME + "["+0+"]";
            
    }


}
