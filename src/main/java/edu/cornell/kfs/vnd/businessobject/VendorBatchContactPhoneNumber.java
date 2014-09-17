package edu.cornell.kfs.vnd.businessobject;

/**
 *  non-persistable to hold vendor contact phone number converted from input data file
 **/
public class VendorBatchContactPhoneNumber {
    private String vendorContactGeneratedIdentifier;
    private String vendorContactPhoneGeneratedIdentifier;
    private String vendorPhoneTypeCode;
    private String vendorPhoneNumber;
    private String vendorPhoneExtensionNumber;
    private String active;
    
    public VendorBatchContactPhoneNumber(String[] phoneNumber) {
        vendorContactGeneratedIdentifier = phoneNumber[0];
        vendorContactPhoneGeneratedIdentifier = phoneNumber[1];
        vendorPhoneTypeCode = phoneNumber[2];
        vendorPhoneNumber = phoneNumber[3];
        vendorPhoneExtensionNumber = phoneNumber[4];
        active = phoneNumber[5];
    }
    
    public String getVendorPhoneTypeCode() {
        return vendorPhoneTypeCode;
    }
    public void setVendorPhoneTypeCode(String vendorPhoneTypeCode) {
        this.vendorPhoneTypeCode = vendorPhoneTypeCode;
    }
    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }
    public void setVendorPhoneNumber(String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }
    public String getVendorPhoneExtensionNumber() {
        return vendorPhoneExtensionNumber;
    }
    public void setVendorPhoneExtensionNumber(String vendorPhoneExtensionNumber) {
        this.vendorPhoneExtensionNumber = vendorPhoneExtensionNumber;
    }
    public String getActive() {
        return active;
    }
    public void setActive(String active) {
        this.active = active;
    }

    public String getVendorContactGeneratedIdentifier() {
        return vendorContactGeneratedIdentifier;
    }

    public void setVendorContactGeneratedIdentifier(
            String vendorContactGeneratedIdentifier) {
        this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
    }

    public String getVendorContactPhoneGeneratedIdentifier() {
        return vendorContactPhoneGeneratedIdentifier;
    }

    public void setVendorContactPhoneGeneratedIdentifier(
            String vendorContactPhoneGeneratedIdentifier) {
        this.vendorContactPhoneGeneratedIdentifier = vendorContactPhoneGeneratedIdentifier;
    }


}
