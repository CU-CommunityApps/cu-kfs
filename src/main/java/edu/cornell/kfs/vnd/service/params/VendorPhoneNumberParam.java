package edu.cornell.kfs.vnd.service.params;

import java.io.Serializable;

public class VendorPhoneNumberParam implements Serializable {
    private Integer vendorPhoneGeneratedIdentifier;
    private String vendorPhoneTypeCode;
    private String vendorPhoneNumber;
    private String vendorPhoneExtensionNumber;
    private boolean active;
	public Integer getVendorPhoneGeneratedIdentifier() {
		return vendorPhoneGeneratedIdentifier;
	}
	public void setVendorPhoneGeneratedIdentifier(
			Integer vendorPhoneGeneratedIdentifier) {
		this.vendorPhoneGeneratedIdentifier = vendorPhoneGeneratedIdentifier;
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
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getVendorPhoneTypeCode()).append("~")
		  .append(getVendorPhoneNumber()).append("~")
		  .append(getVendorPhoneExtensionNumber()).append("~")
		  .append(isActive()).append("~")
		  .append(getVendorPhoneGeneratedIdentifier());
		return sb.toString();
	}
}
