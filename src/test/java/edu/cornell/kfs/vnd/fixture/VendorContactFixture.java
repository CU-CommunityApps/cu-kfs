package edu.cornell.kfs.vnd.fixture;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.vnd.service.params.VendorContactParam;

public enum VendorContactFixture {

	ONE(1, "AR", "Jane Doe", "jane@nosuchvendor.com", "a test vendor contact",
			"P.O. Box 654", "Nowhereville", "SD", "99900", "US", "John Dough", true),
	TWO(2, "PT", "Sandy Claus", "sandy@north_pole_workshop.com", "a test vendor contact",
					"3 Christmas Lane", "Nosuchplace", "ND", "99901", "US", "Kris Kringle", true),	
	THREE(3, "IN", "John Nobody", "john@whatisinsurance.com", "a test vendor contact",
			"333 State St", "Imaginetown", "RI", "99902", "US", "Dewey, Cheatem, and Howe", true),;	

	
    public final Integer vendorContactGeneratedIdentifier;
    public final String vendorContactTypeCode;
    public final String vendorContactName;
    public final String vendorContactEmailAddress;
    public final String vendorContactCommentText;
    public final String vendorLine1Address;
    public final String vendorCityName;
    public final String vendorStateCode;
    public final String vendorZipCode;
    public final String vendorCountryCode;
    public final String vendorAttentionName;
    public final boolean active;

    private VendorContactFixture (Integer vendorContactGeneratedIdentifier,
    		String vendorContactTypeCode, String vendorContactName, String vendorContactEmailAddress,
    		String vendorContactCommentText, String vendorLine1Address, String vendorCityName,
    		String vendorStateCode, String vendorZipCode, String vendorCountryCode, 
    		String vendorAttentionName, boolean active) {
    	
    	this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
    	this.vendorContactTypeCode = vendorContactTypeCode;
    	this.vendorContactName = vendorContactName;
    	this.vendorContactEmailAddress = vendorContactEmailAddress;
    	this.vendorContactCommentText = vendorContactCommentText;
    	this.vendorLine1Address = vendorLine1Address;
    	this.vendorCityName = vendorCityName;
    	this.vendorStateCode = vendorStateCode;
    	this.vendorZipCode = vendorZipCode;
    	this.vendorCountryCode = vendorCountryCode;
    	this.vendorAttentionName = vendorAttentionName;
    	this.active = active;
    	
    }
    
    public VendorContactParam createVendorContactParam() {
    	VendorContactParam vendorContactParam = new VendorContactParam();
    	
    	vendorContactParam.setActive(active);
    	vendorContactParam.setVendorAttentionName(vendorAttentionName);
    	vendorContactParam.setVendorCityName(vendorCityName);
    	vendorContactParam.setVendorContactCommentText(vendorContactCommentText);
    	vendorContactParam.setVendorContactEmailAddress(vendorContactEmailAddress);
    	vendorContactParam.setVendorContactGeneratedIdentifier(vendorContactGeneratedIdentifier);
    	vendorContactParam.setVendorContactName(vendorContactName);
    	vendorContactParam.setVendorContactTypeCode(vendorContactTypeCode);
    	vendorContactParam.setVendorCountryCode(vendorCountryCode);
    	vendorContactParam.setVendorLine1Address(vendorLine1Address);
    	vendorContactParam.setVendorZipCode(vendorZipCode);
    	vendorContactParam.setVendorStateCode(vendorStateCode);
    	
    	return vendorContactParam;
    }
    
    public List<VendorContactParam> getAllFixtures() {
    	ArrayList<VendorContactParam> allFixtures = new ArrayList<VendorContactParam>();
    	
    	allFixtures.add(ONE.createVendorContactParam());
    	allFixtures.add(TWO.createVendorContactParam());
    	allFixtures.add(THREE.createVendorContactParam());

    	
    	return allFixtures;
    }
}
