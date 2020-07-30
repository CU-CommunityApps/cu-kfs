package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.Collections;
import java.util.Map;

import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.sec.SecPropertyConstants;

import edu.cornell.kfs.kns.lookup.PrincipalNameHandlingLookupableHelperServiceBase;

public class SecurityPrincipalLookupableHelperServiceImpl extends PrincipalNameHandlingLookupableHelperServiceBase {

    private static final Map<String, String> PRINCIPAL_MAPPINGS = Collections.singletonMap(
            SecPropertyConstants.SECURITY_PERSON_PRINCIPAL_NAME, KIMPropertyConstants.Principal.PRINCIPAL_ID);

    @Override
    public Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields() {
        return PRINCIPAL_MAPPINGS;
    }
}
