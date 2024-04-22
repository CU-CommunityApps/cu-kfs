package edu.cornell.kfs.kns.lookup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.ObjectUtils;

public interface PrincipalNameHandlingLookupableHelperService extends LookupableHelperService {

    default List<? extends BusinessObject> getSearchResultsWithPrincipalNameHandling(
            Map<String, String> fieldValues,
            Function<Map<String, String>, List<? extends BusinessObject>> actualSearchMethod) {
        return buildCriteriaWithMatchableConvertedPrincipalValues(fieldValues)
                .map(actualSearchMethod)
                .orElseGet(Collections::emptyList);
    }

    default Optional<Map<String, String>> buildCriteriaWithMatchableConvertedPrincipalValues(
            Map<String, String> fieldValues) {
        Map<String, String> newFieldValues = new HashMap<>(fieldValues);
        Map<String, String> fieldMappings = getMappingsFromPrincipalNameFieldsToPrincipalIdFields();
        PersonService personService = getPersonService();
        boolean allPrincipalNamesHaveMatches = true;
        
        for (Map.Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
            String principalNameField = fieldMapping.getKey();
            String principalIdField = fieldMapping.getValue();
            if (fieldValues.containsKey(principalNameField)) {
                String principalName = newFieldValues.remove(principalNameField);
                if (StringUtils.isNotBlank(principalName)) {
                    Person person = personService.getPersonByPrincipalName(principalName);
                    if (ObjectUtils.isNotNull(person)) {
                        newFieldValues.put(principalIdField, person.getPrincipalId());
                    } else {
                        allPrincipalNamesHaveMatches = false;
                    }
                }
            }
        }
        
        return allPrincipalNamesHaveMatches ? Optional.of(newFieldValues) : Optional.empty();
    }

    Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields();

    PersonService getPersonService();
}
