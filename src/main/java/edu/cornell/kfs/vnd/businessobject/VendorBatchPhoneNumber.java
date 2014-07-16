package edu.cornell.kfs.vnd.businessobject;

/**
 *  non-persistable to hold vendor phone number converted from input data file
 **/
public class VendorBatchPhoneNumber {
    private String vendorPhoneGeneratedIdentifier;
    private String vendorPhoneTypeCode;
    private String vendorPhoneNumber;
    private String vendorPhoneExtensionNumber;
    private String active;
    
    public VendorBatchPhoneNumber(String[] phoneNumber) {
    	vendorPhoneGeneratedIdentifier = phoneNumber[0];
    	vendorPhoneTypeCode = phoneNumber[1];
    	vendorPhoneNumber = phoneNumber[2];
    	vendorPhoneExtensionNumber = phoneNumber[3];
    	active = phoneNumber[4];
    }
    
	public String getVendorPhoneGeneratedIdentifier() {
		return vendorPhoneGeneratedIdentifier;
	}
	public void setVendorPhoneGeneratedIdentifier(
			String vendorPhoneGeneratedIdentifier) {
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
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}

}
