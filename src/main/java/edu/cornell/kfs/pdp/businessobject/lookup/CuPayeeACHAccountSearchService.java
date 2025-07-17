package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ojb.broker.query.Criteria;
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
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants.PayeeAchAccount;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

public class CuPayeeACHAccountSearchService extends DefaultSearchService {

    private PersonService personService;
    private CriteriaLookupService criteriaLookupService;

    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            final Class<? extends BusinessObjectBase> businessObjectClass, final int skip, final int limit,
            final String sortField, final boolean sortAscending, final Map<String, String> searchProps) {
        
        if (StringUtils.isNotBlank(searchProps.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME))) {
            List<PayeeACHAccount> results = null;

            // Search for people with the given principal name(s), in a manner that respects
            // lookup criteria Strings.
            final List<Person> people = personService
                    .findPeople(Collections.singletonMap(KIMPropertyConstants.Principal.PRINCIPAL_NAME,
                            searchProps.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME)));
            if (!people.isEmpty()) {
                // Get the users' entity IDs and employee IDs for searching.
                final List<String> entityIds = new ArrayList<String>();
                final List<String> employeeIds = new ArrayList<String>();
                for (final Person person : people) {
                    entityIds.add(person.getEntityId());
                    if (StringUtils.isNotBlank(person.getEmployeeId())) {
                        employeeIds.add(person.getEmployeeId());
                    }
                }
                // Build the sub-predicate to limit by the entity or employee IDs for the given
                // principal names.
                final Predicate principalNameEquivalentPredicate;
                if (employeeIds.isEmpty()) {
                    principalNameEquivalentPredicate = PredicateFactory.and(
                            PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE,
                                    PayeeIdTypeCodes.ENTITY),
                            PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                                    entityIds.toArray(new String[entityIds.size()])));
                } else {
                    principalNameEquivalentPredicate = PredicateFactory.or(
                            PredicateFactory.and(
                                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE,
                                            PayeeIdTypeCodes.ENTITY),
                                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                                            entityIds.toArray(new String[entityIds.size()]))),
                            PredicateFactory.and(
                                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE,
                                            PayeeIdTypeCodes.EMPLOYEE),
                                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER,
                                            employeeIds.toArray(new String[employeeIds.size()]))));
                }

                // Build the criteria and run the search.

                QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
                
                builder.setMaxResults(LookupUtils.getSearchResultsLimit(PayeeACHAccount.class));
                if (!searchProps.isEmpty()) {
                    searchProps.remove(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);
                    builder.setStartAtIndex(skip + 1);
                    searchProps.remove(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);
                    searchProps.remove("skip");
                    searchProps.remove("limit");
                    builder.setPredicates(PredicateUtils.convertMapToPredicate(searchProps),
                            principalNameEquivalentPredicate);
                } else {
                    builder.setPredicates(principalNameEquivalentPredicate);
                }
                results = criteriaLookupService.lookup(PayeeACHAccount.class, builder.build()).getResults();

                // Move results to a mutable list, since the result list from
                // CriteriaLookupService is immutable.
                results = new ArrayList<PayeeACHAccount>(results);

                // Sort results accordingly using code from the ancestor class's version of the
                // method.
                final List<String> defaultSortColumns = List.of(sortField);
                if (defaultSortColumns.size() > 0) {
                    Collections.sort(results, new BeanPropertyComparator(defaultSortColumns, true));
                }
            }
            return Pair.of(results.subList(skip + 1, skip + limit), results.size());
        }

        else
            return super.executeSearch(businessObjectClass, skip, limit, sortField, sortAscending, searchProps);
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
