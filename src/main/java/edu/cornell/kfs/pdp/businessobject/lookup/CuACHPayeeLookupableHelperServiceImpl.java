package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.ACHPayee;
import org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.kns.lookup.CollectionIncomplete;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.pdp.businessobject.CuACHPayee;

public class CuACHPayeeLookupableHelperServiceImpl extends ACHPayeeLookupableHelperServiceImpl {
	

    /**
     * Cornell customization adds entity id to the search fields and removes restriction of employee status active when payee type code is Entity.
     * Also adds principal name to the search fields.
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
        // add principal name
        personFieldValues.put(KIMPropertyConstants.Principal.PRINCIPAL_NAME, fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME));

        return personFieldValues;
    }

    /**
     * Overridden to only search for employee or entity payees when principal name is specified.
     * 
     * @see org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        /*
         * This is mostly a copy of the superclass's method, but has been tweaked to account for principal name
         * and to conform to our line formatting standards.
         */
        List<DisbursementPayee> searchResults = new ArrayList<>();

        String payeeTypeCode = fieldValues.get(KFSPropertyConstants.PAYEE_TYPE_CODE);
        if (StringUtils.isBlank(payeeTypeCode)) {
            GlobalVariables.getMessageMap().putInfo(KFSPropertyConstants.PAYEE_TYPE_CODE,
                    PdpKeyConstants.MESSAGE_PDP_ACH_PAYEE_LOOKUP_NO_PAYEE_TYPE);
        }

        // CU Customization: Updated "else if" to restrict results to people if principal name is given.
        if (StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER))
                || StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NAME))
                || StringUtils.isNotBlank(payeeTypeCode)
                        && PdpConstants.PayeeIdTypeCodes.VENDOR_ID.equals(payeeTypeCode)) {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
        } else if (StringUtils.isNotBlank(fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID))
                || StringUtils.isNotBlank(fieldValues.get(KIMPropertyConstants.Person.ENTITY_ID))
                || StringUtils.isNotBlank(fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME))
                || StringUtils.isNotBlank(payeeTypeCode)
                        && (PdpConstants.PayeeIdTypeCodes.EMPLOYEE.equals(payeeTypeCode)
                                || PdpConstants.PayeeIdTypeCodes.ENTITY.equals(payeeTypeCode))) {
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        } else {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        }

        CollectionIncomplete results = new CollectionIncomplete(searchResults, (long) searchResults.size());

        // sort list if default sort column given
        List<String> defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            results.sort(new BeanPropertyComparator(getDefaultSortColumns(), true));
        }

        return results;
    }

    /**
     * Overridden to return an instance of CuACHPayee instead of ACHPayee, and to populate the "principalName" property.
     * 
     * @see org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl#getPayeeFromPerson(Person, Map)
     */
    @Override
    protected DisbursementPayee getPayeeFromPerson(Person personDetail, Map<String,String> fieldValues) {
        ACHPayee payee = (ACHPayee) super.getPayeeFromPerson(personDetail, fieldValues);
        
        CuACHPayee cuPayee = new CuACHPayee();
        cuPayee.setPayeeIdNumber(payee.getPayeeIdNumber());
        cuPayee.setPayeeTypeCode(payee.getPayeeTypeCode());
        cuPayee.setPayeeName(payee.getPayeeName());
        cuPayee.setPrincipalId(payee.getPrincipalId());
        cuPayee.setTaxNumber(payee.getTaxNumber());
        cuPayee.setAddress(payee.getAddress());
        cuPayee.setActive(payee.isActive());
        cuPayee.setPrincipalName(personDetail.getPrincipalName());
        
        return cuPayee;
    }

    /**
     * Overridden to return an instance of CuACHPayee instead of ACHPayee, for consistency with other overrides in this service.
     * 
     * @see org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl#getPayeeFromVendor(VendorDetail, Map)
     */
    @Override
    protected DisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail, Map<String,String> fieldValues) {
        ACHPayee payee = (ACHPayee) super.getPayeeFromVendor(vendorDetail, fieldValues);
        
        CuACHPayee cuPayee = new CuACHPayee();
        cuPayee.setPayeeIdNumber(payee.getPayeeIdNumber());
        cuPayee.setPayeeTypeCode(payee.getPayeeTypeCode());
        cuPayee.setPayeeName(payee.getPayeeName());
        cuPayee.setPrincipalId(payee.getPrincipalId());
        cuPayee.setTaxNumber(payee.getTaxNumber());
        cuPayee.setAddress(payee.getAddress());
        cuPayee.setActive(payee.isActive());
        
        return cuPayee;
    }

}