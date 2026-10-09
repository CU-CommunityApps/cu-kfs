package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class CemiLegacyPurchaseOrderItem {

    private String documentNumber;
    private Integer purchaseOrderId;
    private Integer itemIdentifier;
    private Integer itemLineNumber;
    private String itemTypeCode;
    private String itemCatalogNumber;
    private String purchasingCommodityCode;
    private String itemDescription;
    private String itemUnitOfMeasureCode;
    private BigDecimal itemUnitPrice;
    private KualiDecimal itemOutstandingEncumberedQuantity;
    private KualiDecimal itemOutstandingEncumberedAmount;
    private KualiDecimal totalAmount;
    private List<CemiLegacyPurchaseOrderAccount> accountingLines = new ArrayList<>();

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(final Integer purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
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

    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    public void setItemCatalogNumber(final String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(final String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(final String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    public void setItemUnitOfMeasureCode(final String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
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

    public KualiDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(final KualiDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /*
     * Contains only the accounting lines with outstanding encumbrances, in ascending order by account identifier.
     */
    public List<CemiLegacyPurchaseOrderAccount> getAccountingLines() {
        return accountingLines;
    }

    public void setAccountingLines(final List<CemiLegacyPurchaseOrderAccount> accountingLines) {
        this.accountingLines = accountingLines;
    }

}
