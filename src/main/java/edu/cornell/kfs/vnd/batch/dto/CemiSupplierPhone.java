package edu.cornell.kfs.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierPhone {

    private VendorPhoneNumber vendorPhoneNumber;
    private String supplierId;
    private String phoneId;
    private String country;
    private String internationalPhoneCode;
    private String phoneNumber;
    private String phoneExtension;
    private String phoneDeviceType;
    private String phonePrimary;
    private String phoneUse;
    private String phoneUse2;
    private String phoneUse3;
    private String phoneUse4;
    private String phoneUseTenanted;
    private String phoneUseTenanted2;
    private String phoneUseTenanted3;
    private String phoneUseTenanted4;
    private String comments;

    public CemiSupplierPhone(final VendorPhoneNumber vendorPhoneNumber, final String supplierId, int phoneNumberCount) {
        this.vendorPhoneNumber = vendorPhoneNumber;
        this.supplierId = supplierId;
        this.phoneId = buildSupplierPhoneId(vendorPhoneNumber, supplierId, phoneNumberCount);
        this.country = CemiVendorConstants.COUNTRY_CODE_UNITED_STATES;
        this.internationalPhoneCode = CemiVendorConstants.DEFAULT_INTERNATIONAL_PHONE_TYPE;
        this.phoneNumber = vendorPhoneNumber.getVendorPhoneNumber();
        this.phoneExtension = vendorPhoneNumber.getVendorPhoneExtensionNumber();
        this.phoneDeviceType = CemiVendorConstants.DEFAULT_PHONE_DEVICE_TYPE;
        this.phonePrimary = CemiUtils.convertToBooleanValueForFileExtract(setFirstSupplierPhoneNumberAsPrimary(phoneNumberCount));
        assignPhoneUseValuesBasedOnPhoneType(vendorPhoneNumber.getVendorPhoneTypeCode());
        
        //columns not populated with this load
        this.phoneExtension = CemiVendorConstants.EMPTY_STRING;
        this.phoneUse2 = CemiVendorConstants.EMPTY_STRING;
        this.phoneUse3 = CemiVendorConstants.EMPTY_STRING;
        this.phoneUse4 = CemiVendorConstants.EMPTY_STRING;
        this.comments = CemiVendorConstants.EMPTY_STRING;
        this.phoneUseTenanted = CemiVendorConstants.EMPTY_STRING;
        this.phoneUseTenanted2 = CemiVendorConstants.EMPTY_STRING;
        this.phoneUseTenanted3 = CemiVendorConstants.EMPTY_STRING;
        this.phoneUseTenanted4 = CemiVendorConstants.EMPTY_STRING;
    }
    
    private static String buildSupplierPhoneId(final VendorPhoneNumber vendorPhoneNumber, final String supplierId, int phoneNumberCount) {
        return MessageFormat.format(CemiVendorConstants.PHONE_ID_FORMAT,
                supplierId,
                vendorPhoneNumber.getVendorPhoneNumber(),
                Integer.toString(phoneNumberCount));
    }
    
    private static boolean setFirstSupplierPhoneNumberAsPrimary(int phoneNumberCount) {
        return phoneNumberCount == 1;
    }
    
    private void assignPhoneUseValuesBasedOnPhoneType(String vendorPhoneTypeCode) {
        if (StringUtils.isNotBlank(vendorPhoneTypeCode)
                && CemiVendorConstants.PHONE_USES.containsKey(vendorPhoneTypeCode)) {
            List<String> useValuesList = CemiVendorConstants.PHONE_USES.get(vendorPhoneTypeCode);
            if (useValuesList.size() == 1) {
                setPhoneUse(useValuesList.get(0));
            } else {
                setPhoneUse(CemiVendorConstants.EMPTY_STRING);
            }
        }
    }

    public VendorPhoneNumber getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    public void setVendorPhoneNumber(VendorPhoneNumber vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getInternationalPhoneCode() {
        return internationalPhoneCode;
    }

    public void setInternationalPhoneCode(String internationalPhoneCode) {
        this.internationalPhoneCode = internationalPhoneCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneDeviceType() {
        return phoneDeviceType;
    }

    public void setPhoneDeviceType(String phoneDeviceType) {
        this.phoneDeviceType = phoneDeviceType;
    }

    public String getPhonePrimary() {
        return phonePrimary;
    }

    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }

    public String getPhoneUse() {
        return phoneUse;
    }

    public void setPhoneUse(String phoneUse) {
        this.phoneUse = phoneUse;
    }

    public String getPhoneUse2() {
        return phoneUse2;
    }

    public void setPhoneUse2(String phoneUse2) {
        this.phoneUse2 = phoneUse2;
    }

    public String getPhoneUse3() {
        return phoneUse3;
    }

    public void setPhoneUse3(String phoneUse3) {
        this.phoneUse3 = phoneUse3;
    }

    public String getPhoneUse4() {
        return phoneUse4;
    }

    public void setPhoneUse4(String phoneUse4) {
        this.phoneUse4 = phoneUse4;
    }

    public String getPhoneUseTenanted() {
        return phoneUseTenanted;
    }

    public void setPhoneUseTenanted(String phoneUseTenanted) {
        this.phoneUseTenanted = phoneUseTenanted;
    }

    public String getPhoneUseTenanted2() {
        return phoneUseTenanted2;
    }

    public void setPhoneUseTenanted2(String phoneUseTenanted2) {
        this.phoneUseTenanted2 = phoneUseTenanted2;
    }

    public String getPhoneUseTenanted3() {
        return phoneUseTenanted3;
    }

    public void setPhoneUseTenanted3(String phoneUseTenanted3) {
        this.phoneUseTenanted3 = phoneUseTenanted3;
    }

    public String getPhoneUseTenanted4() {
        return phoneUseTenanted4;
    }

    public void setPhoneUseTenanted4(String phoneUseTenanted4) {
        this.phoneUseTenanted4 = phoneUseTenanted4;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
