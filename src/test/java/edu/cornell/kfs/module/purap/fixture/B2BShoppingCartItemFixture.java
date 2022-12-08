package edu.cornell.kfs.module.purap.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.businessobject.B2BShoppingCartItem;

/**
 * Fixture class for B2BShoppingCartItem.
 */
public enum B2BShoppingCartItemFixture {

    B2B_ITEM_USING_VENDOR_ID (
            "10", // quantity
            "A0125156", // supplierPartId
            "1012273985063\1", // supplierPartAuxiliaryId
            "252.60", // unitPrice
            "USD", // unitPriceCurrency
            "01:00 Kensington Microsaver Laptop Lock - security cable lock", // description
            "EA", // unitOfMeasure
            "64068F", // manufacturerPartID
            "Dell", // manufacturerName
            "4130-0", // externalSupplierId - vendorID
            "Punchout", // productSource
            "", // systemProductID
            //"624007902" // SupplierID-DUNS: value in KFS
            "002617843", // SupplierID-DUNS: value from SciQuest
            "14035", // SupplierID-SystemSupplierID
            "Cart name", // Cart Name
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true"
    ),
    B2B_ITEM_WITH_LONG_DESCRIPTION(
            "10", // quantity
            "A0125156", // supplierPartId
            "1012273985063\1", // supplierPartAuxiliaryId
            "252.60", // unitPrice
            "USD", // unitPriceCurrency
            "01:00 Kensington Microsaver Laptop Lock - security cable lock 01:00 Kensington Microsaver Laptop Lock - security cable lock 01:00 Kensington"
                    + " Microsaver Laptop Lock - security cable lock Microsaver Laptop Lock - security cable lock Microsaver Laptop Lock - security cable lock",
            "EA", // unitOfMeasure
            "64068F", // manufacturerPartID
            "Dell", // manufacturerName
            "4130-0", // externalSupplierId - vendorID
            "Punchout", // productSource
            "", // systemProductID
            "002617843", // SupplierID-DUNS: value from SciQuest
            "14035", // SupplierID-SystemSupplierID
            "Cart name", // Cart Name
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true",
            "true"
    );

    public String quantity;
    public String supplierPartId;
    public String supplierPartAuxiliaryId;
    public String unitPrice;
    public String unitPriceCurrency;
    public String description;
    public String unitOfMeasure;
    public String manufacturerPartID;
    public String manufacturerName;
    public String externalSupplierId;
    public String productSource;
    public String systemProductID;
    public String duns;
    public String systemSupplierID;
    public String cartName;
    public String controlled;
    public String radioactiveMinor;
    public String greenProduct;
    public String hazardous;
    public String selectAgent;
    public String radioactive;
    public String toxin;
    public String green;
    public String energyStar;

    private B2BShoppingCartItemFixture(
            String quantity,
            String supplierPartId,
            String supplierPartAuxiliaryId,
            String unitPrice,
            String unitPriceCurrency,
            String description,
            String unitOfMeasure,
            String manufacturerPartID,
            String manufacturerName,
            String externalSupplierId,
            String productSource,
            String systemProductID,
            String duns,
            String systemSupplierID,
            String cartName,
            String controlled,
            String radioactiveMinor,
            String greenProduct,
            String hazardous,
            String selectAgent,
            String radioactive,
            String toxin,
            String green,
            String energyStar
    ) {
        this.quantity = quantity;
        this.supplierPartId = supplierPartId;
        this.supplierPartAuxiliaryId = supplierPartAuxiliaryId;
        this.unitPrice = unitPrice;
        this.unitPriceCurrency = unitPriceCurrency;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.manufacturerPartID = manufacturerPartID;
        this.manufacturerName = manufacturerName;
        this.externalSupplierId = externalSupplierId;
        this.productSource = productSource;
        this.systemProductID = systemProductID;                
        this.duns = duns;
        this.systemSupplierID = systemSupplierID;
        this.cartName = cartName;
        this.controlled = controlled;
        this.radioactiveMinor = radioactiveMinor;
        this.greenProduct = greenProduct;
        this.hazardous = hazardous;
        this.selectAgent = selectAgent;
        this.radioactive = radioactive;
        this.toxin = toxin;
        this.green = green;
        this.energyStar = energyStar;
    }
    
    /**
     * Creates a B2BShoppingCartItem from this B2BShoppingCartItemFixture.
     */
    public B2BShoppingCartItem createB2BShoppingCartItem() {
        B2BShoppingCartItem item = new B2BShoppingCartItem();
        
        item.setQuantity(quantity);
        item.setSupplierPartId(supplierPartId);
        item.setSupplierPartAuxiliaryId(supplierPartAuxiliaryId);
        
        item.setUnitPrice(unitPrice);
        item.setUnitPriceCurrency(unitPriceCurrency);
        item.setDescription(description);
        item.setUnitOfMeasure(unitOfMeasure);
        item.setManufacturerPartID(manufacturerPartID);
        item.setManufacturerName(manufacturerName);
        
        item.addExtrinsic("ExternalSupplierId", externalSupplierId);
        item.addExtrinsic("Product Source", productSource);
        item.addExtrinsic("SystemProductID", systemProductID);
        item.addExtrinsic("CartName", cartName);
        
        item.addClassification("Controlled", controlled);
        item.addClassification("RadioactiveMinor", radioactiveMinor);
        item.addClassification("GreenProduct", greenProduct);
        item.addClassification("Hazardous", hazardous);
        item.addClassification("SelectAgent", selectAgent);
        item.addClassification("Radioactive", radioactive);
        item.addClassification("Toxin", toxin);
        item.addClassification("Green", green);
        item.addClassification("EnergyStar", energyStar);
        
        item.setSupplier("DUNS", duns);
        item.setSupplier("SystemSupplierID", systemSupplierID);
        
        return item;
    }
}
