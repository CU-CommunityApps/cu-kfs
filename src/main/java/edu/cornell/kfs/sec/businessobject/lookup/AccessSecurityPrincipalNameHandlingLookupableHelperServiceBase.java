package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.sec.businessobject.lookup.AccessSecurityLookupableHelperServiceImpl;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.kns.lookup.PrincipalNameHandlingLookupableHelperService;

public abstract class AccessSecurityPrincipalNameHandlingLookupableHelperServiceBase
        extends AccessSecurityLookupableHelperServiceImpl
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
