package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;

@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = VendorAddress.class)
})
public class VendorAddressLite {

    @TaxDtoField
    private Integer vendorAddressGeneratedIdentifier;

    @TaxDtoField
    private Integer vendorHeaderGeneratedIdentifier;

    @TaxDtoField
    private Integer vendorDetailAssignedIdentifier;

    @TaxDtoField
    private String vendorAddressTypeCode;

    @TaxDtoField
    private String vendorLine1Address;

    @TaxDtoField
    private String vendorLine2Address;

    @TaxDtoField
    private String vendorCityName;

    @TaxDtoField
    private String vendorStateCode;

    @TaxDtoField
    private String vendorZipCode;

    @TaxDtoField
    private String vendorCountryCode;

    @TaxDtoField
    private String vendorAttentionName;

    @TaxDtoField
    private String vendorAddressInternationalProvinceName;

    @TaxDtoField
    private String vendorAddressEmailAddress;

    @TaxDtoField
    private boolean active;

    public Integer getVendorAddressGeneratedIdentifier() {
        return vendorAddressGeneratedIdentifier;
    }

    public void setVendorAddressGeneratedIdentifier(final Integer vendorAddressGeneratedIdentifier) {
        this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(final Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(final Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public String getVendorAddressTypeCode() {
        return vendorAddressTypeCode;
    }

    public void setVendorAddressTypeCode(final String vendorAddressTypeCode) {
        this.vendorAddressTypeCode = vendorAddressTypeCode;
    }

    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    public void setVendorLine1Address(final String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    public void setVendorLine2Address(final String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    public String getVendorCityName() {
        return vendorCityName;
    }

    public void setVendorCityName(final String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    public String getVendorStateCode() {
        return vendorStateCode;
    }

    public void setVendorStateCode(final String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    public String getVendorZipCode() {
        return vendorZipCode;
    }

    public void setVendorZipCode(final String vendorZipCode) {
        this.vendorZipCode = vendorZipCode;
    }

    public String getVendorCountryCode() {
        return vendorCountryCode;
    }

    public void setVendorCountryCode(final String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
    }

    public String getVendorAttentionName() {
        return vendorAttentionName;
    }

    public void setVendorAttentionName(final String vendorAttentionName) {
        this.vendorAttentionName = vendorAttentionName;
    }

    public String getVendorAddressInternationalProvinceName() {
        return vendorAddressInternationalProvinceName;
    }

    public void setVendorAddressInternationalProvinceName(final String vendorAddressInternationalProvinceName) {
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
    }

    public String getVendorAddressEmailAddress() {
        return vendorAddressEmailAddress;
    }

    public void setVendorAddressEmailAddress(final String vendorAddressEmailAddress) {
        this.vendorAddressEmailAddress = vendorAddressEmailAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

}
