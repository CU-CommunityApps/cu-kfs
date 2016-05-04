package edu.cornell.kfs.module.purap.util;

import java.util.List;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase;

/**
 * Helper class for building Favorite-Account-derived accounting lines
 * on IWantDocuments. Uses an IWantDocument for retrieving the
 * Favorite Account Line ID and account line list to use for the object creation.
 */
public class PurApFavoriteAccountLineBuilderForIWantDocument extends FavoriteAccountLineBuilderBase<IWantAccount,IWantAccount> {

    private IWantDocument iwantDocument;

    /**
     * Constructs a builder with a null IWantDocument.
     */
    public PurApFavoriteAccountLineBuilderForIWantDocument() {
    }

    /**
     * Constructs a builder with an explicit IWantDocument.
     * 
     * @param iwantDocument The document to use for constructing the accounting line.
     */
    public PurApFavoriteAccountLineBuilderForIWantDocument(IWantDocument iwantDocument) {
        this.iwantDocument = iwantDocument;
    }

    public IWantDocument getIwantDocument() {
        return iwantDocument;
    }

    public void setIwantDocument(IWantDocument iwantDocument) {
        this.iwantDocument = iwantDocument;
    }

    /**
     * Returns the IWantAccount class.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getAccountingLineClass()
     */
    @Override
    public Class<IWantAccount> getAccountingLineClass() {
        return IWantAccount.class;
    }

    /**
     * Returns the IWantDocument's Favorite Account Line ID.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getFavoriteAccountLineIdentifier()
     */
    @Override
    public Integer getFavoriteAccountLineIdentifier() {
        return getIwantDocument().getFavoriteAccountLineIdentifier();
    }

    /**
     * Returns the IWantDocument's account list.
     * 
     * @see edu.cornell.kfs.sys.util.FavoriteAccountLineBuilderBase#getAccountingLines()
     */
    @Override
    public List<IWantAccount> getAccountingLines() {
        return getIwantDocument().getAccounts();
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
