package edu.cornell.kfs.module.cam.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetLocation;
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
    
    @Override
    protected void setupAssetLocationOffCampus(final AssetGlobalDetail assetGlobalDetail, final Asset asset) {
        // We are not checking if it already exists since on a new asset it can't
        final AssetLocation offCampusAssetLocation = new AssetLocation();
        offCampusAssetLocation.setCapitalAssetNumber(asset.getCapitalAssetNumber());
        offCampusAssetLocation.setAssetLocationTypeCode(CamsConstants.AssetLocationTypeCode.OFF_CAMPUS);
        asset.getAssetLocations().add(offCampusAssetLocation);

        // Set the location fields either way
        offCampusAssetLocation.setAssetLocationContactName(assetGlobalDetail.getOffCampusName());
        offCampusAssetLocation.setAssetLocationContactIdentifier(assetGlobalDetail.getRepresentativeUniversalIdentifier());
        processAssetLocationInstitutionName(assetGlobalDetail, offCampusAssetLocation);
        offCampusAssetLocation.setAssetLocationStreetAddress(assetGlobalDetail.getOffCampusAddress());
        offCampusAssetLocation.setAssetLocationCityName(assetGlobalDetail.getOffCampusCityName());
        offCampusAssetLocation.setAssetLocationStateCode(assetGlobalDetail.getOffCampusStateCode());
        offCampusAssetLocation.setAssetLocationCountryCode(assetGlobalDetail.getOffCampusCountryCode());
        offCampusAssetLocation.setAssetLocationZipCode(assetGlobalDetail.getOffCampusZipCode());

        // There is no phone number field on Asset Global... odd...
        offCampusAssetLocation.setAssetLocationPhoneNumber(null);
    }
    
    /*
     * CU-Customization
     * The asset representative might mot be a Cornell person, so we need to handle this situation
     * KualiCo jira FINP-11058 should address this bug
     */
    private void processAssetLocationInstitutionName(final AssetGlobalDetail assetGlobalDetail,
            final AssetLocation offCampusAssetLocation) {
        if (ObjectUtils.isNotNull(assetGlobalDetail.getAssetRepresentative())) {
            offCampusAssetLocation.setAssetLocationInstitutionName(
                    assetGlobalDetail.getAssetRepresentative().getPrimaryDepartmentCode());
        } else {
            LOG.info("processAssetLocationInstitutionName, the asset representative is not sent, unable to set asset location institution name");
        }
    }

}   
