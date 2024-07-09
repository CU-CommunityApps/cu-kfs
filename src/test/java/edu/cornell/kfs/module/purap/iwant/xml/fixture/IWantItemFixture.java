package edu.cornell.kfs.module.purap.iwant.xml.fixture;

import java.math.BigDecimal;

import edu.cornell.kfs.module.purap.iwant.xml.IWantItemXml;

public enum IWantItemFixture {

    ITEM_TEST("unit of measure", "cat number", "item description", BigDecimal.valueOf(5.29), "code",
            BigDecimal.valueOf(2.0));

    public final String itemUnitOfMeasureCode;
    public final String itemCatalogNumber;
    public final String itemDescription;
    public final BigDecimal itemUnitPrice;
    public final String purchasingCommodityCode;
    public final BigDecimal itemQuantity;

    private IWantItemFixture(String itemUnitOfMeasureCode, String itemCatalogNumber, String itemDescription,
            BigDecimal itemUnitPrice, String purchasingCommodityCode, BigDecimal itemQuantity) {
        this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
        this.itemCatalogNumber = itemCatalogNumber;
        this.itemDescription = itemDescription;
        this.itemUnitPrice = itemUnitPrice;
        this.purchasingCommodityCode = purchasingCommodityCode;
        this.itemQuantity = itemQuantity;
    }

    public IWantItemXml toIWantItemXml() {
        IWantItemXml item = new IWantItemXml();
        item.setItemCatalogNumber(itemCatalogNumber);
        item.setItemDescription(itemDescription);
        item.setItemQuantity(itemQuantity);
        item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
        item.setItemUnitPrice(itemUnitPrice);
        item.setPurchasingCommodityCode(purchasingCommodityCode);
        return item;
    }
}
