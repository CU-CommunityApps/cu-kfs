package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.businessobject.service.impl.DefaultSearchService;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

public class CuPayeeACHAccountSearchService extends DefaultSearchService {

    private PersonService personService;

    @Override
    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            final Class<? extends BusinessObjectBase> businessObjectClass, final int skip, final int limit,
            final String sortField, final boolean sortAscending, final Map<String, String> searchProps) {
        return super.executeSearch(businessObjectClass, skip, limit, sortField, sortAscending, searchProps);
    }

    @Override
    protected MultiValueMap<String, String> transformSearchParams(final Class<? extends BusinessObjectBase> boClass,
            final MultiValueMap<String, String> searchParams) {
        final MultiValueMap<String, String> transformedSearchParams = super.transformSearchParams(boClass,
                searchParams);

        if (searchParams.containsKey(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME)) {
            String principalName = searchParams.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME).get(0);
            if (StringUtils.isNotBlank(principalName)) {

                List<PayeeACHAccount> results = null;

                // Search for people with the given principal name(s), in a manner that respects
                // lookup criteria Strings.
                final List<Person> people = personService.findPeople(
                        Collections.singletonMap(KIMPropertyConstants.Principal.PRINCIPAL_NAME, principalName));
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

                    transformedSearchParams.remove(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);

                    List<String> payeeTypeIdentifiers = new ArrayList<String>();
                    if (searchParams.containsKey(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE)) {
                        String typeId = searchParams.get(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE).get(0);
                        if (StringUtils.isNotBlank(typeId)) {
                            payeeTypeIdentifiers.add(typeId);
                        }
                    }

                    if (employeeIds.isEmpty()) {
                        payeeTypeIdentifiers.add(PayeeIdTypeCodes.ENTITY);
                        transformedSearchParams.put(PdpPropertyConstants.PAYEE_ID_NUMBER, entityIds);
                    } else {
                        List<String> payeeIds = new ArrayList<String>();
                        payeeTypeIdentifiers.add(PayeeIdTypeCodes.ENTITY);
                        payeeIds.addAll(entityIds);
                        payeeTypeIdentifiers.add(PayeeIdTypeCodes.EMPLOYEE);
                        payeeIds.addAll(employeeIds);
                        transformedSearchParams.put(PdpPropertyConstants.PAYEE_ID_NUMBER, List.of(buildMultiValue(payeeIds)));
                    }
                }
            }
        }

        return transformedSearchParams;
    }
    
    public static String buildMultiValue(List<String> values) {
        return values.stream()
                    .collect(java.util.stream.Collectors.joining(KRADConstants.MULTIPLE_VALUE_LOOKUP_OBJ_IDS_SEPARATOR));
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

}
