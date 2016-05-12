package edu.cornell.kfs.module.purap.util;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase;

/**
 * Base helper class for building Favorite-Account-derived accounting lines
 * on Purchasing documents.
 * 
 * It would be ideal to use the document's configured source accounting line class
 * to determine what type of line to construct; however, not all Purchasing documents
 * configure that to something other than SourceAccountingLine in the data dictionary.
 * Thus, this implementation instead derives the class based on a sample accounting line
 * of the exact type, typically obtained from a PurchasingFormBase method such as
 * setupNewPurchasingAccountingLine().
 * 
 * @param <T> The actual type of the accounting line to build; must extend from PurApAccountingLine.
 */
public abstract class PurchasingFavoriteAccountLineBuilderBase<T extends PurApAccountingLine>
        extends FavoriteAccountLineBuilderBase<PurApAccountingLine,T> {

    private final T sampleAccountingLine;

    /**
     * Constructs a new builder using a sample accounting line of the exact type.
     * 
     * @param sampleAccountingLine The accounting line whose class should be used for the generated line type; cannot be null.
     * @throws IllegalArgumentException if sampleAccountingLine is null.
     */
    protected PurchasingFavoriteAccountLineBuilderBase(T sampleAccountingLine) {
        if (ObjectUtils.isNull(sampleAccountingLine)) {
            throw new IllegalArgumentException("sampleAccountingLine cannot be null");
        }
        this.sampleAccountingLine = sampleAccountingLine;
    }

    /**
     * Returns the class of the sample accounting line that was passed into the constructor. 
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getAccountingLineClass()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getAccountingLineClass() {
        return (Class<T>) sampleAccountingLine.getClass();
    }

}
