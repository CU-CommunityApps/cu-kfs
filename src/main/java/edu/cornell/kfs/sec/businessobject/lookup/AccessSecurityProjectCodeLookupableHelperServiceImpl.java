package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.Collections;
import java.util.Map;

import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class AccessSecurityProjectCodeLookupableHelperServiceImpl
        extends AccessSecurityPrincipalNameHandlingLookupableHelperServiceBase {

    private static final Map<String, String> PRINCIPAL_MAPPINGS = Collections.singletonMap(
            CUKFSPropertyConstants.PROJECT_MANAGER_UNIVERSAL_PRINCIPAL_NAME,
                    KFSPropertyConstants.PROJECT_MANAGER_UNIVERSAL_ID);

    @Override
    public Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields() {
        return PRINCIPAL_MAPPINGS;
    }
}
