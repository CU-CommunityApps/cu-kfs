package edu.cornell.kfs.module.purap.util;

import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * Helper class for constructing Favorite-Account-derived
 * accounting lines that are awaiting distribution to line items.
 * Uses a Purchasing document to get the Favorite Account Line ID, plus an explicit list for the accounting lines.
 * 
 * @param <T> The actual type of the accounting line to build; must extend from PurApAccountingLine.
 */
public class PurchasingFavoriteAccountLineBuilderForDistribution<T extends PurApAccountingLine>
        extends PurchasingFavoriteAccountLineBuilderBase<T> {

    private PurchasingDocumentBase purchasingDocument;
    private List<PurApAccountingLine> accountDistributionLines;

    /**
     * Constructs a new builder with just the superclass's sample accounting line initialized.
     * 
     * @param sampleAccountingLine The accounting line whose class should be used for the generated line type; cannot be null.
     * @throws IllegalArgumentException if sampleAccountingLine is null.
     */
    public PurchasingFavoriteAccountLineBuilderForDistribution(T sampleAccountingLine) {
        super(sampleAccountingLine);
    }

    /**
     * Constructs a new builder with all fields initialized.
     * 
     * @param purchasingDocument The document to retrieve the Favorite Account Line ID from.
     * @param accountDistributionLines The list of accounting lines awaiting distribution to the document's items.
     * @param sampleAccountingLine The accounting line whose class should be used for the generated line type; cannot be null.
     * @throws IllegalArgumentException if sampleAccountingLine is null.
     */
    public PurchasingFavoriteAccountLineBuilderForDistribution(
            PurchasingDocumentBase purchasingDocument, List<PurApAccountingLine> accountDistributionLines, T sampleAccountingLine) {
        this(sampleAccountingLine);
        this.purchasingDocument = purchasingDocument;
        this.accountDistributionLines = accountDistributionLines;
    }

    public PurchasingDocumentBase getPurchasingDocument() {
        return purchasingDocument;
    }

    public void setPurchasingDocument(PurchasingDocumentBase purchasingDocument) {
        this.purchasingDocument = purchasingDocument;
    }

    public List<PurApAccountingLine> getAccountDistributionLines() {
        return accountDistributionLines;
    }

    public void setAccountDistributionLines(List<PurApAccountingLine> accountDistributionLines) {
        this.accountDistributionLines = accountDistributionLines;
    }

    /**
     * Returns the Favorite Account Line ID configured on the document.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getFavoriteAccountLineIdentifier()
     */
    @Override
    public Integer getFavoriteAccountLineIdentifier() {
        return getPurchasingDocument().getFavoriteAccountLineIdentifier();
    }

    /**
     * Returns the explicit list of account distribution lines.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getAccountingLines()
     */
    @Override
    public List<PurApAccountingLine> getAccountingLines() {
        return getAccountDistributionLines();
    }

    /**
     * Returns the static path to the document's Favorite Account Line ID property,
     * starting with the document itself.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getErrorPropertyName()
     */
    @Override
    public String getErrorPropertyName() {
        return CUKFSPropertyConstants.DOCUMENT_FAVORITE_ACCOUNT_LINE_IDENTIFIER;
    }

}
