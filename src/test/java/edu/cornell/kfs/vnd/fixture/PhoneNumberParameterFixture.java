package edu.cornell.kfs.vnd.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;

import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;

@ConfigureContext
public enum PhoneNumberParameterFixture {
	
	ONE(1, "FX", "2136579999", "x783", true),
	TWO(2, "MB", "2223334478", "", true),
	THREE(3, "TF", "8009874321", "x999", true);

	public final Integer vendorPhoneGeneratedIdentifier;
	public final String vendorPhoneTypeCode;
	public final String vendorPhoneNumber;
	public final String vendorPhoneExtensionNumber;
	public boolean active;

	private PhoneNumberParameterFixture(Integer vendorPhoneGeneratedIdentifier,
			String vendorPhoneTypeCode,
			String vendorPhoneNumber,
			String vendorPhoneExtensionNumber,
			boolean active) {

		this.vendorPhoneGeneratedIdentifier = vendorPhoneGeneratedIdentifier;
		this.vendorPhoneTypeCode = vendorPhoneTypeCode;
		this.vendorPhoneNumber = vendorPhoneNumber;
		this.vendorPhoneExtensionNumber = vendorPhoneExtensionNumber;
		this.active = active;
	}
	
	public VendorPhoneNumberParam createPhoneNumberParam() {
		VendorPhoneNumberParam phoneNumberParam = new VendorPhoneNumberParam();
		phoneNumberParam.setVendorPhoneGeneratedIdentifier(this.vendorPhoneGeneratedIdentifier);
		phoneNumberParam.setVendorPhoneNumber(this.vendorPhoneNumber);
		phoneNumberParam.setVendorPhoneTypeCode(this.vendorPhoneTypeCode);
		phoneNumberParam.setVendorPhoneExtensionNumber(this.vendorPhoneExtensionNumber);
		
		return phoneNumberParam;
	}
	
	public List<VendorPhoneNumberParam> getAllFixtures() {
		ArrayList<VendorPhoneNumberParam> allFixtures = new ArrayList<VendorPhoneNumberParam>();
		
		allFixtures.add(ONE.createPhoneNumberParam());
		allFixtures.add(TWO.createPhoneNumberParam());
		allFixtures.add(THREE.createPhoneNumberParam());
		
		return allFixtures;		
	}
	
}
