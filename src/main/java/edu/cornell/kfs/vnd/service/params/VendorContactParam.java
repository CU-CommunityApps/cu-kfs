package edu.cornell.kfs.vnd.service.params;

import java.io.Serializable;

public class VendorContactParam implements Serializable {

    private Integer vendorContactGeneratedIdentifier;
    private String vendorContactTypeCode;
    private String vendorContactName;
    private String vendorContactEmailAddress;
    private String vendorContactCommentText;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorCountryCode;
    private String vendorAttentionName;
    private String vendorAddressInternationalProvinceName;
    private boolean active;
	public String getVendorContactTypeCode() {
		return vendorContactTypeCode;
	}
	public void setVendorContactTypeCode(String vendorContactTypeCode) {
		this.vendorContactTypeCode = vendorContactTypeCode;
	}
	public String getVendorContactName() {
		return vendorContactName;
	}
	public void setVendorContactName(String vendorContactName) {
		this.vendorContactName = vendorContactName;
	}
	public String getVendorContactEmailAddress() {
		return vendorContactEmailAddress;
	}
	public void setVendorContactEmailAddress(String vendorContactEmailAddress) {
		this.vendorContactEmailAddress = vendorContactEmailAddress;
	}
	public String getVendorContactCommentText() {
		return vendorContactCommentText;
	}
	public void setVendorContactCommentText(String vendorContactCommentText) {
		this.vendorContactCommentText = vendorContactCommentText;
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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Integer getVendorContactGeneratedIdentifier() {
		return vendorContactGeneratedIdentifier;
	}
	public void setVendorContactGeneratedIdentifier(
			Integer vendorContactGeneratedIdentifier) {
		this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
	}

}
