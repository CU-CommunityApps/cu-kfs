package edu.cornell.kfs.module.cam.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetPaymentDetail;
import org.kuali.kfs.sys.ConfigureContext;

@ConfigureContext
public enum AssetGlobalFixture {

	ONE();
	
	public final List<AssetPaymentDetail> assetPaymentDetails;
	
	private AssetGlobalFixture() {
		assetPaymentDetails = new ArrayList<AssetPaymentDetail>();
	}

	public AssetGlobal createAssetGlobal() {
		AssetGlobal assetGlobal = new AssetGlobal();
		
		assetGlobal.setAssetPaymentDetails(assetPaymentDetails);
		
		return assetGlobal;
	}
}
