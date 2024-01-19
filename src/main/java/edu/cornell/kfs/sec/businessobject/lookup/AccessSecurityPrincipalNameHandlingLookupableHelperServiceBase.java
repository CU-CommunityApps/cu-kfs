package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sec.businessobject.lookup.AccessSecurityLookupableHelperServiceImpl;

import edu.cornell.kfs.kns.lookup.PrincipalNameHandlingLookupableHelperService;

public abstract class AccessSecurityPrincipalNameHandlingLookupableHelperServiceBase
        extends AccessSecurityLookupableHelperServiceImpl
        implements PrincipalNameHandlingLookupableHelperService {

    private PersonService personService;

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        return getSearchResultsWithPrincipalNameHandling(fieldValues, super::getSearchResults);
    }

    @Override
    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
