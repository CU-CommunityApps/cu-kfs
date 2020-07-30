package edu.cornell.kfs.module.ar.businessobject.lookup;

import java.util.Collections;
import java.util.Map;

import org.kuali.kfs.module.ar.ArPropertyConstants;

import edu.cornell.kfs.kns.lookup.PrincipalNameHandlingLookupableHelperServiceBase;
import edu.cornell.kfs.module.ar.CuArPropertyConstants;

public class InvoiceRecurrenceLookupableHelperServiceImpl extends PrincipalNameHandlingLookupableHelperServiceBase {

    private static final Map<String, String> PRINCIPAL_MAPPINGS = Collections.singletonMap(
            CuArPropertyConstants.InvoiceRecurrenceFields.DOCUMENT_INITIATOR_USER_PRINCIPAL_NAME,
                    ArPropertyConstants.InvoiceRecurrenceFields.INVOICE_RECURRENCE_INITIATOR_USER_ID);

    @Override
    public Map<String, String> getMappingsFromPrincipalNameFieldsToPrincipalIdFields() {
        return PRINCIPAL_MAPPINGS;
    }
}
