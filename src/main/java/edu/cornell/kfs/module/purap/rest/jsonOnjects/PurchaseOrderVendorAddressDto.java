package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

public class PurchaseOrderVendorAddressDto {

    private String vendorAddressTypeCode;
    private boolean defaultAddressIndicator;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorAddressEmailAddress;

    public PurchaseOrderVendorAddressDto() {

    }

    public PurchaseOrderVendorAddressDto(VendorAddress address) {
        this.vendorAddressTypeCode = address.getVendorAddressTypeCode();
        this.defaultAddressIndicator = address.isVendorDefaultAddressIndicator();
        this.vendorLine1Address = address.getVendorLine1Address();
        this.vendorLine2Address = address.getVendorLine2Address();
        this.vendorCityName = address.getVendorCityName();
        this.vendorStateCode = address.getVendorStateCode();
        this.vendorZipCode = address.getVendorZipCode();
        this.vendorAddressEmailAddress = address.getVendorAddressEmailAddress();
    }

    public String getVendorAddressTypeCode() {
        return vendorAddressTypeCode;
    }

    public void setVendorAddressTypeCode(String vendorAddressTypeCode) {
        this.vendorAddressTypeCode = vendorAddressTypeCode;
    }

    public boolean isDefaultAddressIndicator() {
        return defaultAddressIndicator;
    }

    public void setDefaultAddressIndicator(boolean defaultAddressIndicator) {
        this.defaultAddressIndicator = defaultAddressIndicator;
    }

    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    public void setVendorLine1Address(String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    public void setVendorLine2Address(String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    public String getVendorCityName() {
        return vendorCityName;
    }

    public void setVendorCityName(String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    public String getVendorStateCode() {
        return vendorStateCode;
    }

    public void setVendorStateCode(String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    public String getVendorZipCode() {
        return vendorZipCode;
    }

    public void setVendorZipCode(String vendorZipCode) {
        this.vendorZipCode = vendorZipCode;
    }

    public String getVendorAddressEmailAddress() {
        return vendorAddressEmailAddress;
    }

    public void setVendorAddressEmailAddress(String vendorAddressEmailAddress) {
        this.vendorAddressEmailAddress = vendorAddressEmailAddress;
    }

}
