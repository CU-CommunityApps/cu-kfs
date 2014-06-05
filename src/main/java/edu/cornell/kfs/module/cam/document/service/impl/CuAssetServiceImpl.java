package edu.cornell.kfs.module.cam.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.document.service.impl.AssetServiceImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.module.cam.CuCamsConstants;

public class CuAssetServiceImpl extends AssetServiceImpl {

    public List<Asset> findActiveAssetsMatchingTagNumber(String campusTagNumber) {
        List<Asset> activeMatches = new ArrayList<Asset>();
        // find all assets matching this tag number
        Map<String, String> params = new HashMap<String, String>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, campusTagNumber);
        Collection<Asset> tagMatches = SpringContext.getBean(BusinessObjectService.class).findMatching(Asset.class, params);
        if (tagMatches != null && !tagMatches.isEmpty()) {
            for (Asset asset : tagMatches) {
                // if found matching, check if status is not retired
                if (!isAssetRetired(asset) || !SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE))  {
                    activeMatches.add(asset);
                }
            }
        }
        return activeMatches;
    }

}
