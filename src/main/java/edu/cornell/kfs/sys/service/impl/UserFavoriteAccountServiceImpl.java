package edu.cornell.kfs.sys.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
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
	 * populate favorite account to the accounting line.
	 * This method supports PurApAccountingLine implementations that have a default constructor,
	 * as well as IWantAccount objects.
	 *
	 * @param account
	 * @return
	 */
    public <T extends GeneralLedgerPendingEntrySourceDetail> T getPopulatedNewAccount(FavoriteAccount account, Class<T> accountLineClass) {
        if (accountLineClass == null) {
            throw new IllegalArgumentException("accountLineClass cannot be null");
        } else if (!PurApAccountingLine.class.isAssignableFrom(accountLineClass) && !IWantAccount.class.isAssignableFrom(accountLineClass)) {
            throw new IllegalArgumentException("Unsupported accounting line implementation: " + accountLineClass.getName());
        }

        if (ObjectUtils.isNotNull(account)) {
            T acctLine;
            try {
                acctLine = accountLineClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException("Could not instantiate account line: " + e.getMessage());
            }
            // Configure real PURAP accounting lines differently from IWant ones, since the latter are not true account lines and have other unique setup.
            if (acctLine instanceof PurApAccountingLine) {
                populatePurApAccountingLine(account, (PurApAccountingLine) acctLine);
            } else if (acctLine instanceof IWantAccount) {
                populateIWantAccountingLine(account, (IWantAccount) acctLine);
            }
            refreshReferenceObjectsForPopulatedAccountingLine(acctLine);
            return acctLine;
        }
        return null;
    }

    protected void populatePurApAccountingLine(FavoriteAccount account, PurApAccountingLine acctLine) {
        final int ONE_HUNDRED = 100;
        populateAccountNumberOnPurApAccountingLine(account, acctLine);
		acctLine.setChartOfAccountsCode(account.getChartOfAccountsCode());
		acctLine.setSubAccountNumber(account.getSubAccountNumber());
		acctLine.setFinancialObjectCode(account.getFinancialObjectCode());
		acctLine.setFinancialSubObjectCode(account.getFinancialSubObjectCode());
		acctLine.setProjectCode(account.getProjectCode());
		acctLine.setOrganizationReferenceId(account.getOrganizationReferenceId());
		acctLine.setAccountLinePercent(new BigDecimal(ONE_HUNDRED));
    }

    /*
     * This operation has been moved to a separate method for unit testing convenience.
     * The setAccountNumber() method on accounting lines will invoke SpringContext.getBean(),
     * so unit-test-specific subclasses can override this to set the account number without using the setter.
     */
    protected void populateAccountNumberOnPurApAccountingLine(FavoriteAccount account, PurApAccountingLine acctLine) {
        acctLine.setAccountNumber(account.getAccountNumber());
    }

    protected void populateIWantAccountingLine(FavoriteAccount account, IWantAccount acctLine) {
        final int ONE_HUNDRED = 100;
        acctLine.setAccountNumber(account.getAccountNumber());
        acctLine.setChartOfAccountsCode(account.getChartOfAccountsCode());
        acctLine.setSubAccountNumber(account.getSubAccountNumber());
        acctLine.setFinancialObjectCode(account.getFinancialObjectCode());
        acctLine.setFinancialSubObjectCode(account.getFinancialSubObjectCode());
        acctLine.setProjectCode(account.getProjectCode());
        acctLine.setOrganizationReferenceId(account.getOrganizationReferenceId());
        acctLine.setUseAmountOrPercent(CUPurapConstants.PERCENT);
        acctLine.setAmountOrPercent(new KualiDecimal(ONE_HUNDRED));
    }

    protected void refreshReferenceObjectsForPopulatedAccountingLine(GeneralLedgerPendingEntrySourceDetail acctLine) {
        acctLine.refreshReferenceObject("chart");
        acctLine.refreshReferenceObject("account");
        acctLine.refreshReferenceObject("objectCode");
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
