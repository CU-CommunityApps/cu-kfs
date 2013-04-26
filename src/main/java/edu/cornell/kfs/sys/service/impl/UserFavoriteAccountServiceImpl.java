package edu.cornell.kfs.sys.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserFavoriteAccountService;

public class UserFavoriteAccountServiceImpl implements UserFavoriteAccountService {

	private BusinessObjectService businessObjectService;
	
	/**
	 * get user's primary favorite account
	 */
    public FavoriteAccount getFavoriteAccount(String principalId) {
    	Map<String, String> fieldValues = new HashMap<String, String>();
    	fieldValues.put("principalId", principalId);
    	List<UserProcurementProfile> userProfiles = (List<UserProcurementProfile>)businessObjectService.findMatching(UserProcurementProfile.class, fieldValues);

    	if (CollectionUtils.isNotEmpty(userProfiles) && CollectionUtils.isNotEmpty(userProfiles.get(0).getFavoriteAccounts())) {
    		for (FavoriteAccount account : userProfiles.get(0).getFavoriteAccounts()) {
    			if (account.getPrimaryInd()) {
    				return account;
    			}
    		}
    	}
    	return null;

    }

	/**
	 * populate favorite account to the accounting line
	 * @param account
	 * @return
	 */
    public PurApAccountingLine getPopulatedNewAccount(FavoriteAccount account, boolean isRequisition) {
    	if (ObjectUtils.isNotNull(account)) {
    		PurApAccountingLine acctLine;
			if (isRequisition) {
				acctLine = new RequisitionAccount();
			} else {
				acctLine = new PurchaseOrderAccount();
			}
    		acctLine.setAccountNumber(account.getAccountNumber());
    		acctLine.setChartOfAccountsCode(account.getChartOfAccountsCode());
    		acctLine.setSubAccountNumber(account.getSubAccountNumber());
    		acctLine.setFinancialObjectCode(account.getFinancialObjectCode());
    		acctLine.setFinancialSubObjectCode(account.getFinancialSubObjectCode());
    		acctLine.setProjectCode(account.getProjectCode());
    		acctLine.setOrganizationReferenceId(account.getOrganizationReferenceId());
    		acctLine.setAccountLinePercent(new BigDecimal(100));
    		return acctLine;
    	}
    	return null;

    }
    
	/**
	 * retrieve the favorite account based on PK
	 * @param accountLineIdentifier
	 * @return
	 */
    public FavoriteAccount getSelectedFavoriteAccount(Integer accountLineIdentifier) {
    	Map<String, Object> primaryKey = new HashMap<String, Object>();
    	primaryKey.put("accountLineIdentifier", accountLineIdentifier);
    	return (FavoriteAccount)businessObjectService.findByPrimaryKey(FavoriteAccount.class, primaryKey);

    }

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

}
