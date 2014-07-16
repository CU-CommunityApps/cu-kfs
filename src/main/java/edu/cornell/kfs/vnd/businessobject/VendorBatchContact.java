package edu.cornell.kfs.vnd.businessobject;

/**
 *  non-persistable to hold vendor contact converted from input data file
 **/
public class VendorBatchContact {

    private String vendorContactGeneratedIdentifier;
    private String vendorContactTypeCode;
    private String vendorContactName;
    private String vendorContactEmailAddress;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorCountryCode;
    private String vendorAttentionName;
    private String vendorAddressInternationalProvinceName;
    private String vendorContactCommentText;
    private String active;
    
    public VendorBatchContact(String[] contacts) {
    	vendorContactGeneratedIdentifier = contacts[0];
    	vendorContactTypeCode = contacts[1];
    	vendorContactName = contacts[2];
    	vendorContactEmailAddress = contacts[3];
    	vendorLine1Address = contacts[4];
    	vendorLine2Address = contacts[5];
    	vendorCityName = contacts[6];
    	vendorStateCode = contacts[7];
    	vendorZipCode = contacts[8];
    	vendorCountryCode = contacts[9];
    	vendorAttentionName = contacts[10];
    	vendorAddressInternationalProvinceName = contacts[11];
    	vendorContactCommentText = contacts[12];
    	active = contacts[13];
    }

	public String getVendorContactGeneratedIdentifier() {
		return vendorContactGeneratedIdentifier;
	}
	public void setVendorContactGeneratedIdentifier(
			String vendorContactGeneratedIdentifier) {
		this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
	}
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
	public String getVendorContactCommentText() {
		return vendorContactCommentText;
	}
	public void setVendorContactCommentText(String vendorContactCommentText) {
		this.vendorContactCommentText = vendorContactCommentText;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}

}
