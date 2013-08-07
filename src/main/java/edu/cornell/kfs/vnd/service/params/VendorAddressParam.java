package edu.cornell.kfs.vnd.service.params;

import java.io.Serializable;

public class VendorAddressParam implements Serializable {

    private Integer vendorAddressGeneratedIdentifier;
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
    private boolean vendorDefaultAddressIndicator;
    private String purchaseOrderTransmissionMethodCode;
    private boolean active;
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
	public boolean isVendorDefaultAddressIndicator() {
		return vendorDefaultAddressIndicator;
	}
	public void setVendorDefaultAddressIndicator(
			boolean vendorDefaultAddressIndicator) {
		this.vendorDefaultAddressIndicator = vendorDefaultAddressIndicator;
	}
	public String getPurchaseOrderTransmissionMethodCode() {
		return purchaseOrderTransmissionMethodCode;
	}
	public void setPurchaseOrderTransmissionMethodCode(
			String purchaseOrderTransmissionMethodCode) {
		this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
	}
	public Integer getVendorAddressGeneratedIdentifier() {
		return vendorAddressGeneratedIdentifier;
	}
	public void setVendorAddressGeneratedIdentifier(
			Integer vendorAddressGeneratedIdentifier) {
		this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getVendorAddressEmailAddress()).append("~")
		  .append(getVendorAddressInternationalProvinceName()).append("~")
		  .append(getVendorAddressTypeCode()).append("~")
		  .append(getVendorAttentionName()).append("~")
		  .append(getVendorBusinessToBusinessUrlAddress()).append("~")
		  .append(getVendorCityName()).append("~")
		  .append(getVendorCountryCode()).append("~")
		  .append(getVendorFaxNumber()).append("~")
		  .append(getVendorLine1Address()).append("~")
		  .append(getVendorLine2Address()).append("~")
		  .append(getVendorStateCode()).append("~")
		  .append(getVendorZipCode()).append("~")
		  .append(isActive()).append("~")
		  .append(getVendorAddressGeneratedIdentifier());
		return sb.toString();
	}

}
