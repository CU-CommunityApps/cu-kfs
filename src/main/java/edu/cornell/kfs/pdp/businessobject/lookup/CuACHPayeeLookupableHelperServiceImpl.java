package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.Map;

import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kim.impl.KIMPropertyConstants;

public class CuACHPayeeLookupableHelperServiceImpl extends ACHPayeeLookupableHelperServiceImpl {
	

    /**
     * Cornell customization adds entity id to the search fields and removes restriction of employee status active when payee type code is Entity.
     * 
     * @see org.kuali.kfs.fp.businessobject.lookup.AbstractPayeeLookupableHelperServiceImpl#getPersonFieldValues(java.util.Map)
     */
    @Override
    protected Map<String, String> getPersonFieldValues(Map<String, String> fieldValues) {
        Map<String, String> personFieldValues = super.getPersonFieldValues(fieldValues);
        
        String payeeTypeCode = fieldValues.get(KFSPropertyConstants.PAYEE_TYPE_CODE);

		// if payee type Entity then do not restrict search on entries with
		// employee status code active; the entity type code is used for alumni
		// and retirees that are inactive
        
        if (PdpConstants.PayeeIdTypeCodes.ENTITY.equals(payeeTypeCode)) {
        	personFieldValues.remove(KIMPropertyConstants.Person.EMPLOYEE_STATUS_CODE);
        }

        // add entity
        personFieldValues.put(KIMPropertyConstants.Person.ENTITY_ID, fieldValues.get(KIMPropertyConstants.Person.ENTITY_ID));

        return personFieldValues;
    }

}