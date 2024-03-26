package edu.cornell.kfs.module.cam.businessobject.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetLocation;
import org.kuali.kfs.module.cam.businessobject.lookup.AssetLookupableHelperServiceImpl;

import edu.cornell.kfs.module.cam.CuCamsPropertyConstants;

public class CuAssetLookupableHelperServiceImpl extends AssetLookupableHelperServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuAssetLookupableHelperServiceImpl.class);

    @Override
    protected List<? extends BusinessObject> getSearchResultsHelper(final Map<String, String> fieldValues, boolean unbounded) {
        // perform the lookup on the asset representative first
        final String principalName = fieldValues.get(CamsPropertyConstants.Asset.REP_USER_AUTH_ID);
        if (StringUtils.isNotBlank(principalName)) {
            final Person person = personService.getPersonByPrincipalName(principalName);

            if (person == null) {
                return Collections.EMPTY_LIST;
            }
            // place the universal ID into the fieldValues map and remove the dummy attribute
            fieldValues.put(CamsPropertyConstants.Asset.REPRESENTATIVE_UNIVERSAL_IDENTIFIER, person.getPrincipalId());
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

    protected List<? extends BusinessObject> excludeBlankOffCampusLocations(final List<? extends BusinessObject> results) {
        final List<Asset> resultsModified = new ArrayList<Asset>();
        int count = 0;
        LOG.info("Asset count: " + results.size());
        for (final BusinessObject boAsset : results) {
            Asset asset;
            if (boAsset instanceof Asset) {
                count++;
                boolean remove = false;
                asset = (Asset) boAsset;
                final List<AssetLocation> locs = asset.getAssetLocations();
                if (locs.isEmpty()) {
                    resultsModified.add(asset);
                }
                LOG.info("Asset location counts: " + locs.size());
                for (final AssetLocation assetLoc : locs) {
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
