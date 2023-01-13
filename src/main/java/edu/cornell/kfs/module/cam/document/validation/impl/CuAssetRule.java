package edu.cornell.kfs.module.cam.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsKeyConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.document.validation.impl.AssetRule;

import edu.cornell.kfs.module.cam.CuCamsConstants;

public class CuAssetRule extends AssetRule {
    private static final Logger LOG = LogManager.getLogger();

    protected boolean validateTagNumber() {
        boolean valid = true;
        
        if (!assetService.isTagNumberCheckExclude(newAsset)) {

            Map<String, Object> fieldValues = new HashMap<>();
            if (ObjectUtils.isNotNull(newAsset.getCampusTagNumber())) {
                fieldValues.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER,
                        newAsset.getCampusTagNumber().toUpperCase(Locale.US));
                Collection<Asset> results = getBoService().findMatching(Asset.class, fieldValues);

                for (Asset asset : results) {
                    if (!asset.getCapitalAssetNumber().equals(newAsset.getCapitalAssetNumber())) {
                        // KFSMI-6149 - do not invalidate if the asset from the database is retired
                        if (StringUtils.isBlank(asset.getRetirementReasonCode())
                                || !parameterService.getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE)) {
                            putFieldError(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER,
                                    CamsKeyConstants.AssetLocationGlobal.ERROR_DUPLICATE_TAG_NUMBER_FOUND,
                                    new String[]{newAsset.getCampusTagNumber(), asset.getCapitalAssetNumber().toString(),
                                            newAsset.getCapitalAssetNumber().toString()});
                            valid = false;
                            LOG.info(
                                    "The asset tag number [{}] is a duplicate of asset number [{}]'s tag number",
                                    () -> newAsset.getCampusTagNumber().toUpperCase(Locale.US),
                                    asset::getCapitalAssetNumber
                            );
                        } else {
                            LOG.info(
                                    "Although the asset tag number [{}] is a duplicate of asset number [{}]'s tag "
                                    + "number, the old asset has already been retired",
                                    () -> newAsset.getCampusTagNumber().toUpperCase(Locale.US),
                                    asset::getCapitalAssetNumber
                            );
                        }

                    }
                }
            }
        }

        return valid;
    }

}
