package edu.cornell.kfs.module.cam.fixture;

import edu.cornell.kfs.fp.businessobject.CapitalAssetInformationDetailExtendedAttribute;

public enum CapitalAssetInformationDetailExtensionFixture {

	ONE("Ithaca", "US", "NY", "120 Maple Avenue", "14850");
	
	private String assetLocationCityName;
	private String assetLocationCountryCode;
	private String assetLocationStateCode;
	private String assetLocationStreetAddress;
	private String assetLocationZipCode;
	
	private CapitalAssetInformationDetailExtensionFixture (String assetLocationCityName, String assetLocationCountryCode, String assetLocationStateCode, String assetLocationStreetAddress, String assetLocationZipCode) {
		this.assetLocationCityName = assetLocationCityName;
		this.assetLocationCountryCode = assetLocationCountryCode;
		this.assetLocationStateCode = assetLocationStateCode;
		this.assetLocationStreetAddress = assetLocationStreetAddress;
		this.assetLocationZipCode = assetLocationZipCode;
	}
	
	public CapitalAssetInformationDetailExtendedAttribute createExtendedAttribute() {
		CapitalAssetInformationDetailExtendedAttribute capitalAssetInformationDetailExtendedAttribute = new CapitalAssetInformationDetailExtendedAttribute();
		
		return capitalAssetInformationDetailExtendedAttribute;
	}
}
