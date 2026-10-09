package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItemUseTax;

/*
 * Lightweight implementation of PurchaseOrderItem that retrieves the needed PUR_PO_ITM_T fields
 * without loading other unneeded reference objects. This dramatically improves the batch job's execution time.
 */
public class CemiPurchaseOrderItemLite extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private Integer itemIdentifier;
    private Integer itemLineNumber;
    private String itemTypeCode;
    private String itemUnitOfMeasureCode;
    private KualiDecimal itemQuantity;
    private KualiDecimal itemInvoicedTotalQuantity;
    private KualiDecimal itemInvoicedTotalAmount;
    private String itemCatalogNumber;
    private KualiDecimal itemReceivedTotalQuantity;
    private String itemDescription;
    private BigDecimal itemUnitPrice;
    private KualiDecimal itemOutstandingEncumberedQuantity;
    private KualiDecimal itemOutstandingEncumberedAmount;
    private String itemAuxiliaryPartIdentifier;
    private boolean itemActiveIndicator;
    private String purchasingCommodityCode;
    private String externalOrganizationB2bProductReferenceNumber;
    private String externalOrganizationB2bProductTypeName;
    private boolean itemAssignedToTradeInIndicator;
    private KualiDecimal itemDamagedTotalQuantity;
    private KualiDecimal itemSalesTaxAmount;
    private boolean controlled;
    private boolean green;
    private boolean hazardous;
    private boolean radioactive;
    private boolean radioactiveMinor;
    private boolean selectAgent;
    private boolean toxin;
    private boolean recycled;
    private boolean energyStar;

    private List<PurchaseOrderItemUseTax> useTaxItems;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    public void setItemIdentifier(final Integer itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    public Integer getItemLineNumber() {
        return itemLineNumber;
    }

    public void setItemLineNumber(final Integer itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

    public String getItemTypeCode() {
        return itemTypeCode;
    }

    public void setItemTypeCode(final String itemTypeCode) {
        this.itemTypeCode = itemTypeCode;
    }

    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    public void setItemUnitOfMeasureCode(final String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
    }

    public KualiDecimal getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(final KualiDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public KualiDecimal getItemInvoicedTotalQuantity() {
        return itemInvoicedTotalQuantity;
    }

    public void setItemInvoicedTotalQuantity(final KualiDecimal itemInvoicedTotalQuantity) {
        this.itemInvoicedTotalQuantity = itemInvoicedTotalQuantity;
    }

    public KualiDecimal getItemInvoicedTotalAmount() {
        return itemInvoicedTotalAmount;
    }

    public void setItemInvoicedTotalAmount(final KualiDecimal itemInvoicedTotalAmount) {
        this.itemInvoicedTotalAmount = itemInvoicedTotalAmount;
    }

    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    public void setItemCatalogNumber(final String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    public KualiDecimal getItemReceivedTotalQuantity() {
        return itemReceivedTotalQuantity;
    }

    public void setItemReceivedTotalQuantity(final KualiDecimal itemReceivedTotalQuantity) {
        this.itemReceivedTotalQuantity = itemReceivedTotalQuantity;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(final String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public BigDecimal getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(final BigDecimal itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public KualiDecimal getItemOutstandingEncumberedQuantity() {
        return itemOutstandingEncumberedQuantity;
    }

    public void setItemOutstandingEncumberedQuantity(final KualiDecimal itemOutstandingEncumberedQuantity) {
        this.itemOutstandingEncumberedQuantity = itemOutstandingEncumberedQuantity;
    }

    public KualiDecimal getItemOutstandingEncumberedAmount() {
        return itemOutstandingEncumberedAmount;
    }

    public void setItemOutstandingEncumberedAmount(final KualiDecimal itemOutstandingEncumberedAmount) {
        this.itemOutstandingEncumberedAmount = itemOutstandingEncumberedAmount;
    }

    public String getItemAuxiliaryPartIdentifier() {
        return itemAuxiliaryPartIdentifier;
    }

    public void setItemAuxiliaryPartIdentifier(final String itemAuxiliaryPartIdentifier) {
        this.itemAuxiliaryPartIdentifier = itemAuxiliaryPartIdentifier;
    }

    public boolean isItemActiveIndicator() {
        return itemActiveIndicator;
    }

    public void setItemActiveIndicator(final boolean itemActiveIndicator) {
        this.itemActiveIndicator = itemActiveIndicator;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(final String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public String getExternalOrganizationB2bProductReferenceNumber() {
        return externalOrganizationB2bProductReferenceNumber;
    }

    public void setExternalOrganizationB2bProductReferenceNumber(final String externalOrganizationB2bProductReferenceNumber) {
        this.externalOrganizationB2bProductReferenceNumber = externalOrganizationB2bProductReferenceNumber;
    }

    public String getExternalOrganizationB2bProductTypeName() {
        return externalOrganizationB2bProductTypeName;
    }

    public void setExternalOrganizationB2bProductTypeName(final String externalOrganizationB2bProductTypeName) {
        this.externalOrganizationB2bProductTypeName = externalOrganizationB2bProductTypeName;
    }

    public boolean isItemAssignedToTradeInIndicator() {
        return itemAssignedToTradeInIndicator;
    }

    public void setItemAssignedToTradeInIndicator(final boolean itemAssignedToTradeInIndicator) {
        this.itemAssignedToTradeInIndicator = itemAssignedToTradeInIndicator;
    }

    public KualiDecimal getItemDamagedTotalQuantity() {
        return itemDamagedTotalQuantity;
    }

    public void setItemDamagedTotalQuantity(final KualiDecimal itemDamagedTotalQuantity) {
        this.itemDamagedTotalQuantity = itemDamagedTotalQuantity;
    }

    public KualiDecimal getItemSalesTaxAmount() {
        return itemSalesTaxAmount;
    }

    public void setItemSalesTaxAmount(final KualiDecimal itemSalesTaxAmount) {
        this.itemSalesTaxAmount = itemSalesTaxAmount;
    }

    public boolean isControlled() {
        return controlled;
    }

    public void setControlled(final boolean controlled) {
        this.controlled = controlled;
    }

    public boolean isGreen() {
        return green;
    }

    public void setGreen(final boolean green) {
        this.green = green;
    }

    public boolean isHazardous() {
        return hazardous;
    }

    public void setHazardous(final boolean hazardous) {
        this.hazardous = hazardous;
    }

    public boolean isRadioactive() {
        return radioactive;
    }

    public void setRadioactive(final boolean radioactive) {
        this.radioactive = radioactive;
    }

    public boolean isRadioactiveMinor() {
        return radioactiveMinor;
    }

    public void setRadioactiveMinor(final boolean radioactiveMinor) {
        this.radioactiveMinor = radioactiveMinor;
    }

    public boolean isSelectAgent() {
        return selectAgent;
    }

    public void setSelectAgent(final boolean selectAgent) {
        this.selectAgent = selectAgent;
    }

    public boolean isToxin() {
        return toxin;
    }

    public void setToxin(final boolean toxin) {
        this.toxin = toxin;
    }

    public boolean isRecycled() {
        return recycled;
    }

    public void setRecycled(final boolean recycled) {
        this.recycled = recycled;
    }

    public boolean isEnergyStar() {
        return energyStar;
    }

    public void setEnergyStar(final boolean energyStar) {
        this.energyStar = energyStar;
    }

    public List<PurchaseOrderItemUseTax> getUseTaxItems() {
        if (useTaxItems == null) {
            useTaxItems = new ArrayList<>();
        }
        return useTaxItems;
    }

    public void setUseTaxItems(final List<PurchaseOrderItemUseTax> useTaxItems) {
        this.useTaxItems = useTaxItems;
    }

}
