package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

public class SprintaxInfo1042S {

    private String rowId;
    private String taxId;
    private String payeeId;
    private Integer vendorHeaderId;
    private Integer vendorDetailId;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String paymentAddressLine1;
    private String vendorNameForOutput;
    private String parentVendorNameForOutput;
    private String vendorLastName;
    private String vendorFirstName;
    private String vendorEmailAddress;
    private String vendorGIIN;
    private String vendorUSAddressLine1;
    private String vendorUSAddressLine2;
    private String vendorUSCityName;
    private String vendorUSStateCode;
    private String vendorUSZipCode;
    private String vendorForeignAddressLine1;
    private String vendorForeignAddressLine2;
    private String vendorForeignCityName;
    private String vendorForeignZipCode;
    private String vendorForeignProvinceName;
    private String vendorForeignCountryCode;
    private String formattedSSNValue;
    private String formattedITINValue;
    private String chapter3StatusCode;
    private String chapter4StatusCode;
    private String chapter4ExemptionCode;
    private String payerEIN;
    private String stateCode;
    private Date endDate;
    private boolean biographicRowWritten;

    private SprintaxPayment1042S currentPayment;

    public String getRowId() {
        return rowId;
    }

    public void setRowId(final String rowId) {
        this.rowId = rowId;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(final String taxId) {
        this.taxId = taxId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(final String payeeId) {
        this.payeeId = payeeId;
    }

    public Integer getVendorHeaderId() {
        return vendorHeaderId;
    }

    public void setVendorHeaderId(Integer vendorHeaderId) {
        this.vendorHeaderId = vendorHeaderId;
    }

    public Integer getVendorDetailId() {
        return vendorDetailId;
    }

    public void setVendorDetailId(Integer vendorDetailId) {
        this.vendorDetailId = vendorDetailId;
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

    public String getPaymentAddressLine1() {
        return paymentAddressLine1;
    }

    public void setPaymentAddressLine1(final String paymentAddressLine1) {
        this.paymentAddressLine1 = paymentAddressLine1;
    }

    public String getVendorNameForOutput() {
        return vendorNameForOutput;
    }

    public void setVendorNameForOutput(final String vendorNameForOutput) {
        this.vendorNameForOutput = vendorNameForOutput;
    }

    public String getParentVendorNameForOutput() {
        return parentVendorNameForOutput;
    }

    public void setParentVendorNameForOutput(final String parentVendorNameForOutput) {
        this.parentVendorNameForOutput = parentVendorNameForOutput;
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

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(final String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorGIIN() {
        return vendorGIIN;
    }

    public void setVendorGIIN(final String vendorGIIN) {
        this.vendorGIIN = vendorGIIN;
    }

    public String getVendorUSAddressLine1() {
        return vendorUSAddressLine1;
    }

    public void setVendorUSAddressLine1(final String vendorUSAddressLine1) {
        this.vendorUSAddressLine1 = vendorUSAddressLine1;
    }

    public String getVendorUSAddressLine2() {
        return vendorUSAddressLine2;
    }

    public void setVendorUSAddressLine2(final String vendorUSAddressLine2) {
        this.vendorUSAddressLine2 = vendorUSAddressLine2;
    }

    public String getVendorUSCityName() {
        return vendorUSCityName;
    }

    public void setVendorUSCityName(final String vendorUSCityName) {
        this.vendorUSCityName = vendorUSCityName;
    }

    public String getVendorUSStateCode() {
        return vendorUSStateCode;
    }

    public void setVendorUSStateCode(final String vendorUSStateCode) {
        this.vendorUSStateCode = vendorUSStateCode;
    }

    public String getVendorUSZipCode() {
        return vendorUSZipCode;
    }

    public void setVendorUSZipCode(final String vendorUSZipCode) {
        this.vendorUSZipCode = vendorUSZipCode;
    }

    public String getVendorForeignAddressLine1() {
        return vendorForeignAddressLine1;
    }

    public void setVendorForeignAddressLine1(final String vendorForeignAddressLine1) {
        this.vendorForeignAddressLine1 = vendorForeignAddressLine1;
    }

    public String getVendorForeignAddressLine2() {
        return vendorForeignAddressLine2;
    }

    public void setVendorForeignAddressLine2(final String vendorForeignAddressLine2) {
        this.vendorForeignAddressLine2 = vendorForeignAddressLine2;
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

    public String getFormattedSSNValue() {
        return formattedSSNValue;
    }

    public void setFormattedSSNValue(final String formattedSSNValue) {
        this.formattedSSNValue = formattedSSNValue;
    }

    public String getFormattedITINValue() {
        return formattedITINValue;
    }

    public void setFormattedITINValue(final String formattedITINValue) {
        this.formattedITINValue = formattedITINValue;
    }

    public String getChapter3StatusCode() {
        return chapter3StatusCode;
    }

    public void setChapter3StatusCode(final String chapter3StatusCode) {
        this.chapter3StatusCode = chapter3StatusCode;
    }

    public String getChapter4StatusCode() {
        return chapter4StatusCode;
    }

    public void setChapter4StatusCode(final String chapter4StatusCode) {
        this.chapter4StatusCode = chapter4StatusCode;
    }

    public String getChapter4ExemptionCode() {
        return chapter4ExemptionCode;
    }

    public void setChapter4ExemptionCode(final String chapter4ExemptionCode) {
        this.chapter4ExemptionCode = chapter4ExemptionCode;
    }

    public String getPayerEIN() {
        return payerEIN;
    }

    public void setPayerEIN(final String payerEIN) {
        this.payerEIN = payerEIN;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(final String stateCode) {
        this.stateCode = stateCode;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public boolean isBiographicRowWritten() {
        return biographicRowWritten;
    }

    public void setBiographicRowWritten(final boolean biographicRowWritten) {
        this.biographicRowWritten = biographicRowWritten;
    }

    public SprintaxPayment1042S getCurrentPayment() {
        return currentPayment;
    }

    public void setCurrentPayment(final SprintaxPayment1042S currentPayment) {
        this.currentPayment = currentPayment;
    }

}
