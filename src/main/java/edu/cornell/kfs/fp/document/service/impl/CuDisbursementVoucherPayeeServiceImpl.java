package edu.cornell.kfs.fp.document.service.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPayeeServiceImpl;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.CuDisbursementPayee;
import edu.cornell.kfs.fp.businessobject.CuDisbursementVoucherPayeeDetail;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;



public class CuDisbursementVoucherPayeeServiceImpl extends DisbursementVoucherPayeeServiceImpl implements CuDisbursementVoucherPayeeService {
    
    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#getPayeeFromVendor(org.kuali.kfs.vnd.businessobject.VendorDetail)
     */
    @Override
    public CuDisbursementPayee getPayeeFromVendor(VendorDetail vendorDetail) {
        CuDisbursementPayee disbursementPayee = new CuDisbursementPayee();

        disbursementPayee.setActive(vendorDetail.isActiveIndicator());

        disbursementPayee.setPayeeIdNumber(vendorDetail.getVendorNumber());
        disbursementPayee.setPayeeName(vendorDetail.getAltVendorName());
        disbursementPayee.setTaxNumber(vendorDetail.getVendorHeader().getVendorTaxNumber());

        String vendorTypeCode = vendorDetail.getVendorHeader().getVendorTypeCode();
        String payeeTypeCode = getVendorPayeeTypeCodeMapping().get(vendorTypeCode);
        disbursementPayee.setPayeeTypeCode(payeeTypeCode);

        String vendorAddress = MessageFormat.format(addressPattern, vendorDetail.getDefaultAddressLine1(),
                vendorDetail.getDefaultAddressCity(), vendorDetail.getDefaultAddressStateCode(),
                vendorDetail.getDefaultAddressCountryCode());
        disbursementPayee.setAddress(vendorAddress);

        return disbursementPayee;
    }
    
    
    @Override
    public String getPayeeTypeDescription(String payeeTypeCode) {
        String payeeTypeDescription = StringUtils.EMPTY;

        if (KFSConstants.PaymentPayeeTypes.EMPLOYEE.equals(payeeTypeCode) || 
        CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode) ||
        CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class, FPParameterConstants.NON_VENDOR_EMPLOYEE_PAYEE_TYPE_LABEL);
        }
        else if (KFSConstants.PaymentPayeeTypes.VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(CuDisbursementVoucherDocument.class, FPParameterConstants.PO_AND_DV_PAYEE_TYPE_LABEL);
        }
        else if (KFSConstants.PaymentPayeeTypes.REVOLVING_FUND_VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = this.getVendorTypeDescription(VendorConstants.VendorTypes.REVOLVING_FUND);
        }
        else if (KFSConstants.PaymentPayeeTypes.SUBJECT_PAYMENT_VENDOR.equals(payeeTypeCode)) {
            payeeTypeDescription = this.getVendorTypeDescription(VendorConstants.VendorTypes.SUBJECT_PAYMENT);
        }
        else if (KFSConstants.PaymentPayeeTypes.CUSTOMER.equals(payeeTypeCode)) {
            payeeTypeDescription = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class, DisbursementVoucherConstants.PAYEE_TYPE_NAME);
        }

        return payeeTypeDescription;
    }
    
    
    public DisbursementPayee getPayeeFromPerson(Person person, String payeeTypeCode) {
        CuDisbursementPayee disbursementPayee = new CuDisbursementPayee();

        disbursementPayee.setActive(person.isActive());
        
        Collection<String> payableEmplStatusCodes = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(CuDisbursementVoucherDocument.class, CuDisbursementVoucherConstants.ALLOWED_EMPLOYEE_STATUSES_FOR_PAYMENT);

        if (StringUtils.equalsIgnoreCase(payeeTypeCode, KFSConstants.PaymentPayeeTypes.EMPLOYEE) && StringUtils.isNotBlank(person.getEmployeeId()) && payableEmplStatusCodes.contains(person.getEmployeeStatusCode())) {
            disbursementPayee.setPayeeIdNumber(person.getEmployeeId());
        } else {
            disbursementPayee.setPayeeIdNumber(person.getPrincipalId());
        }

        disbursementPayee.setPrincipalId(person.getPrincipalId());
        disbursementPayee.setPrincipalName(person.getPrincipalName()); 
        
        disbursementPayee.setPayeeName(person.getNameUnmasked());
        disbursementPayee.setTaxNumber(KFSConstants.BLANK_SPACE);

        disbursementPayee.setPayeeTypeCode(KFSConstants.PaymentPayeeTypes.EMPLOYEE);

        disbursementPayee.setPayeeTypeCode(payeeTypeCode);
        
        String personAddress = MessageFormat.format(addressPattern, person.getAddressLine1Unmasked(), person.getAddressCityUnmasked(), person.getAddressStateProvinceCodeUnmasked(), person.getAddressCountryCode() == null ? "" : person.getAddressCountryCode());
        disbursementPayee.setAddress(personAddress);

        return (DisbursementPayee) disbursementPayee;
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#getFieldConversionBetweenPayeeAndPerson()
     */
    @Override
    public Map<String, String> getFieldConversionBetweenPayeeAndPerson() {
        Map<String, String> fieldConversionMap = super.getFieldConversionBetweenPayeeAndPerson();
        fieldConversionMap.put(KFSPropertyConstants.PERSON_USER_IDENTIFIER, KIMPropertyConstants.Principal.PRINCIPAL_NAME);
        return fieldConversionMap;
    }
    
    public boolean isStudent(CuDisbursementVoucherPayeeDetail dvPayeeDetail) {
        String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementPayee)
     */
    public boolean isStudent(CuDisbursementPayee payee) {
        String payeeTypeCode = payee.getPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_STUDENT.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail)
     */
    public boolean isAlumni(CuDisbursementVoucherPayeeDetail dvPayeeDetail) {
        String payeeTypeCode = dvPayeeDetail.getDisbursementVoucherPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode);
    }

    /**
     * @see org.kuali.kfs.fp.document.service.DisbursementVoucherPayeeService#isVendor(org.kuali.kfs.fp.businessobject.DisbursementPayee)
     */
    public boolean isAlumni(CuDisbursementPayee payee) {
        String payeeTypeCode = payee.getPayeeTypeCode();
        return CuDisbursementVoucherConstants.DV_PAYEE_TYPE_ALUMNI.equals(payeeTypeCode);
    }

    @Override
    public boolean isPayeeSignedUpForACH(DisbursementVoucherPayeeDetail disbursementVoucherPayeeDetail) {
        boolean result = false;

        if (ObjectUtils.isNotNull(disbursementVoucherPayeeDetail)) {
            String payeeTypeCode = disbursementVoucherPayeeDetail.getDisbursementVoucherPayeeTypeCode();
            String payeeIdNumber = disbursementVoucherPayeeDetail.getDisbVchrPayeeIdNumber();

            result = payeeACHService.isPayeeSignedUpForACH(payeeTypeCode, payeeIdNumber);

            if (!result) {
                result = payeeACHService.isPayeeSignedUpForACH(PdpConstants.PayeeIdTypeCodes.ENTITY, getPayeeEntityId(payeeIdNumber));
            }
        }

        return result;
    }
    
    protected String getPayeeEntityId(String payeeIdNumber) {
        String entityId = StringUtils.EMPTY;

        Person person = personService.getPersonByEmployeeId(payeeIdNumber);
        if (ObjectUtils.isNotNull(person)) {
            entityId = person.getEntityId();
        } else {
            entityId = payeeIdNumber;
        }

        return entityId;
    }

    @Override
    public String getPayeeTypeCodeForVendorType(String vendorTypeCode) {
        if (StringUtils.isBlank(vendorTypeCode)) {
            return null;
        }
        return getVendorPayeeTypeCodeMapping().get(vendorTypeCode);
    }
}
