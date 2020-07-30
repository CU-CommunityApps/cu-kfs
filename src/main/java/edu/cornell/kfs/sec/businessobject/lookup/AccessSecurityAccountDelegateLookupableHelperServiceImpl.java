package edu.cornell.kfs.sec.businessobject.lookup;

import java.util.Collections;
import java.util.Map;

import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class AccessSecurityAccountDelegateLookupableHelperServiceImpl
        extends AccessSecurityPrincipalNameHandlingLookupableHelperServiceBase {

    private static final Map<String, String> PRINCIPAL_MAPPINGS = Collections.singletonMap(
            CUKFSPropertyConstants.ACCOUNT_DELEGATE_PRINCIPAL_NAME, KFSPropertyConstants.ACCOUNT_DELEGATE_SYSTEM_ID);

    @Override
    public Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields() {
        return PRINCIPAL_MAPPINGS;
    }
}
