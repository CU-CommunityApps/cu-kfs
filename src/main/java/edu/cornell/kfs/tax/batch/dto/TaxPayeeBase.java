package edu.cornell.kfs.tax.batch.dto;

public abstract class TaxPayeeBase {

    private String payeeId;
    private Integer vendorHeaderId;
    private Integer vendorDetailId;
    private String vendorName;
    private String parentVendorName;
    private String vendorLastName;
    private String vendorFirstName;
    private String vendorTaxNumber;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private Boolean vendorForeignIndicator;
    private String vendorEmailAddress;
    private String vendorChapter4StatusCode;
    private String vendorGIIN;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorForeignLine1Address;
    private String vendorForeignLine2Address;
    private String vendorForeignCityName;
    private String vendorForeignZipCode;
    private String vendorForeignProvinceName;
    private String vendorForeignCountryCode;

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(final String payeeId) {
        this.payeeId = payeeId;
    }

    public Integer getVendorHeaderId() {
        return vendorHeaderId;
    }

    public void setVendorHeaderId(final Integer vendorHeaderId) {
        this.vendorHeaderId = vendorHeaderId;
    }

    public Integer getVendorDetailId() {
        return vendorDetailId;
    }

    public void setVendorDetailId(final Integer vendorDetailId) {
        this.vendorDetailId = vendorDetailId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public String getParentVendorName() {
        return parentVendorName;
    }

    public void setParentVendorName(final String parentVendorName) {
        this.parentVendorName = parentVendorName;
    }

    public String getVendorLastName() {
        return vendorLastName;
    }

    public void setVendorLastName(final String vendorLastName) {
        this.vendorLastName = vendorLastName;
    }

    public String getVendorFirstName() {
        return vendorFirstName;
    }

    public void setVendorFirstName(final String vendorFirstName) {
        this.vendorFirstName = vendorFirstName;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(final String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(final String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(final String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getVendorOwnershipCategoryCode() {
        return vendorOwnershipCategoryCode;
    }

    public void setVendorOwnershipCategoryCode(final String vendorOwnershipCategoryCode) {
        this.vendorOwnershipCategoryCode = vendorOwnershipCategoryCode;
    }

    public Boolean getVendorForeignIndicator() {
        return vendorForeignIndicator;
    }

    public void setVendorForeignIndicator(final Boolean vendorForeignIndicator) {
        this.vendorForeignIndicator = vendorForeignIndicator;
    }

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(final String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorChapter4StatusCode() {
        return vendorChapter4StatusCode;
    }

    public void setVendorChapter4StatusCode(final String vendorChapter4StatusCode) {
        this.vendorChapter4StatusCode = vendorChapter4StatusCode;
    }

    public String getVendorGIIN() {
        return vendorGIIN;
    }

    public void setVendorGIIN(final String vendorGIIN) {
        this.vendorGIIN = vendorGIIN;
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

    public String getVendorForeignLine1Address() {
        return vendorForeignLine1Address;
    }

    public void setVendorForeignLine1Address(final String vendorForeignLine1Address) {
        this.vendorForeignLine1Address = vendorForeignLine1Address;
    }

    public String getVendorForeignLine2Address() {
        return vendorForeignLine2Address;
    }

    public void setVendorForeignLine2Address(final String vendorForeignLine2Address) {
        this.vendorForeignLine2Address = vendorForeignLine2Address;
    }

    public String getVendorForeignCityName() {
        return vendorForeignCityName;
    }

    public void setVendorForeignCityName(final String vendorForeignCityName) {
        this.vendorForeignCityName = vendorForeignCityName;
    }

    public String getVendorForeignZipCode() {
        return vendorForeignZipCode;
    }

    public void setVendorForeignZipCode(final String vendorForeignZipCode) {
        this.vendorForeignZipCode = vendorForeignZipCode;
    }

    public String getVendorForeignProvinceName() {
        return vendorForeignProvinceName;
    }

    public void setVendorForeignProvinceName(final String vendorForeignProvinceName) {
        this.vendorForeignProvinceName = vendorForeignProvinceName;
    }

    public String getVendorForeignCountryCode() {
        return vendorForeignCountryCode;
    }

    public void setVendorForeignCountryCode(final String vendorForeignCountryCode) {
        this.vendorForeignCountryCode = vendorForeignCountryCode;
    }

}
