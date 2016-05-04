package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;

public interface UserFavoriteAccountService {

	/**
	 * get user's primary favorite account
	 * @param principalId
	 * @return
	 */
	FavoriteAccount getFavoriteAccount(String principalId);
	
	/**
	 * populate favorite account to the accounting line
	 * @param account
	 * @return
	 */
	<T extends GeneralLedgerPendingEntrySourceDetail> T getPopulatedNewAccount(FavoriteAccount account, Class<T> accountLineClass);
	
	/**
	 * retrieve the favorite account based on PK
	 * @param accountLineIdentifier
	 * @return
	 */
	FavoriteAccount getSelectedFavoriteAccount(Integer accountLineIdentifier);
}
