package edu.cornell.kfs.module.purap.fixture;

import java.math.BigDecimal;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public enum RequisitionItemFixture {
	REQ_ITEM(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(1), new KualiDecimal(1), "80141605",
			new BigDecimal(1), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

	REQ_ITEM2(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(4), new KualiDecimal(1), "80141605",
			new BigDecimal(4), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE2,
			false),

	REQ_ITEM3(new Integer(2), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(2), new KualiDecimal(1), "80141605",
			new BigDecimal(2), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3,
			false),

	REQ_ITEM_INACTIVE_COMM_CD(new Integer(1), "EA", "1234567", "item desc",
			"ITEM", "Punchout", new KualiDecimal(1), new KualiDecimal(1),
			"24112404", new BigDecimal(1),
			PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE, false),

	REQ_ITEM_MISC_TRADE_IN(new Integer(1), "EA", "1234567", "item desc",
			"TRDI", "Punchout", KualiDecimal.ZERO, KualiDecimal.ZERO,
			"80141605", new BigDecimal(0),
			PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE, false),

	REQ_ITEM_TRADE_IN(new Integer(1), "EA", "1234567", "item desc", "ITEM",
			"Punchout", new KualiDecimal(1), new KualiDecimal(1), "80141605",
			new BigDecimal(1), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			true),

	REQ_NON_QTY_ITEM_AMOUNT_BELOW_5K(new Integer(1), null, "1234567", "item desc", "SRVC", "Punchout",
			new KualiDecimal(4000), null, "80141605",
			new BigDecimal(4000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

	REQ_NON_QTY_ITEM_AMOUNT_AT_5K(new Integer(1), null, "1234567", "item desc", "SRVC", "Punchout",
			new KualiDecimal(5000), null, "80141605",
			new BigDecimal(5000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),			

	REQ_NON_QTY_ITEM_AMOUNT_ABOVE_5K(new Integer(1), null, "1234567", "item desc", "SRVC", "Punchout",
			new KualiDecimal(6000), null, "80141605",
			new BigDecimal(6000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

	REQ_QTY_ITEM_AMOUNT_BELOW_5K(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(4000), new KualiDecimal(1), "80141605",
			new BigDecimal(4000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

	REQ_QTY_ITEM_AMOUNT_AT_5K(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(5000), new KualiDecimal(1), "80141605",
			new BigDecimal(5000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

	REQ_QTY_ITEM_AMOUNT_ABOVE_5K(new Integer(1), "EA", "1234567", "item desc", "ITEM", "Punchout",
			new KualiDecimal(6000), new KualiDecimal(1), "80141605",
			new BigDecimal(6000), PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE,
			false),

    REQ_QTY_ITEM_AMOUNT_AT_8K_NO_ACCT_LINE(1, "EA", "1234567", "item desc", "ITEM", "Punchout",
            8000, 2, "80141605", 4000, null, false),

    REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA(1, "EA", "1234567", "item desc", "ITEM", "Punchout",
            8000, 2, "80141605", 4000, PurapAccountingLineFixture.REQ_ITEM_ACCT_WITH_CFDA_NUMBER, false),

    REQ_QTY_ITEM_AMOUNT_AT_8K_NO_CFDA(1, "EA", "1234567", "item desc", "ITEM", "Punchout",
            8000, 2, "80141605", 4000, PurapAccountingLineFixture.REQ_ITEM_ACCT_WITHOUT_CFDA_NUMBER, false),

    REQ_QTY_ITEM_AMOUNT_AT_15K_NO_CFDA(2, "EA", "4444444", "item desc 2", "ITEM", "Punchout",
            15000, 1, "80141605", 15000, PurapAccountingLineFixture.REQ_ITEM_ACCT_WITHOUT_CFDA_NUMBER, false),

    REQ_QTY_ITEM_AMOUNT_AT_15K_WITH_CFDA(2, "EA", "4444444", "item desc 2", "ITEM", "Punchout",
            15000, 1, "80141605", 15000, PurapAccountingLineFixture.REQ_ITEM_ACCT_WITH_CFDA_NUMBER, false);

    public static final int BASE_ITEM_ID = 1000;

	public final Integer itemLineNumber;
	public final String itemUnitOfMeasureCode;
	public final String itemCatalogNumber;
	public final String itemDescription;
	public final String itemTypeCode;
	public final String externalOrganizationB2bProductTypeName;
	public final KualiDecimal extendedPrice;
	public final KualiDecimal itemQuantity;
	public final String purchasingCommodityCode;
	public final BigDecimal itemUnitPrice;

	public final PurapAccountingLineFixture accountingLineFixture;
	public final boolean itemAssignedToTradeInIndicator;

    private RequisitionItemFixture(int itemLineNumber,
            String itemUnitOfMeasureCode, String itemCatalogNumber,
            String itemDescription, String itemTypeCode,
            String externalOrganizationB2bProductTypeName,
            double extendedPrice, double itemQuantity,
            String purchasingCommodityCode, double itemUnitPrice,
            PurapAccountingLineFixture accountingLineFixture,
            boolean itemAssignedToTradeInIndicator) {
        this(Integer.valueOf(itemLineNumber), itemUnitOfMeasureCode, itemCatalogNumber,
                itemDescription, itemTypeCode, externalOrganizationB2bProductTypeName,
                new KualiDecimal(extendedPrice), new KualiDecimal(itemQuantity),
                purchasingCommodityCode, new BigDecimal(itemUnitPrice),
                accountingLineFixture, itemAssignedToTradeInIndicator);
    }

	private RequisitionItemFixture(Integer itemLineNumber,
			String itemUnitOfMeasureCode, String itemCatalogNumber,
			String itemDescription, String itemTypeCode,
			String externalOrganizationB2bProductTypeName,
			KualiDecimal extendedPrice, KualiDecimal itemQuantity,
			String purchasingCommodityCode, BigDecimal itemUnitPrice,
			PurapAccountingLineFixture accountingLineFixture,
			boolean itemAssignedToTradeInIndicator) {

		this.itemLineNumber = itemLineNumber;
		this.itemUnitOfMeasureCode = itemUnitOfMeasureCode;
		this.itemCatalogNumber = itemCatalogNumber;
		this.itemDescription = itemDescription;
		this.itemTypeCode = itemTypeCode;
		this.externalOrganizationB2bProductTypeName = externalOrganizationB2bProductTypeName;
		this.extendedPrice = extendedPrice;
		this.itemQuantity = itemQuantity;
		this.purchasingCommodityCode = purchasingCommodityCode;
		this.itemUnitPrice = itemUnitPrice;

		this.accountingLineFixture = accountingLineFixture;
		this.itemAssignedToTradeInIndicator = itemAssignedToTradeInIndicator;

	}

	public RequisitionItem createRequisitionItem(boolean addAccountingLine) {
		// item
		RequisitionItem item = new RequisitionItem();
		item.setItemIdentifier(new Integer(SpringContext.getBean(org.kuali.kfs.krad.service.SequenceAccessorService.class).getNextAvailableSequenceNumber("REQS_ITM_ID").toString()));
		item.setItemLineNumber(itemLineNumber);
		item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
		item.setItemCatalogNumber(itemCatalogNumber);
		item.setItemDescription(itemDescription);
		item.setItemTypeCode(itemTypeCode);
		item.setExternalOrganizationB2bProductTypeName(externalOrganizationB2bProductTypeName);
		item.setExtendedPrice(extendedPrice);
		item.setItemQuantity(itemQuantity);
		item.setPurchasingCommodityCode(purchasingCommodityCode);
		item.setItemUnitPrice(itemUnitPrice);
		item.setItemAssignedToTradeInIndicator(itemAssignedToTradeInIndicator);

		// We don't always want to add the accounting line to the item immediately. That is because we need to
		// workaround an NPE that occurs when access security is enabled and refreshNonUpdatableReferences
		// is called on the account. For some reason the RequisitionItem cannot be found in ojb's cache and so when
		// it is attempted to be instantiated and constructor methods called, an NPE is thrown. So to workaround that issue
		// we add and save the item first, then add the accounting line and save again.
		// More analysis could probably be done to determine the root cause and address it, but for now this is good enough.
		if (addAccountingLine) {
			item.getSourceAccountingLines().add(
					accountingLineFixture.createRequisitionAccount(item.getItemIdentifier()));
			item.refreshNonUpdateableReferences();
		}

		return item;

	}

    public RequisitionItem createRequisitionItemForMicroTest() {
        RequisitionItem item = new RequisitionItem();
        item.setItemIdentifier(getOrdinalBasedItemIdentifier());
        item.setItemLineNumber(itemLineNumber);
        item.setItemUnitOfMeasureCode(itemUnitOfMeasureCode);
        item.setItemCatalogNumber(itemCatalogNumber);
        item.setItemDescription(itemDescription);
        item.setItemTypeCode(itemTypeCode);
        item.setExternalOrganizationB2bProductTypeName(externalOrganizationB2bProductTypeName);
        item.setExtendedPrice(extendedPrice);
        item.setItemQuantity(itemQuantity);
        item.setPurchasingCommodityCode(purchasingCommodityCode);
        item.setItemUnitPrice(itemUnitPrice);
        item.setItemAssignedToTradeInIndicator(itemAssignedToTradeInIndicator);

        if (accountingLineFixture != null) {
            PurApAccountingLine accountingLine = accountingLineFixture.createRequisitionAccountForMicroTest(
                    item.getItemIdentifier());
            item.getSourceAccountingLines().add(accountingLine);
        }

        return item;
    }

    public Integer getOrdinalBasedItemIdentifier() {
        return Integer.valueOf(BASE_ITEM_ID + ordinal());
    }

}
