package edu.cornell.kfs.sys.businessobject.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.GlobalVariables;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;


public class FavoriteAccountValuesFiner extends KeyValuesBase {

    public List getKeyValues() {

        List<KeyLabelPair> keyValues = new ArrayList<KeyLabelPair>();

        List<FavoriteAccount> favoriteAccounts = getUserFavoriteAccounts();

        keyValues.add(new KeyLabelPair("", ""));

		if (CollectionUtils.isNotEmpty(favoriteAccounts)) {
				for (FavoriteAccount account : favoriteAccounts) {
					// TODO : need to sort primary ind first.
					keyValues.add(new KeyLabelPair(account.getAccountLineIdentifier(), getAccountingLineDescription(account)));
				}
		}
        return keyValues;
    }

    private String getAccountingLineDescription(FavoriteAccount account) {
    	StringBuffer sb = new StringBuffer();
    	sb.append(account.getDescription()).append(KFSConstants.COMMA).append(account.getAccountNumber());
    	if (account.getAccount().isActive()) {
    		sb.append("-active");
    	} else {
    		sb.append("-inactive");
    	}
    	
    	if (StringUtils.isNotBlank(account.getSubAccountNumber())) {
    		sb.append(KFSConstants.COMMA).append(account.getSubAccountNumber());
    	}
    	sb.append(KFSConstants.COMMA).append(account.getFinancialObjectCode());
    	if (account.getObjectCode() != null) {
    		if (account.getObjectCode().isActive()) {
        		sb.append("-active");
        	} else {
        		sb.append("-inactive");
        	}
    	} else {
    		sb.append("-inactive");
    	}
    	if (StringUtils.isNotBlank(account.getFinancialSubObjectCode())) {
    		sb.append(KFSConstants.COMMA).append(account.getFinancialSubObjectCode());
    	}
    	if (StringUtils.isNotBlank(account.getProjectCode())) {
    		sb.append(KFSConstants.COMMA).append(account.getProjectCode());
    	}
    	return sb.toString();
    }
    
    /*
     * get user favoriteacounts with primary account sorted first
     */
    private List<FavoriteAccount> getUserFavoriteAccounts() {
    	Map<String, Object> fieldValues = new HashMap<String, Object>();
    	fieldValues.put("principalId", GlobalVariables.getUserSession().getPrincipalId());
    	List<UserProcurementProfile> userProfiles = (List<UserProcurementProfile>)SpringContext.getBean(BusinessObjectService.class).findMatching(UserProcurementProfile.class, fieldValues);

    	if (CollectionUtils.isNotEmpty(userProfiles)) {
    		UserProcurementProfile userProfile = userProfiles.get(0);
    		fieldValues = new HashMap<String, Object>();
        	fieldValues.put("userProfileId", userProfile.getUserProfileId());
        	// retrieve from db for sorting purpose.
        	return (List<FavoriteAccount>)SpringContext.getBean(BusinessObjectService.class).findMatchingOrderBy(FavoriteAccount.class, fieldValues, "primaryInd", false);

    	}
    	return null;

    }

}
