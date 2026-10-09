package edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrder;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrderItem;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderExtractRow;

public class CemiPurchaseOrderIterator implements Iterator<CemiLegacyPurchaseOrder> {

    private final PeekingIterator<CemiPurchaseOrderExtractRow> extractRowsIterator;

    /*
     * NOTE: It is assumed that the provided iterator will return results in ascending order by document number,
     *       then by item line number and item identifier, then by account identifier. This class groups
     *       consecutive rows having the same document number into a single Purchase Order.
     */
    public CemiPurchaseOrderIterator(final Iterator<CemiPurchaseOrderExtractRow> extractRowsIterator) {
        Validate.notNull(extractRowsIterator, "extractRowsIterator cannot be null");
        this.extractRowsIterator = PeekingIterator.peekingIterator(extractRowsIterator);
    }

    @Override
    public boolean hasNext() {
        return extractRowsIterator.hasNext();
    }

    @Override
    public CemiLegacyPurchaseOrder next() {
        Validate.validState(hasNext(), "There are no more Purchase Orders left in this Iterator");
        final CemiPurchaseOrderExtractRow firstRow = extractRowsIterator.next();
        final CemiLegacyPurchaseOrder purchaseOrder = firstRow.getPurchaseOrder();
        final String documentNumber = purchaseOrder.getDocumentNumber();
        final List<CemiLegacyPurchaseOrderItem> openItems = new ArrayList<>();

        addItemDataFromRow(openItems, firstRow);
        while (extractRowsIterator.hasNext() && Objects.equals(documentNumber,
                extractRowsIterator.peek().getPurchaseOrder().getDocumentNumber())) {
            addItemDataFromRow(openItems, extractRowsIterator.next());
        }

        purchaseOrder.setOpenItems(Collections.unmodifiableList(openItems));
        return purchaseOrder;
    }

    private void addItemDataFromRow(final List<CemiLegacyPurchaseOrderItem> openItems,
            final CemiPurchaseOrderExtractRow row) {
        if (row.getItem().isEmpty()) {
            return;
        }

        final CemiLegacyPurchaseOrderItem rowItem = row.getItem().get();
        final CemiLegacyPurchaseOrderItem lastItem = openItems.isEmpty() ? null : openItems.get(openItems.size() - 1);
        final CemiLegacyPurchaseOrderItem currentItem;
        if (lastItem != null && Objects.equals(lastItem.getItemIdentifier(), rowItem.getItemIdentifier())) {
            currentItem = lastItem;
        } else {
            openItems.add(rowItem);
            currentItem = rowItem;
        }

        row.getAccountingLine().ifPresent(currentItem.getAccountingLines()::add);
    }

}
