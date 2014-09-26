package edu.cornell.kfs.module.cab.fixture;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;

public enum CapitalAssetInformationFixture {

	ONE();
	
	private CapitalAssetInformationFixture() 
	{
		
	}
	
	public CapitalAssetInformation createCapitalAssetInformation() {
		CapitalAssetInformation capitalAssetInformation = new CapitalAssetInformation();
		
		return capitalAssetInformation;
	}
}
