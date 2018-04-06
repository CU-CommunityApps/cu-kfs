package edu.cornell.kfs.module.cam.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetLocation;
import org.kuali.kfs.module.cam.businessobject.lookup.AssetLookupableHelperServiceImpl;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.module.cam.CuCamsPropertyConstants;

public class CuAssetLookupableHelperServiceImpl extends AssetLookupableHelperServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAssetLookupableHelperServiceImpl.class);

    @Override
    protected List<? extends BusinessObject> getSearchResultsHelper(Map<String, String> fieldValues, boolean unbounded) {
        // perform the lookup on the asset representative first
        String principalName = fieldValues.get(CamsPropertyConstants.Asset.REP_USER_AUTH_ID);
        if (StringUtils.isNotBlank(principalName)) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(principalName);

            if (principal == null) {
                return Collections.EMPTY_LIST;
            }
            // place the universal ID into the fieldValues map and remove the dummy attribute
            fieldValues.put(CamsPropertyConstants.Asset.REPRESENTATIVE_UNIVERSAL_IDENTIFIER, principal.getPrincipalId());
            fieldValues.remove(CamsPropertyConstants.Asset.REP_USER_AUTH_ID);
        }

        List<? extends BusinessObject> results;
        
        if (StringUtils.isNotBlank(fieldValues.get(CuCamsPropertyConstants.Asset.ASSET_LOCATION_TYPE_CODE))) {
            unbounded = true;
            results = excludeBlankOffCampusLocations(super.getSearchResultsHelper(fieldValues, unbounded));
        } else {
            results = super.getSearchResultsHelper(fieldValues, unbounded);
        }
        return results;
    }

    protected List<? extends BusinessObject> excludeBlankOffCampusLocations(List<? extends BusinessObject> results) {
        List<Asset> resultsModified = new ArrayList<Asset>();
        int count = 0;
        LOG.info("Asset count: " + results.size());
    	for (BusinessObject boAsset : results) {
    		Asset asset;
    		if (boAsset instanceof Asset) {
    			count++;
    			boolean remove = false;
    			asset = (Asset) boAsset;
    			List<AssetLocation> locs = asset.getAssetLocations();
    			if (locs.isEmpty()) {
    				resultsModified.add(asset);
    			}
    			LOG.info("Asset location counts: " + locs.size());
    			for (AssetLocation assetLoc : locs) {
    				if (StringUtils.equalsIgnoreCase(assetLoc.getAssetLocationTypeCode(), "O")) {
    					remove |= StringUtils.isBlank(assetLoc.getAssetLocationStreetAddress());
    				}
    			}
    			if (!remove) {
    				resultsModified.add(asset);
    			} else {
    				LOG.info("Removing asset: " + asset.getCapitalAssetNumber());
    			}
    		} else {
    			break;
    		}
    	}

    	LOG.info("Assets reviewed: " + count);
    	LOG.info("Results returned: " + resultsModified.size());
    	
    	return resultsModified;
    }
    

}
