package edu.cornell.kfs.fp.businessobject.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.lookup.DisbursementPayeeLookupableHelperServiceImpl;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliation;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuDisbursementPayeeLookupableHelperServiceImpl extends DisbursementPayeeLookupableHelperServiceImpl {
        
    private static final String ACTIVE = "A";
    private static final String RETIRED = "R";
    
    /**
     * @see org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
    		List<DisbursementPayee> searchResults = new ArrayList<>();

        if (StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER)) 
                || StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.VENDOR_NAME))) {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
        } else if (StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.EMPLOYEE_ID)) 
                || StringUtils.isNotBlank(fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME))) {
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        } else {
            searchResults.addAll(this.getVendorsAsPayees(fieldValues));
            searchResults.addAll(this.getPersonAsPayees(fieldValues));
        }

        return sortSearchResults( searchResults);
   }
            
    @Override
    public void validateSearchParameters(Map fieldValues) {
        super.validateSearchParameters(fieldValues);

        if (checkMinimumFieldsFilled(fieldValues)) {
            validateVendorNameUse(fieldValues);
            validateEmployeeNameUse(fieldValues);
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException("errors in search criteria");
        }
    }
    
    @Deprecated
    public boolean checkMinimumFieldsFilled(Map fieldValues) {
        final String principalName = (String) fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME);  
        
        if (StringUtils.isBlank((String) fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER))
                && StringUtils.isBlank((String) fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID))
                && StringUtils.isBlank((String) fieldValues.get(KIMPropertyConstants.Person.FIRST_NAME)) 
                && StringUtils.isBlank((String) fieldValues.get(KIMPropertyConstants.Person.LAST_NAME)) && StringUtils.isBlank((String) 
                        fieldValues.get(KFSPropertyConstants.VENDOR_NAME))  && StringUtils.isBlank(principalName)) {
            final String vendorNumberLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
            final String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            final String firstNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
            final String lastNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
            final String employeeIdLabel = this.getAttributeLabel(KIMPropertyConstants.Person.EMPLOYEE_ID);
            final String principalNameLabel = this.getAttributeLabel(KIMPropertyConstants.Principal.PRINCIPAL_NAME);

            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NUMBER,
                    FPKeyConstants.ERROR_DV_LOOKUP_NEEDS_SOME_FIELD, vendorNumberLabel, employeeIdLabel,
                    vendorNameLabel, firstNameLabel, lastNameLabel, principalNameLabel);
            return false;
        }
        return true;
    }
    
    @Override
    @Deprecated
    public void validateVendorNameUse(Map fieldValues) {
        final String vendorName = (String) fieldValues.get(KFSPropertyConstants.VENDOR_NAME);
        final String vendorNumber = (String) fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER);
        final String employeeId = (String) fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID);
        final String principalName = (String) fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME);  
   
        final boolean isVendorInfoEntered = StringUtils.isNotBlank(vendorName) || StringUtils.isNotBlank(vendorNumber);
        final boolean isEmployeeInfoEntered = StringUtils.isNotBlank(employeeId) 
                || StringUtils.isNotBlank(principalName); 
  
        if (isVendorInfoEntered && isEmployeeInfoEntered) {
            // only can use the vendor name and vendor number fields or the employee id field, but not both.
            String messageKey = FPKeyConstants.ERROR_DV_VENDOR_EMPLOYEE_CONFUSION;
            String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            String vendorNumberLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NUMBER);
            String principalNameLabel = this.getAttributeLabel(KIMPropertyConstants.Principal.PRINCIPAL_NAME); 
            

            GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.EMPLOYEE_ID, messageKey, 
                    this.getAttributeLabel(KIMPropertyConstants.Person.EMPLOYEE_ID), vendorNameLabel, vendorNumberLabel, principalNameLabel);
        }

        if (StringUtils.isBlank(vendorNumber) && !StringUtils.isBlank(vendorName) && !filledEnough(vendorName)) {
            final String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME,
                    FPKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, vendorNameLabel,
                    Integer.toString(getNameLengthWithWildcardRequirement()));
        }
    }
    
    @Override
    @Deprecated
    public void validateEmployeeNameUse(Map fieldValues) {
        final String firstName = (String) fieldValues.get(KIMPropertyConstants.Person.FIRST_NAME);
        final String lastName = (String) fieldValues.get(KIMPropertyConstants.Person.LAST_NAME);
        final String vendorName = (String) fieldValues.get(KFSPropertyConstants.VENDOR_NAME);
        final String employeeId = (String) fieldValues.get(KIMPropertyConstants.Person.EMPLOYEE_ID);
        final boolean isPersonNameEntered = StringUtils.isNotBlank(firstName) || StringUtils.isNotBlank(lastName);
        final String principalName = (String) fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME);  
        

        if (isPersonNameEntered && StringUtils.isNotBlank(vendorName)) {
            // only can use the person first and last name fields or the vendor name field, but not both.
            String messageKey = FPKeyConstants.ERROR_DV_VENDOR_NAME_PERSON_NAME_CONFUSION;

            String vendorNameLabel = this.getAttributeLabel(KFSPropertyConstants.VENDOR_NAME);
            String firstNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
            String lastNameLabel = this.getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.VENDOR_NAME, messageKey, vendorNameLabel,
                    firstNameLabel, lastNameLabel);
        }
        if (StringUtils.isBlank(employeeId)) {  
            if (StringUtils.isBlank(principalName)) {
                if (StringUtils.isBlank(firstName) && !StringUtils.isBlank(lastName) && !filledEnough(lastName)) {
                    final String label = getAttributeLabel(KIMPropertyConstants.Person.LAST_NAME);
                    GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.LAST_NAME,
                            FPKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, label,
                            Integer.toString(getNameLengthWithWildcardRequirement()));
                } else if (StringUtils.isBlank(lastName) && !StringUtils.isBlank(firstName) && !filledEnough(firstName)) {
                    final String label = getAttributeLabel(KIMPropertyConstants.Person.FIRST_NAME);
                    GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Person.FIRST_NAME,
                            FPKeyConstants.ERROR_DV_NAME_NOT_FILLED_ENOUGH, label,
                            Integer.toString(getNameLengthWithWildcardRequirement()));
                }
            }
        }

    }
    
    @Override
    protected List<DisbursementPayee> getPersonAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();

        Map<String, String> fieldsForLookup = this.getPersonFieldValues(fieldValues);
        List<Person> persons = SpringContext.getBean(PersonService.class).findPeople(fieldsForLookup);
        
       boolean warningExists = false;
        
        for (Person personDetail : persons) {
            for(EntityAffiliation entityAffiliation : (personDetail).getAffiliations()) {
                if(entityAffiliation.isDefaultValue()) {
                    if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.STUDENT)) {
                        CuDisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues, CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT);
                        payeeList.add(payee);
                    }
                    else if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.ALUMNI)) {
                        CuDisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues, CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI);
                        payeeList.add(payee);
                    }
                    else if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.FACULTY) ||
                            StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.STAFF)) {
                        if (StringUtils.isNotBlank(personDetail.getEmployeeStatusCode()) && 
                                (personDetail.getEmployeeStatusCode().equals(ACTIVE)) || personDetail.getEmployeeStatusCode().equals(RETIRED)) {
                            CuDisbursementPayee payee = getPayeeFromPerson(personDetail, fieldValues, KFSConstants.PaymentPayeeTypes.EMPLOYEE);
                            payeeList.add(payee);
                        }
                        else {
                            if  (GlobalVariables.getMessageMap().containsMessageKey(CUKFSKeyConstants.WARNING_DV_PAYEE_MUST_BE_ACTIVE)) {
                                    warningExists = true;
                                    break;
                                }
                            }
                            if(!warningExists) {
                                GlobalVariables.getMessageMap().putWarningWithoutFullErrorPath(KFSPropertyConstants.PRINCIPAL_ID, CUKFSKeyConstants.WARNING_DV_PAYEE_MUST_BE_ACTIVE);     
                        }
                    }
                    break;
                }
            }
        }

        return payeeList;
    }
    
protected List<DisbursementPayee> getVendorsAsPayees(Map<String, String> fieldValues) {
        List<DisbursementPayee> payeeList = new ArrayList<DisbursementPayee>();
    
        Map<String, String> fieldsForLookup = this.getVendorFieldValues(fieldValues);
        vendorLookupable.setBusinessObjectClass(VendorDetail.class);
        vendorLookupable.validateSearchParameters(fieldsForLookup);
    
        List<? extends BusinessObject> vendorList = vendorLookupable.getSearchResults(fieldsForLookup);
        for (BusinessObject vendor : vendorList) {
            VendorDetail vendorDetail = (VendorDetail) vendor;
            CuDisbursementPayee payee = getPayeeFromVendor(vendorDetail, fieldValues);
            payeeList.add(payee);
        }
    
        return payeeList;
    }


    @Override
    protected Map<String, String> getPersonFieldValues(Map<String, String> fieldValues) {
        Map<String, String> personFieldValues = new HashMap<>();
        personFieldValues.put(KFSPropertyConstants.PERSON_FIRST_NAME,
                fieldValues.get(KFSPropertyConstants.PERSON_FIRST_NAME));
        personFieldValues.put(KFSPropertyConstants.PERSON_LAST_NAME,
                fieldValues.get(KFSPropertyConstants.PERSON_LAST_NAME));
        personFieldValues.put(KFSPropertyConstants.EMPLOYEE_ID, fieldValues.get(KFSPropertyConstants.EMPLOYEE_ID));       
        personFieldValues.put(KFSPropertyConstants.ACTIVE, fieldValues.get(KFSPropertyConstants.ACTIVE));
        personFieldValues.put(KFSPropertyConstants.PERSON_USER_IDENTIFIER, fieldValues.get(KIMPropertyConstants.Principal.PRINCIPAL_NAME));
        
        Map<String, String> fieldConversionMap =
                disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndPerson();
        this.replaceFieldKeys(personFieldValues, fieldConversionMap);
        

        return personFieldValues;
    }
 
    
    
    protected CuDisbursementPayee getPayeeFromPerson(Person personDetail, Map<String, String> fieldValues, String payeeTypeCode) {
        DisbursementPayee payee =  ((CuDisbursementVoucherPayeeService)disbursementVoucherPayeeService).getPayeeFromPerson(personDetail, payeeTypeCode);
        payee.setPaymentReasonCode(fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE));

        return (CuDisbursementPayee) payee;
    }
    

    
    protected Map<String, String> getVendorFieldValues(Map<String, String> fieldValues) {
        Map<String, String> vendorFieldValues = new HashMap<>();
        vendorFieldValues.put(KFSPropertyConstants.TAX_NUMBER, fieldValues.get(KFSPropertyConstants.TAX_NUMBER));
        vendorFieldValues.put(KFSPropertyConstants.VENDOR_NAME, fieldValues.get(KFSPropertyConstants.VENDOR_NAME));
        vendorFieldValues.put(KFSPropertyConstants.VENDOR_NUMBER, fieldValues.get(KFSPropertyConstants.VENDOR_NUMBER));
        vendorFieldValues.put(KFSPropertyConstants.PERSON_FIRST_NAME, fieldValues.get(KFSPropertyConstants.PERSON_FIRST_NAME));
        vendorFieldValues.put(KFSPropertyConstants.PERSON_LAST_NAME, fieldValues.get(KFSPropertyConstants.PERSON_LAST_NAME));
        vendorFieldValues.put(KFSPropertyConstants.ACTIVE, fieldValues.get(KFSPropertyConstants.ACTIVE));

        Map<String, String> fieldConversionMap = disbursementVoucherPayeeService.getFieldConversionBetweenPayeeAndVendor();
        this.replaceFieldKeys(vendorFieldValues, fieldConversionMap);

        String vendorName = this.getVendorName(vendorFieldValues);
        if (StringUtils.isNotBlank(vendorName)) {
            vendorFieldValues.put(KFSPropertyConstants.VENDOR_NAME, vendorName);
        }

        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_FIRST_NAME);
        vendorFieldValues.remove(VendorPropertyConstants.VENDOR_LAST_NAME);

        return vendorFieldValues;
    }
    
    
    protected CuDisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail, Map<String, String> fieldValues) {
        CuDisbursementPayee payee =  ((CuDisbursementVoucherPayeeService)disbursementVoucherPayeeService).getPayeeFromVendor(vendorDetail);
        payee.setPaymentReasonCode(fieldValues.get(KFSPropertyConstants.PAYMENT_REASON_CODE));

        return payee;
    }
}
