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
package org.kuali.kfs.module.purap.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.util.ObjectPopulationUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class PurApItemBase extends PersistableBusinessObjectBase implements PurApItem {

    private Integer itemIdentifier;
    private Integer itemLineNumber;
    private String itemUnitOfMeasureCode;
    private String itemCatalogNumber;
    private String itemDescription;
    private BigDecimal itemUnitPrice;
    private String itemTypeCode;
    private String itemAuxiliaryPartIdentifier;
    private String externalOrganizationB2bProductReferenceNumber;
    private String externalOrganizationB2bProductTypeName;
    private boolean itemAssignedToTradeInIndicator;
    // not currently in DB
    private KualiDecimal extendedPrice;
    private KualiDecimal itemSalesTaxAmount;

    private List<PurApItemUseTax> useTaxItems;
    private List<PurApAccountingLine> sourceAccountingLines;
    private List<PurApAccountingLine> baselineSourceAccountingLines;
    private PurApAccountingLine newSourceLine;

    private ItemType itemType;
    private Integer purapDocumentIdentifier;
    private KualiDecimal itemQuantity;

    private PurchasingAccountsPayableDocument purapDocument;

    public PurApItemBase() {
        itemTypeCode = PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE;
        sourceAccountingLines = new ArrayList<>();
        baselineSourceAccountingLines = new ArrayList<>();
        useTaxItems = new ArrayList<>();
        resetAccount();
    }

    @Override
    public String getItemIdentifierString() {
        final String itemLineNumberString = getItemLineNumber() != null ? getItemLineNumber().toString() : "";
        return getItemType().isLineItemIndicator() ? "Item " + itemLineNumberString :
                getItemType().getItemTypeDescription();
    }

    @Override
    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    @Override
    public void setItemIdentifier(final Integer ItemIdentifier) {
        itemIdentifier = ItemIdentifier;
    }

    @Override
    public Integer getItemLineNumber() {
        return itemLineNumber;
    }

    @Override
    public void setItemLineNumber(final Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

    @Override
    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    @Override
    public void setItemUnitOfMeasureCode(final String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = StringUtils.isNotBlank(itemUnitOfMeasureCode) ?
                itemUnitOfMeasureCode.toUpperCase(Locale.US) : itemUnitOfMeasureCode;
    }

    @Override
    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    @Override
    public void setItemCatalogNumber(final String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    @Override
    public String getItemDescription() {
        return itemDescription;
    }

    @Override
    public void setItemDescription(final String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @Override
    public BigDecimal getItemUnitPrice() {
        // Setting scale on retrieval of unit price
        if (itemUnitPrice != null) {
            if (itemUnitPrice.scale() < PurapConstants.DOLLAR_AMOUNT_MIN_SCALE) {
                itemUnitPrice = itemUnitPrice.setScale(PurapConstants.DOLLAR_AMOUNT_MIN_SCALE,
                        KualiDecimal.ROUND_BEHAVIOR);
            } else if (itemUnitPrice.scale() > PurapConstants.UNIT_PRICE_MAX_SCALE) {
                itemUnitPrice = itemUnitPrice.setScale(PurapConstants.UNIT_PRICE_MAX_SCALE,
                        KualiDecimal.ROUND_BEHAVIOR);
            }
        }

        return itemUnitPrice;
    }

    @Override
    public void setItemUnitPrice(BigDecimal itemUnitPrice) {
        if (itemUnitPrice != null) {
            if (itemUnitPrice.scale() < PurapConstants.DOLLAR_AMOUNT_MIN_SCALE) {
                itemUnitPrice = itemUnitPrice.setScale(PurapConstants.DOLLAR_AMOUNT_MIN_SCALE,
                        KualiDecimal.ROUND_BEHAVIOR);
            } else if (itemUnitPrice.scale() > PurapConstants.UNIT_PRICE_MAX_SCALE) {
                itemUnitPrice = itemUnitPrice.setScale(PurapConstants.UNIT_PRICE_MAX_SCALE,
                        KualiDecimal.ROUND_BEHAVIOR);
            }
        }
        this.itemUnitPrice = itemUnitPrice;
    }

    @Override
    public String getItemTypeCode() {
        return itemTypeCode;
    }

    @Override
    public void setItemTypeCode(final String itemTypeCode) {
        this.itemTypeCode = itemTypeCode;
    }

    @Override
    public String getItemAuxiliaryPartIdentifier() {
        return itemAuxiliaryPartIdentifier;
    }

    @Override
    public void setItemAuxiliaryPartIdentifier(final String itemAuxiliaryPartIdentifier) {
        this.itemAuxiliaryPartIdentifier = itemAuxiliaryPartIdentifier;
    }

    @Override
    public String getExternalOrganizationB2bProductReferenceNumber() {
        return externalOrganizationB2bProductReferenceNumber;
    }

    @Override
    public void setExternalOrganizationB2bProductReferenceNumber(final String externalOrganizationB2bProductReferenceNumber) {
        this.externalOrganizationB2bProductReferenceNumber = externalOrganizationB2bProductReferenceNumber;
    }

    @Override
    public String getExternalOrganizationB2bProductTypeName() {
        return externalOrganizationB2bProductTypeName;
    }

    @Override
    public void setExternalOrganizationB2bProductTypeName(final String externalOrganizationB2bProductTypeName) {
        this.externalOrganizationB2bProductTypeName = externalOrganizationB2bProductTypeName;
    }

    @Override
    public boolean getItemAssignedToTradeInIndicator() {
        return itemAssignedToTradeInIndicator;
    }

    @Override
    public void setItemAssignedToTradeInIndicator(final boolean itemAssignedToTradeInIndicator) {
        this.itemAssignedToTradeInIndicator = itemAssignedToTradeInIndicator;
    }

    @Override
    public ItemType getItemType() {
        if (ObjectUtils.isNull(itemType) || !itemType.getItemTypeCode().equals(itemTypeCode)) {
            refreshReferenceObject(PurapPropertyConstants.ITEM_TYPE);
        }
        return itemType;
    }

    @Override
    @Deprecated
    public void setItemType(final ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public KualiDecimal getItemTaxAmount() {
        KualiDecimal taxAmount = KualiDecimal.ZERO;

        if (ObjectUtils.isNull(purapDocument)) {
            refreshReferenceObject("purapDocument");
        }

        if (!purapDocument.isUseTaxIndicator()) {
            taxAmount = itemSalesTaxAmount;
        } else {
            // sum use tax item tax amounts
            for (final PurApItemUseTax useTaxItem : getUseTaxItems()) {
                taxAmount = taxAmount.add(useTaxItem.getTaxAmount());
            }
        }

        return taxAmount;
    }

    @Override
    public void setItemTaxAmount(final KualiDecimal itemTaxAmount) {

        if (purapDocument == null) {
            refreshReferenceObject("purapDocument");
        }

        if (!purapDocument.isUseTaxIndicator()) {
            itemSalesTaxAmount = itemTaxAmount;
        }

    }

    public final KualiDecimal getItemSalesTaxAmount() {
        return itemSalesTaxAmount;
    }

    public final void setItemSalesTaxAmount(final KualiDecimal itemSalesTaxAmount) {
        this.itemSalesTaxAmount = itemSalesTaxAmount;
    }

    @Override
    public KualiDecimal getExtendedPrice() {
        return calculateExtendedPrice();
    }

    @Override
    public KualiDecimal getTotalAmount() {
        KualiDecimal totalAmount = getExtendedPrice();
        if (ObjectUtils.isNull(totalAmount)) {
            totalAmount = KualiDecimal.ZERO;
        }

        KualiDecimal taxAmount = getItemTaxAmount();
        if (ObjectUtils.isNull(taxAmount)) {
            taxAmount = KualiDecimal.ZERO;
        }

        totalAmount = totalAmount.add(taxAmount);

        return totalAmount;
    }

    @Override
    public void setTotalAmount(final KualiDecimal totalAmount) {
        // do nothing, setter required by interface
    }

    @Override
    public KualiDecimal calculateExtendedPrice() {
        KualiDecimal extendedPrice = KualiDecimal.ZERO;
        if (ObjectUtils.isNotNull(itemUnitPrice)) {
            if (itemType.isAmountBasedGeneralLedgerIndicator()) {
                // SERVICE ITEM: return unit price as extended price
                extendedPrice = new KualiDecimal(itemUnitPrice.toString());
            } else if (ObjectUtils.isNotNull(getItemQuantity())) {
                final BigDecimal calcExtendedPrice = itemUnitPrice.multiply(itemQuantity.bigDecimalValue());
                // ITEM TYPE (qty driven): return (unitPrice x qty)
                extendedPrice = new KualiDecimal(calcExtendedPrice.setScale(KualiDecimal.SCALE,
                        KualiDecimal.ROUND_BEHAVIOR));
            }
        }
        return extendedPrice;
    }

    @Override
    public void setExtendedPrice(final KualiDecimal extendedPrice) {
        this.extendedPrice = extendedPrice;
    }

    @Override
    public List<PurApAccountingLine> getSourceAccountingLines() {
        return sourceAccountingLines;
    }

    @Override
    public void setSourceAccountingLines(final List<PurApAccountingLine> accountingLines) {
        sourceAccountingLines = accountingLines;
    }

    @Override
    public List<PurApAccountingLine> getBaselineSourceAccountingLines() {
        return baselineSourceAccountingLines;
    }

    public void setBaselineSourceAccountingLines(final List<PurApAccountingLine> baselineSourceLines) {
        baselineSourceAccountingLines = baselineSourceLines;
    }

    /**
     * This implementation is coupled tightly with some underlying issues that the Struts PojoProcessor plugin has
     * with how objects get instantiated within lists. The first three lines are required otherwise when the
     * PojoProcessor tries to automatically inject values into the list, it will get an index out of bounds error if
     * the instance at an index is being called and prior instances at indices before that one are not being
     * instantiated. So changing the code below will cause adding lines to break if you add more than one item to the
     * list.
     */
    public PurApAccountingLine getSourceAccountingLine(final int index) {
        while (getSourceAccountingLines().size() <= index) {
            final PurApAccountingLine newAccount = getNewAccount();
            getSourceAccountingLines().add(newAccount);
        }
        return getSourceAccountingLines().get(index);
    }

    /**
     * This implementation is coupled tightly with some underlying issues that the Struts PojoProcessor plugin has
     * with how objects get instantiated within lists. The first three lines are required otherwise when the
     * PojoProcessor tries to automatically inject values into the list, it will get an index out of bounds error if
     * the instance at an index is being called and prior instances at indices before that one are not being
     * instantiated. So changing the code below will cause adding lines to break if you add more than one item to the
     * list.
     */
    public PurApAccountingLine getBaselineSourceAccountingLine(final int index) {
        while (getBaselineSourceAccountingLines().size() <= index) {
            final PurApAccountingLine newAccount = getNewAccount();
            getBaselineSourceAccountingLines().add(newAccount);
        }
        return getBaselineSourceAccountingLines().get(index);
    }

    private PurApAccountingLine getNewAccount() throws RuntimeException {
        final Class accountingLineClass = getAccountingLineClass();
        if (accountingLineClass == null) {
            throw new RuntimeException("Can't instantiate Purchasing Account from base");
        }

        final PurApAccountingLine newAccount;
        try {
            newAccount = (PurApAccountingLine) accountingLineClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to get class");
        }
        return newAccount;
    }

    @Override
    public abstract Class getAccountingLineClass();

    @Override
    public abstract Class getUseTaxClass();

    @Override
    public void resetAccount() {
        // add a blank accounting line
        final PurApAccountingLine purApAccountingLine = getNewAccount();

        purApAccountingLine.setItemIdentifier(itemIdentifier);
        purApAccountingLine.setPurapItem(this);
        purApAccountingLine.setSequenceNumber(0);
        setNewSourceLine(purApAccountingLine);
    }

    @Override
    public List buildListOfDeletionAwareLists() {
        final List managedLists = new ArrayList();
        managedLists.add(getSourceAccountingLines());
        return managedLists;
    }

    @Override
    public PurApAccountingLine getNewSourceLine() {
        return newSourceLine;
    }

    @Override
    public void setNewSourceLine(final PurApAccountingLine newAccountingLine) {
        newSourceLine = newAccountingLine;
    }

    @Override
    public Integer getPurapDocumentIdentifier() {
        return purapDocumentIdentifier;
    }

    @Override
    public void setPurapDocumentIdentifier(final Integer purapDocumentIdentifier) {
        this.purapDocumentIdentifier = purapDocumentIdentifier;
    }

    @Override
    public List<PurApItemUseTax> getUseTaxItems() {
        return useTaxItems;
    }

    @Override
    public void setUseTaxItems(final List<PurApItemUseTax> useTaxItems) {
        this.useTaxItems = useTaxItems;
    }

    @Override
    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }

    @Override
    public void setItemQuantity(final KualiDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public boolean isAccountListEmpty() {
        final List<PurApAccountingLine> accounts = getSourceAccountingLines();
        if (ObjectUtils.isNotNull(accounts)) {
            for (final PurApAccountingLine element : accounts) {
                if (!element.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PurApSummaryItem getSummaryItem() {
        final PurApSummaryItem summaryItem = new PurApSummaryItem();
        ObjectPopulationUtils.populateFromBaseClass(PurApItemBase.class, this, summaryItem, new HashMap<>());
        summaryItem.getItemType().setItemTypeDescription(itemType.getItemTypeDescription());
        return summaryItem;
    }

    @Override
    public final <T extends PurchasingAccountsPayableDocument> T getPurapDocument() {
        return (T) purapDocument;
    }

    @Override
    public final void setPurapDocument(final PurchasingAccountsPayableDocument purapDoc) {
        purapDocument = purapDoc;
    }

    @Override
    public void fixAccountReferences() {
        if (ObjectUtils.isNull(getItemIdentifier())) {
            for (final PurApAccountingLine account : getSourceAccountingLines()) {
                account.setSequenceNumber(0);
                account.setPurapItem(this);
            }
        }
    }

    @Override
    public void refreshNonUpdateableReferences() {
        PurchasingAccountsPayableDocument document = null;
        final PurchasingAccountsPayableDocument tempDocument = getPurapDocument();
        if (tempDocument != null) {
            final Integer tempDocumentIdentifier = tempDocument.getPurapDocumentIdentifier();
            if (tempDocumentIdentifier != null) {
                document = getPurapDocument();
            }
        }
        super.refreshNonUpdateableReferences();
        if (ObjectUtils.isNotNull(document)) {
            setPurapDocument(document);
        }
    }

    @Override
    public KualiDecimal getTotalRemitAmount() {
        if (!purapDocument.isUseTaxIndicator()) {
            return getTotalAmount();
        }
        return getExtendedPrice();
    }

    @Override
    public String toString() {
        return "Line " + (itemLineNumber == null ? "(null)" : itemLineNumber.toString()) + ": [" + itemTypeCode +
                "] " + "Unit:" + (itemUnitPrice == null ? "(null)" : itemUnitPrice.toString()) + " " + "Tax:" +
                (itemSalesTaxAmount == null ? "(null)" : itemSalesTaxAmount.toString()) + " " + "*" +
                itemDescription + "*";
    }

    // KFSUPGRADE-485
    public boolean isNoQtyItem() {
    	return StringUtils.equals(itemTypeCode, PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE);
    }

}
