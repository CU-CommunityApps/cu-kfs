package edu.cornell.kfs.pdp.businessobject.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.lookup.Lookupable;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.businessobject.ACHPayee;
import org.kuali.kfs.pdp.businessobject.service.ACHPayeeSearchService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.util.MultiValueMap;

import edu.cornell.kfs.pdp.businessobject.CuACHPayee;

public class CuACHPayeeSearchService extends ACHPayeeSearchService{
    
    public CuACHPayeeSearchService(DataDictionaryService dataDictionaryService,
            DisbursementVoucherPayeeService disbursementVoucherPayeeService, Lookupable vendorLookupable,
            PersonService personService) {
        super(dataDictionaryService, disbursementVoucherPayeeService, vendorLookupable, personService);
    }

    protected Map<String, String> getPersonFieldValues(final Map<String, String> fieldValues) {
        final Map<String, String> personFieldValues = super.getPersonFieldValues(fieldValues);
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
    
    protected DisbursementPayee getPayeeFromPerson(final Person personDetail, final Map<String,String> fieldValues) {
        final ACHPayee payee = (ACHPayee) super.getPayeeFromPerson(personDetail, fieldValues);
        
        final CuACHPayee cuPayee = new CuACHPayee();
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
    
    protected ACHPayee getPayeeFromVendor(final VendorDetail vendorDetail) {
        final ACHPayee payee = (ACHPayee) super.getPayeeFromVendor(vendorDetail);
        
        final CuACHPayee cuPayee = new CuACHPayee();
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
