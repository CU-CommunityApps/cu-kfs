package edu.cornell.kfs.module.purap.util;

import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;

/**
 * Helper class for constructing Favorite-Account-derived
 * accounting lines on Purchasing line items.
 * Uses a Purchasing item to get the Favorite Account Line ID and accounting lines list.
 * Also uses a line index for generating the error property name.
 * 
 * @param <T> The actual type of the accounting line to build; must extend from PurApAccountingLine.
 */
public class PurchasingFavoriteAccountLineBuilderForLineItem<T extends PurApAccountingLine>
        extends PurchasingFavoriteAccountLineBuilderBase<T> {

    private PurchasingItemBase lineItem;
    private int lineIndex;

    /**
     * Constructs a new builder with just the superclass's sample accounting line initialized.
     * 
     * @param sampleAccountingLine The accounting line whose class should be used for the generated line type; cannot be null.
     * @throws IllegalArgumentException if sampleAccountingLine is null.
     */
    public PurchasingFavoriteAccountLineBuilderForLineItem(T sampleAccountingLine) {
        super(sampleAccountingLine);
    }

    /**
     * Constructs a new builder with all fields initialized.
     * 
     * @param lineItem The Purchasing item that the new accounting line should be added to.
     * @param lineIndex The index of the Purchasing item in the document's list; for error-logging purposes only.
     * @param sampleAccountingLine The accounting line whose class should be used for the generated line type; cannot be null.
     * @throws IllegalArgumentException if sampleAccountingLine is null.
     */
    public PurchasingFavoriteAccountLineBuilderForLineItem(PurchasingItemBase lineItem, int lineIndex, T sampleAccountingLine) {
        this(sampleAccountingLine);
        this.lineItem = lineItem;
        this.lineIndex = lineIndex;
    }

    public PurchasingItemBase getLineItem() {
        return lineItem;
    }

    public void setLineItem(PurchasingItemBase lineItem) {
        this.lineItem = lineItem;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    /**
     * Returns the Favorite Account Line ID on the referenced Purchasing item.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getFavoriteAccountLineIdentifier()
     */
    @Override
    public Integer getFavoriteAccountLineIdentifier() {
        return getLineItem().getFavoriteAccountLineIdentifier();
    }

    /**
     * Returns the Purchasing item's source accounting lines list.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getAccountingLines()
     */
    @Override
    public List<PurApAccountingLine> getAccountingLines() {
        return getLineItem().getSourceAccountingLines();
    }

    /**
     * Returns the line-index-based path to the line item's Favorite Account Line ID property,
     * starting with the document.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getErrorPropertyName()
     */
    @Override
    public String getErrorPropertyName() {
        return "document.item[" + getLineIndex() + "].favoriteAccountLineIdentifier";
    }

}
