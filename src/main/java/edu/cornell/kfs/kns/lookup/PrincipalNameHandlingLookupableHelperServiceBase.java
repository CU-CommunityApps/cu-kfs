package edu.cornell.kfs.kns.lookup;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.kfs.krad.bo.BusinessObject;

public abstract class PrincipalNameHandlingLookupableHelperServiceBase extends KualiLookupableHelperServiceImpl
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
