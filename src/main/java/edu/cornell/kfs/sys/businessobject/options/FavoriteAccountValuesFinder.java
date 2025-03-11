package edu.cornell.kfs.sys.businessobject.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;


public class FavoriteAccountValuesFinder extends KeyValuesBase {
    private static final long serialVersionUID = 5514852567213028935L;
    protected BusinessObjectService businessObjectService;

    public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        List<FavoriteAccount> favoriteAccounts = getUserFavoriteAccounts();

        keyValues.add(new ConcreteKeyValue("", ""));

		if (CollectionUtils.isNotEmpty(favoriteAccounts)) {
			Collections.sort(favoriteAccounts, new Comparator() {
                public int compare(Object o1, Object o2) {                   
                    FavoriteAccount accountFirst = (FavoriteAccount) o1;
                    FavoriteAccount accountSecond = (FavoriteAccount) o2;
                    if (accountFirst.getPrimaryInd() && !accountSecond.getPrimaryInd()) {
                        return -1;
                    } else if (!accountFirst.getPrimaryInd() && accountSecond.getPrimaryInd()) {
                        return 1;
                    }
                    if (StringUtils.equals(accountFirst.getDescription(), accountSecond.getDescription())) {
                        return accountFirst.getAccountNumber().compareTo(accountSecond.getAccountNumber());
                    } else if (StringUtils.isBlank(accountFirst.getDescription())) {
                    	// Be aware case comparison.
                    	return -1;                    	
                    } else if (StringUtils.isBlank(accountSecond.getDescription())) {
                    	return 1;                    	
                    } else {
                    	return accountFirst.getDescription().compareTo(accountSecond.getDescription());
                    }
                }
            });
				for (FavoriteAccount account : favoriteAccounts) {
					keyValues.add(new ConcreteKeyValue(account.getAccountLineIdentifier().toString(), getAccountingLineDescription(account)));
				}
		}
        return keyValues;
    }

    private String getAccountingLineDescription(FavoriteAccount account) {
    	StringBuffer sb = new StringBuffer();
    	if (StringUtils.isNotBlank(account.getDescription())) {
    		sb.append(account.getDescription());
    	}
    	
    	sb.append(StringUtils.isBlank(account.getDescription()) ? KFSConstants.EMPTY_STRING : KFSConstants.COMMA).append(account.getAccountNumber());
    	
    	checkToAppend(sb, account.getSubAccountNumber());
    	checkToAppend(sb, account.getFinancialObjectCode());
    	checkToAppend(sb, account.getFinancialSubObjectCode());
    	checkToAppend(sb, account.getProjectCode());
    	return sb.toString();
    }
    
    private void checkToAppend(StringBuffer sb, String fieldValue) {
    	if (StringUtils.isNotBlank(fieldValue)) {
    		sb.append(KFSConstants.COMMA).append(fieldValue);
    	}

    }
    
    /*
     * get user favoriteacounts with primary account sorted first
     */
    private List<FavoriteAccount> getUserFavoriteAccounts() {
    	Map<String, Object> fieldValues = new HashMap<String, Object>();
    	fieldValues.put("principalId", GlobalVariables.getUserSession().getPrincipalId());
    	List<UserProcurementProfile> userProfiles = (List<UserProcurementProfile>) businessObjectService.findMatching(UserProcurementProfile.class, fieldValues);

    	if (CollectionUtils.isNotEmpty(userProfiles)) {
    		UserProcurementProfile userProfile = userProfiles.get(0);
    		fieldValues = new HashMap<String, Object>();
        	fieldValues.put("userProfileId", userProfile.getUserProfileId());
        	// retrieve from db for sorting purpose.
        	return (List<FavoriteAccount>) businessObjectService.findMatchingOrderBy(FavoriteAccount.class, fieldValues, "primaryInd", false);

    	}
    	return null;

    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
