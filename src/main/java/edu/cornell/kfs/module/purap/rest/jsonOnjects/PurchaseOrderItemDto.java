package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PurApItem;

public class PurchaseOrderItemDto {
    
    private String poLineNumber;
    private String quantity;
    private String unitOfMeasure;
    private String catalogNumber;
    private String description;
    private String lineItemCost;
    private String tax;
    private String itemTypeCode;
    private String totalLineItemCost;
    
    public PurchaseOrderItemDto() {
        
    }
    
    public PurchaseOrderItemDto(PurApItem item) {
        this.poLineNumber = item.getItemLineNumber() != null ? item.getItemLineNumber().toString() : StringUtils.EMPTY;
        this.quantity = item.getItemQuantity() != null ? item.getItemQuantity().toString() : StringUtils.EMPTY;
        this.unitOfMeasure = item.getItemUnitOfMeasureCode();
        this.catalogNumber = item.getItemCatalogNumber();
        this.description = item.getItemDescription();
        this.lineItemCost = item.getItemUnitPrice() != null ? item.getItemUnitPrice().toPlainString() : StringUtils.EMPTY;
        this.tax = item.getItemTaxAmount() != null ?  item.getItemTaxAmount().toString() : StringUtils.EMPTY;
        this.itemTypeCode = item.getItemTypeCode();
        this.totalLineItemCost = item.getTotalAmount() != null ? item.getTotalAmount().toString() : StringUtils.EMPTY;
    }

    public String getPoLineNumber() {
        return poLineNumber;
    }

    public void setPoLineNumber(String poLineNumber) {
        this.poLineNumber = poLineNumber;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLineItemCost() {
        return lineItemCost;
    }

    public void setLineItemCost(String lineItemCost) {
        this.lineItemCost = lineItemCost;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getItemTypeCode() {
        return itemTypeCode;
    }

    public void setItemTypeCode(String itemTypeCode) {
        this.itemTypeCode = itemTypeCode;
    }

    public String getTotalLineItemCost() {
        return totalLineItemCost;
    }

    public void setTotalLineItemCost(String totalLineItemCost) {
        this.totalLineItemCost = totalLineItemCost;
    }

}
