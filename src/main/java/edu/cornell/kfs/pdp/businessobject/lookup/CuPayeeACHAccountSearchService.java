package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.PredicateUtils;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

public class CuPayeeACHAccountSearchService extends DefaultSearchService {
    private static final Logger LOG = LogManager.getLogger(CuPayeeACHAccountSearchService.class);
    
    // Constants for search parameter keys
    private static final String SKIP_PARAM = "skip";
    private static final String SORT_PARAM = "sort";
    private static final String LIMIT_PARAM = "limit";

    private PersonService personService;
    private CriteriaLookupService criteriaLookupService;

    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            final Class<? extends BusinessObjectBase> businessObjectClass, final int skip, final int limit,
            final String sortField, final boolean sortAscending, final Map<String, String> searchProps) {
        final String principalName = searchProps.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);
        
        if (StringUtils.isBlank(principalName)) {
            return super.executeSearch(businessObjectClass, skip, limit, sortField, sortAscending, searchProps);
        }
        
        final List<PayeeACHAccount> results = searchByPrincipalName(principalName, searchProps, skip, sortField);
        return createSearchResult(results, limit);
    }
    
    private List<PayeeACHAccount> searchByPrincipalName(
            final String principalName, 
            final Map<String, String> searchProps, 
            final int skip, 
            final String sortField) {
        
        final List<Person> people = findPeopleByPrincipalName(principalName);
        if (people.isEmpty()) {
            LOG.debug("searchByPrincipalName, No people found for principal name: {}", principalName);
            return new ArrayList<>();
        }

        // Extract entity and employee IDs
        final Pair<List<String>, List<String>> ids = extractPersonIds(people);
        final List<String> entityIds = ids.getLeft();
        final List<String> employeeIds = ids.getRight();

        // Build predicate for payee ID search
        final Predicate principalNamePredicate = buildPrincipalNamePredicate(entityIds, employeeIds);

        // Execute the search
        final List<PayeeACHAccount> results = executePayeeSearch(searchProps, skip, principalNamePredicate);

        // Sort results
        return sortResults(results, sortField);
    }
    
    private List<Person> findPeopleByPrincipalName(final String principalName) {
        final Map<String, String> searchCriteria = Collections.singletonMap(
            KIMPropertyConstants.Principal.PRINCIPAL_NAME, principalName);
        return personService.findPeople(searchCriteria);
    }
    
    private Pair<List<String>, List<String>> extractPersonIds(final List<Person> people) {
        final List<String> entityIds = new ArrayList<>();
        final List<String> employeeIds = new ArrayList<>();
        
        for (final Person person : people) {
            if (StringUtils.isNotBlank(person.getEntityId())) {
                entityIds.add(person.getEntityId());
            }
            if (StringUtils.isNotBlank(person.getEmployeeId())) {
                employeeIds.add(person.getEmployeeId());
            }
        }
        
        return Pair.of(entityIds, employeeIds);
    }
    
    private Predicate buildPrincipalNamePredicate(final List<String> entityIds, final List<String> employeeIds) {
        // Build the sub-predicate to limit by the entity or employee IDs for the given
        // principal names.
        final Predicate principalNameEquivalentPredicate;
        if (employeeIds.isEmpty()) {
            principalNameEquivalentPredicate = PredicateFactory.and(
                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.ENTITY),
                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                            entityIds.toArray(new String[entityIds.size()])));
        } else {
            principalNameEquivalentPredicate = PredicateFactory.or(PredicateFactory.and(
                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.ENTITY),
                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                            entityIds.toArray(new String[entityIds.size()]))),
                    PredicateFactory.and(
                            PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE,
                                    PayeeIdTypeCodes.EMPLOYEE),
                            PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                                    employeeIds.toArray(new String[employeeIds.size()]))));
        }
        return principalNameEquivalentPredicate;
    }
    
    private List<PayeeACHAccount> executePayeeSearch(final Map<String, String> searchProps, final int skip,
            final Predicate principalNamePredicate) {

        final QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
        builder.setMaxResults(LookupUtils.getSearchResultsLimit(PayeeACHAccount.class));
        builder.setStartAtIndex(skip);

        if (!searchProps.isEmpty()) {
            final Map<String, String> cleanedProps = cleanSearchProperties(searchProps);
            final Predicate searchPropsPredicate = PredicateUtils.convertMapToPredicate(cleanedProps);
            builder.setPredicates(searchPropsPredicate, principalNamePredicate);
        } else {
            builder.setPredicates(principalNamePredicate);
        }

        final List<PayeeACHAccount> results = criteriaLookupService.lookup(PayeeACHAccount.class, builder.build())
                .getResults();
        return new ArrayList<>(results);
    }
    
    private Map<String, String> cleanSearchProperties(final Map<String, String> searchProps) {
        final Map<String, String> cleanedProps = new java.util.HashMap<>(searchProps);
        cleanedProps.remove(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);
        cleanedProps.remove(SKIP_PARAM);
        cleanedProps.remove(SORT_PARAM);
        cleanedProps.remove(LIMIT_PARAM);
        return cleanedProps;
    }
    
    private List<PayeeACHAccount> sortResults(final List<PayeeACHAccount> results, final String sortField) {
        if (StringUtils.isNotBlank(sortField)) {
            final List<String> sortColumns = List.of(sortField);
            Collections.sort(results, new BeanPropertyComparator(sortColumns, true));
        }
        return results;
    }
    
    private Pair<Collection<? extends BusinessObjectBase>, Integer> createSearchResult(
            final List<PayeeACHAccount> results, final int limit) {
        
        if (results == null || results.isEmpty()) {
            return Pair.of(new ArrayList<PayeeACHAccount>(), 0);
        }

        final int totalSize = results.size();
        final int endIndex = Math.min(results.size(), limit);
        final List<PayeeACHAccount> paginatedResults = results.subList(0, endIndex);
        
        return Pair.of(paginatedResults, totalSize);
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public CriteriaLookupService getCriteriaLookupService() {
        return criteriaLookupService;
    }

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
