package edu.cornell.kfs.kns.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.krad.bo.BusinessObject;

public abstract class PrincipalNameHandlingLookupableHelperServiceBase extends KualiLookupableHelperServiceImpl
        implements PrincipalNameHandlingLookupableHelperService {

    private IdentityService identityService;

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        return getSearchResultsWithPrincipalNameHandling(fieldValues, super::getSearchResults);
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }
}
