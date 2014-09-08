package edu.cornell.kfs.module.cam.fixture;

import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;

public enum AssetGlobalDetailFixture {
	
	ONE("NT00002147");
	
	public final String campusTagNumber;
	
	private AssetGlobalDetailFixture(String campusTagNumber) {
		this.campusTagNumber = campusTagNumber;
	}
	
	public AssetGlobalDetail createAssetGlobalDetail() {
		AssetGlobalDetail assetGlobalDetail = new AssetGlobalDetail();
		
		assetGlobalDetail.setCampusTagNumber(campusTagNumber);
		
		return assetGlobalDetail;
	}

}
