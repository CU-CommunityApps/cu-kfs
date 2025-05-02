package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PurApItem;

public class PurchaseOrderItemDto {

    private String itemLineNumber;
    private String itemQuantity;
    private String itemUnitOfMeasureCode;
    private String itemCatalogNumber;
    private String itemDescription;
    private String itemUnitPrice;
    private String itemTaxAmount;
    private String itemTypeCode;
    private String itemTotalAmount;

    public PurchaseOrderItemDto() {

    }

    public PurchaseOrderItemDto(PurApItem item) {
        this.itemLineNumber = item.getItemLineNumber() != null ? item.getItemLineNumber().toString() : StringUtils.EMPTY;
        this.itemQuantity = item.getItemQuantity() != null ? item.getItemQuantity().toString() : StringUtils.EMPTY;
        this.itemUnitOfMeasureCode = item.getItemUnitOfMeasureCode();
        this.itemCatalogNumber = item.getItemCatalogNumber();
        this.itemDescription = item.getItemDescription();
        this.itemUnitPrice = item.getItemUnitPrice() != null ? item.getItemUnitPrice().toPlainString()
                : StringUtils.EMPTY;
        this.itemTaxAmount = item.getItemTaxAmount() != null ? item.getItemTaxAmount().toString() : StringUtils.EMPTY;
        this.itemTypeCode = item.getItemTypeCode();
        this.itemTotalAmount = item.getTotalAmount() != null ? item.getTotalAmount().toString() : StringUtils.EMPTY;
    }

    public String getItemLineNumber() {
        return itemLineNumber;
    }

    public void setItemLineNumber(String itemLineNumber) {
        this.itemLineNumber = itemLineNumber;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemUnitOfMeasureCode() {
        return itemUnitOfMeasureCode;
    }

    public void setItemUnitOfMeasureCode(String itemUnitOfMeasureCode) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
    }

    public String getItemCatalogNumber() {
        return itemCatalogNumber;
    }

    public void setItemCatalogNumber(String itemCatalogNumber) {
        this.itemCatalogNumber = itemCatalogNumber;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(String itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public String getItemTaxAmount() {
        return itemTaxAmount;
    }

    public void setItemTaxAmount(String itemTaxAmount) {
        this.itemTaxAmount = itemTaxAmount;
    }

    public String getItemTypeCode() {
        return itemTypeCode;
    }

    public void setItemTypeCode(String itemTypeCode) {
        this.itemTypeCode = itemTypeCode;
    }

    public String getItemTotalAmount() {
        return itemTotalAmount;
    }

    public void setItemTotalAmount(String itemTotalAmount) {
        this.itemTotalAmount = itemTotalAmount;
    }

}
