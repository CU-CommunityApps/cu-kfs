package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import org.kuali.kfs.module.purap.businessobject.PurApItem;

public class PurchaseOrderItemDto {
    
    private String poLineNumber;
    private String quantity;
    private String unitOfMeasure;
    private String catalogNumber;
    private String description;
    private String lineItemCost;
    private String totalLineItemCost;
    private String shipping;
    private String miscellaneous;
    private String tax;
    
    public PurchaseOrderItemDto() {
        
    }
    
    public PurchaseOrderItemDto(PurApItem item) {
        this.poLineNumber = String.valueOf(item.getItemLineNumber());
        this.quantity = item.getItemQuantity().toString();
        this.unitOfMeasure = item.getItemUnitOfMeasureCode();
        this.catalogNumber = item.getItemCatalogNumber();
        this.description = item.getItemDescription();
        this.lineItemCost = String.valueOf(item.getItemUnitPrice());
        //this.totalLineItemCost = item.getco
        //this.shipping
        //this.miscellaneous = item.getmi
        this.tax = item.getItemTaxAmount().toString();
    }

}
