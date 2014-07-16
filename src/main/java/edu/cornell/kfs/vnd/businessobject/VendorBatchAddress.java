package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang.StringUtils;

/**
 *  non-persistable to hold vendor address converted from input data file
 **/
public class VendorBatchAddress {

    private String vendorAddressGeneratedIdentifier;
    private String vendorAddressTypeCode;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorCountryCode;
    private String vendorAttentionName;
    private String vendorAddressInternationalProvinceName;
    private String vendorAddressEmailAddress;
    private String vendorBusinessToBusinessUrlAddress;
    private String vendorFaxNumber;
    private String vendorDefaultAddressIndicator;
    private String purchaseOrderTransmissionMethodCode;
    private String active;
    
    public VendorBatchAddress(String[] address) {
        vendorAddressGeneratedIdentifier = address[0];
        vendorAddressTypeCode = address[1];
        vendorLine1Address = address[2];
        vendorLine2Address = address[3];
        vendorCityName = address[4];
        vendorStateCode = address[5];
        vendorZipCode = address[6];
        vendorCountryCode = address[7];
        vendorAttentionName = address[8];
        vendorAddressInternationalProvinceName = address[9];
        vendorAddressEmailAddress = address[10];
        vendorBusinessToBusinessUrlAddress = address[11];
        if (StringUtils.isNotBlank(address[12])) {
            vendorFaxNumber = address[12];
        }
        vendorDefaultAddressIndicator = address[13];
        purchaseOrderTransmissionMethodCode = address[14];
        active = address[15];
    }
    
    public String getVendorAddressTypeCode() {
        return vendorAddressTypeCode;
    }
    public void setVendorAddressTypeCode(String vendorAddressTypeCode) {
        this.vendorAddressTypeCode = vendorAddressTypeCode;
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
    public String getVendorCountryCode() {
        return vendorCountryCode;
    }
    public void setVendorCountryCode(String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
    }
    public String getVendorAttentionName() {
        return vendorAttentionName;
    }
    public void setVendorAttentionName(String vendorAttentionName) {
        this.vendorAttentionName = vendorAttentionName;
    }
    public String getVendorAddressInternationalProvinceName() {
        return vendorAddressInternationalProvinceName;
    }
    public void setVendorAddressInternationalProvinceName(
            String vendorAddressInternationalProvinceName) {
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
    }
    public String getVendorAddressEmailAddress() {
        return vendorAddressEmailAddress;
    }
    public void setVendorAddressEmailAddress(String vendorAddressEmailAddress) {
        this.vendorAddressEmailAddress = vendorAddressEmailAddress;
    }
    public String getVendorBusinessToBusinessUrlAddress() {
        return vendorBusinessToBusinessUrlAddress;
    }
    public void setVendorBusinessToBusinessUrlAddress(
            String vendorBusinessToBusinessUrlAddress) {
        this.vendorBusinessToBusinessUrlAddress = vendorBusinessToBusinessUrlAddress;
    }
    public String getVendorFaxNumber() {
        return vendorFaxNumber;
    }
    public void setVendorFaxNumber(String vendorFaxNumber) {
        this.vendorFaxNumber = vendorFaxNumber;
    }
    public String getVendorDefaultAddressIndicator() {
        return vendorDefaultAddressIndicator;
    }
    public void setVendorDefaultAddressIndicator(
            String vendorDefaultAddressIndicator) {
        this.vendorDefaultAddressIndicator = vendorDefaultAddressIndicator;
    }
    public String getPurchaseOrderTransmissionMethodCode() {
        return purchaseOrderTransmissionMethodCode;
    }
    public void setPurchaseOrderTransmissionMethodCode(
            String purchaseOrderTransmissionMethodCode) {
        this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
    }
    public String getActive() {
        return active;
    }
    public void setActive(String active) {
        this.active = active;
    }

    public String getVendorAddressGeneratedIdentifier() {
        return vendorAddressGeneratedIdentifier;
    }

    public void setVendorAddressGeneratedIdentifier(
            String vendorAddressGeneratedIdentifier) {
        this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
    }
    
    

}
