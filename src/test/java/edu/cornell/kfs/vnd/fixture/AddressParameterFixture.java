package edu.cornell.kfs.vnd.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;

import edu.cornell.kfs.vnd.service.params.VendorAddressParam;
import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;

@ConfigureContext
public enum AddressParameterFixture {

	ONE(1, "PO", "332 Someplace Street", "Somewhereville", "ND", "14850", "US", true, true);
	
	public final Integer identifier;
	public final String vendorAddressTypeCode;
	public final String vendorLine1Address;
	public final String vendorCityName;
	public final String vendorStateCode;
	public final String vendorCountryCode;
	public final String vendorZipCode;
	public boolean vendorDefaultAddressIndicator;
	public boolean active;
	
	private AddressParameterFixture(Integer identifier, String vendorAddressTypeCode, String vendorLine1Address,
			String vendorCityName, String vendorStateCode, String vendorZipCode, String vendorCountryCode, boolean vendorDefaultAddressIndicator, boolean active) {
		this.identifier = identifier;
		this.vendorAddressTypeCode = vendorAddressTypeCode;
		this.vendorLine1Address = vendorLine1Address;
		this.vendorCityName = vendorCityName;
		this.vendorCountryCode = vendorCountryCode;
		this.vendorStateCode = vendorStateCode;
		this.vendorZipCode = vendorZipCode;
		this.vendorDefaultAddressIndicator = vendorDefaultAddressIndicator;
	}
	
	
	public VendorAddressParam createVendorAddressParam() {
		VendorAddressParam vendorAddressParam = new VendorAddressParam();
		
		vendorAddressParam.setActive(active);
		vendorAddressParam.setVendorAddressTypeCode(vendorAddressTypeCode);
		vendorAddressParam.setVendorLine1Address(vendorLine1Address);
		vendorAddressParam.setVendorCityName(vendorCityName);
		vendorAddressParam.setVendorStateCode(vendorStateCode);
		vendorAddressParam.setVendorCountryCode(vendorCountryCode);
		vendorAddressParam.setVendorDefaultAddressIndicator(vendorDefaultAddressIndicator);
		vendorAddressParam.setVendorAddressGeneratedIdentifier(identifier);
		vendorAddressParam.setVendorZipCode(vendorZipCode);
		return vendorAddressParam;
	}
	
	public List<VendorAddressParam> getAllFixtures() {
		ArrayList<VendorAddressParam> allFixtures = new ArrayList<VendorAddressParam>();
		
		allFixtures.add(ONE.createVendorAddressParam());
		
		return allFixtures;		
	}
	
}
