package edu.cornell.kfs.module.cam.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.document.service.impl.AssetGlobalServiceImpl;

import edu.cornell.kfs.module.cam.businessobject.AssetExtension;

public class CuAssetGlobalServiceImpl extends AssetGlobalServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    // KFSUPGRADE-535
    // if we need to implement service rate ind, then it can be populated from detail to assetext too.
    @Override
    protected Asset setupAsset(final AssetGlobal assetGlobal, final AssetGlobalDetail assetGlobalDetail, final boolean separate) {
        final Asset asset = super.setupAsset(assetGlobal, assetGlobalDetail, separate);
        final AssetExtension ae = (AssetExtension) asset.getExtension();
        ae.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        return asset;

    }

}
