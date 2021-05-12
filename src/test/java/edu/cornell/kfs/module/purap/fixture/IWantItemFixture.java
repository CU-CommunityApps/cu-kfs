package edu.cornell.kfs.module.purap.fixture;

import java.math.BigDecimal;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.businessobject.IWantItem;

public enum IWantItemFixture {
	I_WANT_ITEM(new Integer(1), new Integer(1), "EA", "823542376", "Item DEsc",
			new BigDecimal(10), "37654", new KualiDecimal(1));

	// private String documentNumber;
	private Integer itemIdentifier;
	private Integer itemLineNumber;
	private String itemUnitOfMeasureCode;
	private String itemCatalogNumber;
	private String itemDescription;
	private BigDecimal itemUnitPrice;
	private String purchasingCommodityCode;
	private KualiDecimal itemQuantity;

	private IWantItemFixture(Integer itemIdentifier, Integer itemLineNumber,
			String itemUnitOfMeasureCode, String itemCatalogNumber,
			String itemDescription, BigDecimal itemUnitPrice,
			String purchasingCommodityCode, KualiDecimal itemQuantity) {
		this.itemIdentifier = itemIdentifier;
		this.itemLineNumber = itemLineNumber;
		this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
		this.itemCatalogNumber = itemCatalogNumber;
		this.itemDescription = itemDescription;
		this.itemUnitPrice = itemUnitPrice;
		this.purchasingCommodityCode = purchasingCommodityCode;
		this.itemQuantity = itemQuantity;

	}

	public IWantItem createIWantItem(String iWantDocNumber) {
		IWantItem item = new IWantItem();

		item.setDocumentNumber(iWantDocNumber);
		item.setItemIdentifier(itemIdentifier);
		item.setItemLineNumber(itemLineNumber);
		item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
		item.setItemCatalogNumber(itemCatalogNumber);
		item.setItemDescription(itemDescription);
		item.setItemUnitPrice(itemUnitPrice);
		item.setPurchasingCommodityCode(purchasingCommodityCode);
		item.setItemQuantity(itemQuantity);

		return item;

	}

}
