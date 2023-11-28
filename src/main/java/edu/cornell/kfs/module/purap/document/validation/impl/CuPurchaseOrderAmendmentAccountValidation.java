package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.List;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchaseOrderAmendmentAccountValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

public class CuPurchaseOrderAmendmentAccountValidation extends PurchaseOrderAmendmentAccountValidation {

    /**
     * Overridden to allow POAs to use expired accounts.
     * 
     * @see org.kuali.kfs.module.purap.document.validation.impl.PurchaseOrderAmendmentAccountValidation#validate(
     * org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        boolean valid = true;
        final PurchaseOrderDocument poaDocument = (PurchaseOrderDocument) event.getDocument();
        final List<PurApItem> items = poaDocument.getItemsActiveOnly();

        final PurchaseOrderDocument po = getPurchaseOrderService().getCurrentPurchaseOrder(poaDocument.getPurapDocumentIdentifier());
        final List<PurApItem> poItems = po.getItems();

        for (final PurApItem item : items) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE)
                    && item.getSourceAccountingLines() != null && item.getSourceAccountingLines().size() > 0) {

                if (isItemChanged(item, poItems)) {
                    final List<PurApAccountingLine> accountingLines = item.getSourceAccountingLines();
                    for (final PurApAccountingLine accountingLine : accountingLines) {
                        if (!accountingLine.getAccount().isActive()) {
                            valid = false;
							GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
									PurapKeyConstants.ERROR_ITEM_ACCOUNT_INACTIVE,
									accountingLine.getAccount().getAccountNumber());
                            break;
                        }
                    }
                }
            }
        }

        return valid;
    }

    // Copied and tweaked this superclass method, and increased its visibility.
    protected boolean isItemChanged(final PurApItem poaItem, final List<PurApItem> poItems) {
        boolean changed = false;

        final int poaItemId = poaItem.getItemLineNumber().intValue();

        for (final PurApItem poItem : poItems) {

			if (poItem.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE)
					&& poaItemId == poItem.getItemLineNumber().intValue()) {
                if (poaItem.getItemQuantity() == null || poaItem.getItemQuantity().intValue() != poItem.getItemQuantity().intValue()) {
                    changed = true;
                }
                if (!poaItem.getItemUnitOfMeasureCode().equals(poItem.getItemUnitOfMeasureCode())) {
                    changed = true;
                }
                if (poaItem.getItemUnitPrice() == null || poaItem.getItemUnitPrice().floatValue() != poItem.getItemUnitPrice().floatValue()) {
                    changed = true;
                }

                if (poaItem.getTotalAmount().floatValue() != poItem.getTotalAmount().floatValue()) {
                    changed = true;
                }
                if (poaItem.getItemAssignedToTradeInIndicator() != poItem.getItemAssignedToTradeInIndicator()) {
                    changed = true;
                }
                if (poaItem.getItemCatalogNumber() != null
                        && !poaItem.getItemCatalogNumber().equals(poItem.getItemCatalogNumber())
                        || poItem.getItemCatalogNumber() != null
                        && !poItem.getItemCatalogNumber().equals(poaItem.getItemCatalogNumber())) {
                        changed = true;
                }
                if (poaItem.getItemDescription() != null
                        && !poaItem.getItemDescription().equals(poItem.getItemDescription())
                        || poItem.getItemDescription() != null
                        && !poItem.getItemDescription().equals(poaItem.getItemDescription())) {
                        changed = true;
                }
                if (poaItem.getExtendedPrice() != null && poItem.getExtendedPrice() != null
                        && poaItem.getExtendedPrice().floatValue() != poItem.getExtendedPrice().floatValue()
                        || poaItem.getExtendedPrice() != null
                        && poaItem.getExtendedPrice().floatValue() != 0
                        && poItem.getExtendedPrice() == null
                        || poaItem.getExtendedPrice() == null
                        && poItem.getExtendedPrice() != null && poItem.getExtendedPrice().floatValue() != 0) {
                        changed = true;
                }
                if (poaItem.getItemTaxAmount() != null
                        && poItem.getItemTaxAmount() != null
                        && poaItem.getItemTaxAmount().floatValue() != poItem.getItemTaxAmount().floatValue()
                        || poaItem.getItemTaxAmount() != null
                        && poaItem.getItemTaxAmount().floatValue() != 0 && poItem.getItemTaxAmount() != null
                        || poaItem.getItemTaxAmount() == null
                        && poItem.getItemTaxAmount() != null && poItem.getItemTaxAmount().floatValue() != 0) {
                        changed = true;
                }


                break;
            }
        }

        return changed;
    }

}
