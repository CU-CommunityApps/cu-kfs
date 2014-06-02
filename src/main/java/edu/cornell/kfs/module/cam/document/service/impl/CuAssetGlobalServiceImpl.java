package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.document.service.impl.AssetGlobalServiceImpl;
import org.kuali.rice.krad.bo.PersistableBusinessObject;

import edu.cornell.kfs.module.cam.businessobject.AssetExtension;

public class CuAssetGlobalServiceImpl extends AssetGlobalServiceImpl {

    // KFSUPGRADE-535
	// if we need to implement service rate ind, then it can be populated from detail to assetext too.
	@Override
	protected Asset setupAsset(AssetGlobal assetGlobal,
			AssetGlobalDetail assetGlobalDetail, boolean separate) {
		Asset asset =  super.setupAsset(assetGlobal, assetGlobalDetail, separate);
        AssetExtension ae = (AssetExtension) asset.getExtension();
        ae.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        return asset;

	}

    @Override
    public List<PersistableBusinessObject> getSeparateAssets(AssetGlobal assetGlobal) {
        List<PersistableBusinessObject> assets = super.getSeparateAssets(assetGlobal);
        for (PersistableBusinessObject pbo : assets) {
            for (AssetPayment payment : ((Asset)pbo).getAssetPayments()) {
                if (StringUtils.equals(payment.getDocumentNumber(), assetGlobal.getDocumentNumber())) {
                    payment.setPrimaryDepreciationBaseAmount(payment.getAccountChargeAmount());
                    payment.setAccumulatedPrimaryDepreciationAmount(payment.getAccountChargeAmount());
                }                
            }           
        }
        return assets;
    }	
}
