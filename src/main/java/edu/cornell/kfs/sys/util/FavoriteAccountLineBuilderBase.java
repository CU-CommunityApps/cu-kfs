package edu.cornell.kfs.sys.util;

import java.util.List;

import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.exception.FavoriteAccountException;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

/**
 * Base class for helper objects that generate accounting line instances for a given Favorite Account object.
 *
 * @param <E> The element type of the list that the new accounting line will be inserted into.
 * @param <T> The actual type of the accounting line to be inserted.
 */
public abstract class FavoriteAccountLineBuilderBase<E extends GeneralLedgerPendingEntrySourceDetail,T extends E> {

    protected UserProcurementProfileValidationService userProcurementProfileValidationService;
    protected UserFavoriteAccountService userFavoriteAccountService;

    /**
     * Returns the Class object for the actual accounting line type to use; cannot be null.
     */
    public abstract Class<T> getAccountingLineClass();

    /**
     * Returns the primary key of the Favorite Account to generate an accounting line for; may be null.
     */
    public abstract Integer getFavoriteAccountLineIdentifier();

    /**
     * Returns the list that the new accounting line will be inserted into and/or to check for duplicates; cannot be null.
     */
    public abstract List<E> getAccountingLines();

    /**
     * Returns the property name to use when performing an error-handled operation that places validation errors in the message map; should not be null.
     */
    public abstract String getErrorPropertyName();

    /**
     * Returns the UserProcurementProfileValidationService instance to use; should not be null.
     * The default implementation uses the one passed to the setter if given and non-null; otherwise, it uses the one from the Spring context.
     */
    public UserProcurementProfileValidationService getUserProcurementProfileValidationService() {
        if (userProcurementProfileValidationService != null) {
            return userProcurementProfileValidationService;
        }
        return SpringContext.getBean(UserProcurementProfileValidationService.class);
    }

    /**
     * Sets the UserProcurementProfileValidationService instance to use.
     */
    public void setUserProcurementProfileValidationService(UserProcurementProfileValidationService userProcurementProfileValidationService) {
        this.userProcurementProfileValidationService = userProcurementProfileValidationService;
    }

    /**
     * Returns the UserFavoriteAccountService instance to use; should not be null.
     * The default implementation uses the one passed to the setter if given and non-null; otherwise, it uses the one from the Spring context.
     */
    public UserFavoriteAccountService getUserFavoriteAccountService() {
        if (userFavoriteAccountService != null) {
            return userFavoriteAccountService;
        }
        return SpringContext.getBean(UserFavoriteAccountService.class);
    }

    /**
     * Sets the UserFavoriteAccountService instance to use.
     */
    public void setUserFavoriteAccountService(UserFavoriteAccountService userFavoriteAccountService) {
        this.userFavoriteAccountService = userFavoriteAccountService;
    }

    /**
     * Builds a new accounting line from the Favorite Account referenced by the associated getter.
     * Outside code should invoke the "IfPossible" version of this method instead, which has
     * built-in error handling for validation problems.
     * 
     * @return A new accounting line configured according to the given Favorite Account.
     * @throws FavoriteAccountException if the Favorite Account ID is null or does not reference an existing Favorite Account,
     * or if the list already contains an accounting line for that Favorite Account. 
     */
    protected T createNewFavoriteAccountLine() throws FavoriteAccountException {
        if (getFavoriteAccountLineIdentifier() == null) {
            throw new FavoriteAccountException(CUKFSKeyConstants.ERROR_FAVORITE_ACCOUNT_NOT_SELECTED);
        }
        
        FavoriteAccount favoriteAccount = getUserFavoriteAccountService().getSelectedFavoriteAccount(getFavoriteAccountLineIdentifier());
        
        if (ObjectUtils.isNull(favoriteAccount)) {
            throw new FavoriteAccountException(CUKFSKeyConstants.ERROR_FAVORITE_ACCOUNT_NOT_EXIST);
        } else if (getUserProcurementProfileValidationService().isAccountExist(favoriteAccount, getAccountingLines())) {
            throw new FavoriteAccountException(CUKFSKeyConstants.ERROR_FAVORITE_ACCOUNT_EXIST);
        }
        
        return getUserFavoriteAccountService().getPopulatedNewAccount(favoriteAccount, getAccountingLineClass());
    }

    /**
     * Adds a new Favorite-Account-derived accounting line to the configured list.
     * Outside code should invoke the "IfPossible" version of this method instead, which has
     * built-in error handling for validation problems.
     * 
     * @throws FavoriteAccountException if the Favorite Account ID is null or does not reference an existing Favorite Account,
     * or if the list already contains an accounting line for that Favorite Account.
     */
    protected void addNewFavoriteAccountLineToList() throws FavoriteAccountException {
        T newAccountingLine = createNewFavoriteAccountLine();
        getAccountingLines().add(newAccountingLine);
    }

    /**
     * Performs an error-handled invocation of createNewFavoriteAccountLine() that will catch and handle FavoriteAccountException.
     * If such an error occurs, then an entry will be added to the message map using the exception's text and the configured error property name,
     * and a null value will be returned.
     * 
     * @return A new accounting line configured according to the given Favorite Account, or null if a validation error occurred during generation.
     */
    public T createNewFavoriteAccountLineIfPossible() {
        try {
            return createNewFavoriteAccountLine();
        } catch (FavoriteAccountException e) {
            GlobalVariables.getMessageMap().putError(getErrorPropertyName(), e.getMessage());
        }
        
        return null;
    }

    /**
     * Performs an error-handled invocation of addNewFavoriteAccountLineToList() that will catch and handle FavoriteAccountException.
     * If such an error occurs, then an entry will be added to the message map using the exception's text and the configured error property name.
     */
    public void addNewFavoriteAccountLineToListIfPossible() {
        try {
            addNewFavoriteAccountLineToList();
        } catch (FavoriteAccountException e) {
            GlobalVariables.getMessageMap().putError(getErrorPropertyName(), e.getMessage());
        }
    }

}
