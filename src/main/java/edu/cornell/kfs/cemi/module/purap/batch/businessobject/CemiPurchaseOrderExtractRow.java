package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.util.Optional;

import org.apache.commons.lang3.Validate;

/*
 * Represents a single row from the PO extract query. Each row contains the PO header data, plus one open item
 * and one of its open accounting lines (if present). The item will be absent if the PO has no open items,
 * and the accounting line will be absent if the item has no accounting lines with outstanding encumbrances.
 */
public class CemiPurchaseOrderExtractRow {

    private final CemiLegacyPurchaseOrder purchaseOrder;
    private final Optional<CemiLegacyPurchaseOrderItem> item;
    private final Optional<CemiLegacyPurchaseOrderAccount> accountingLine;

    public CemiPurchaseOrderExtractRow(final CemiLegacyPurchaseOrder purchaseOrder,
            final Optional<CemiLegacyPurchaseOrderItem> item,
            final Optional<CemiLegacyPurchaseOrderAccount> accountingLine) {
        Validate.notNull(purchaseOrder, "purchaseOrder cannot be null");
        Validate.notNull(item, "item wrapper object cannot be null");
        Validate.notNull(accountingLine, "accountingLine wrapper object cannot be null");
        Validate.isTrue(item.isPresent() || accountingLine.isEmpty(),
                "accountingLine cannot be present when item is absent");
        this.purchaseOrder = purchaseOrder;
        this.item = item;
        this.accountingLine = accountingLine;
    }

    public CemiLegacyPurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public Optional<CemiLegacyPurchaseOrderItem> getItem() {
        return item;
    }

    public Optional<CemiLegacyPurchaseOrderAccount> getAccountingLine() {
        return accountingLine;
    }

}
