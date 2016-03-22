package edu.cornell.kfs.sys.service;

import java.util.List;

import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;

public interface UserProcurementProfileValidationService {

	/**
	 * validate the favorite accounts in maint doc
	 * @param favoriteAccounts
	 * @return
	 */
	public boolean validateAccounts(List<FavoriteAccount> favoriteAccounts);
	
	/**
	 * validate bo by calling datadictionaryvalidationservice
	 * @param propertyPreFix
	 * @param pbo
	 * @return
	 */
	public boolean validateBo(String propertyPreFix, PersistableBusinessObjectBase pbo);
	
	
    /**
     * check if the favorite account is already added to accounting line
     */
	boolean isAccountExist(FavoriteAccount accountingLine, List<? extends GeneralLedgerPendingEntrySourceDetail> acctLines);

	/**
	 * validate the favorite account 
	 */
	public boolean validateAccount(String propertyPreFix, FavoriteAccount account, boolean hasPrimary);

	/**
     * check if user has procurement profile set up
     */
	public boolean validateUserProfileExist(String principalId);

	
    /**
     * check if user has permission to maintain other user's procurement profile
     */
	public boolean canMaintainUserProcurementProfile();

}
