package edu.cornell.kfs.sys.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.kns.lookup.LookupableHelperServiceWithPrincipalNameHandling;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

@SuppressWarnings("deprecation")
public class UserProcurementProfileLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl
        implements LookupableHelperServiceWithPrincipalNameHandling {

	private static final long serialVersionUID = 1L;
	private static final Map<String, String> PRINCIPAL_MAPPINGS = Collections.singletonMap(
	        CUKFSPropertyConstants.PROFILE_USER_PRINCIPAL_NAME, KIMPropertyConstants.Principal.PRINCIPAL_ID);

	private UserProcurementProfileValidationService userProcurementProfileValidationService;
	private IdentityService identityService;

	@Override
	public List<? extends BusinessObject> getSearchResults(
			Map<String, String> fieldValues) {
        Map<String, String> convertedValues = convertPrincipalNameFieldValuesIfPresent(fieldValues);
        List<PersistableBusinessObject> searchResults =
                (List<PersistableBusinessObject>) super.getSearchResults(convertedValues);
        Map<String, String> newFieldValues = new HashMap<String, String>();
        boolean hasAccountcriteria = false;
        for (String key : convertedValues.keySet()) {
        	if (key.startsWith("favoriteAccounts.")) {
        		if (StringUtils.isNotBlank(convertedValues.get(key))) {
        			hasAccountcriteria = true;
        		}
        	    newFieldValues.put(key.replace("favoriteAccounts.", ""), convertedValues.get(key));
        	}
        }
      
        if (!hasAccountcriteria) {
        	return getSearchResultsNoAcctCriteria(searchResults);
        } else {
        	//newFieldValues.put("userProfileId", getSelectedUsers(searchResults)); // this will not convert to userprovileid in ()
        	// performance concern here
        	return getSearchResultsWithAcctCriteria(searchResults, newFieldValues);
        	
        }
//        return searchResults;
	}

	private List<PersistableBusinessObject> getSearchResultsNoAcctCriteria(List<PersistableBusinessObject> searchResults) {
    	List<PersistableBusinessObject> returnResults = new ArrayList<PersistableBusinessObject>();
    	for (PersistableBusinessObject pbo : searchResults) {
    		UserProcurementProfile userProfile = (UserProcurementProfile)pbo;
    		if (CollectionUtils.isEmpty(userProfile.getFavoriteAccounts())) {
    			userProfile.setResultAccount(new FavoriteAccount());
    			returnResults.add(userProfile);
    		} else {
    			for (FavoriteAccount account : userProfile.getFavoriteAccounts()) {
    				returnResults.add(buildUserProfile(userProfile, account));
    			}
    		}
    	}
      	 sortList(returnResults);
    	return returnResults;
		
	}
	
	private List<PersistableBusinessObject> getSearchResultsWithAcctCriteria(List<PersistableBusinessObject> searchResults,Map<String, String> newFieldValues) {
    	List<PersistableBusinessObject> returnResults = new ArrayList<PersistableBusinessObject>();
   	    for (String profileId : getSelectedUsers(searchResults)) {
   	    	newFieldValues.put("userProfileId", profileId);
        List<PersistableBusinessObject> searchResults1 = (List) getLookupService().findCollectionBySearch(FavoriteAccount.class, newFieldValues);
    	for (PersistableBusinessObject pbo : searchResults1) {
    		FavoriteAccount account = (FavoriteAccount)pbo;
    				returnResults.add(buildUserProfile(account.getUserProcurementProfile(), account));
    			
    		}
    	}
   	    sortList(returnResults);
    	return returnResults;
		
	}
	
	private void sortList(List<PersistableBusinessObject> returnResults) {
		Collections.sort(returnResults, new Comparator() {
            public int compare(Object o1, Object o2) {                   
            	UserProcurementProfile profile1 = (UserProcurementProfile) o1;
            	UserProcurementProfile profile2 = (UserProcurementProfile) o2;
                return profile1.getProfileUser().getName().compareTo(profile2.getProfileUser().getName());
            }
        });
       
	}
	private List<String> getSelectedUsers(List<PersistableBusinessObject> searchResults) {
		List<String> returnResults = new ArrayList<String>();
		for (PersistableBusinessObject pbo : searchResults) {
			String profileId = ((UserProcurementProfile) pbo).getUserProfileId().toString();
			if (!returnResults.contains(profileId)) {
				returnResults.add(profileId);
			}
		}
		return returnResults;
	}

	private UserProcurementProfile buildUserProfile(UserProcurementProfile userProfile, FavoriteAccount account) {
		
		UserProcurementProfile profile = (UserProcurementProfile)ObjectUtils.deepCopy(userProfile);
		profile.setResultAccount(account);
		return profile;
		
	}

	@Override
	public List<HtmlData> getCustomActionUrls(BusinessObject businessObject,
			List pkNames) {
		List<HtmlData> actions = super.getCustomActionUrls(businessObject, pkNames);
		if (!userProcurementProfileValidationService.canMaintainUserProcurementProfile()) {
			if (((UserProcurementProfile)businessObject).getPrincipalId().equals(GlobalVariables.getUserSession().getPrincipalId())) {
				actions.remove(1);
			} else {
				actions.clear();
			}
		}
		return actions;
	}

    @Override
    public Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields() {
        return PRINCIPAL_MAPPINGS;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

	public void setUserProcurementProfileValidationService(
			UserProcurementProfileValidationService userProcurementProfileValidationService) {
		this.userProcurementProfileValidationService = userProcurementProfileValidationService;
	}
}
