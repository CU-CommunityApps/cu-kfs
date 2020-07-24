package edu.cornell.kfs.kns.lookup;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.principal.Principal;

public interface LookupableHelperServiceWithPrincipalNameHandling extends LookupableHelperService {

    default Map<String, String> convertPrincipalNameFieldValuesIfPresent(Map<String, String> fieldValues) {
        Map<String, String> newFieldValues = new HashMap<>(fieldValues);
        Map<String, String> fieldMappings = getMappingsFromPrincipalNameFieldsToPrincipalIdFields();
        IdentityService identityService = getIdentityService();
        
        for (Map.Entry<String, String> fieldMapping : fieldMappings.entrySet()) {
            String principalNameField = fieldMapping.getKey();
            String principalIdField = fieldMapping.getValue();
            if (fieldValues.containsKey(principalNameField)) {
                String principalName = newFieldValues.remove(principalNameField);
                if (StringUtils.isNotBlank(principalName)) {
                    Principal principal = identityService.getPrincipalByPrincipalName(principalName);
                    if (ObjectUtils.isNotNull(principal)) {
                        newFieldValues.put(principalIdField, principal.getPrincipalId());
                    }
                }
            }
        }
        return newFieldValues;
    }

    Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields();

    IdentityService getIdentityService();
}
