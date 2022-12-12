package edu.cornell.kfs.module.cam.document.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.cam.businessobject.AssetExtension;
import edu.cornell.kfs.module.cam.document.service.CuAssetService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.impl.identity.PersonServiceImpl;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.CamsPropertyConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.document.service.impl.AssetServiceImpl;

import edu.cornell.kfs.module.cam.CuCamsConstants;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.impl.identity.Person;

public class CuAssetServiceImpl extends AssetServiceImpl implements CuAssetService {

    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private PersonServiceImpl personService;

    @Override
    public List<Asset> findActiveAssetsMatchingTagNumber(String campusTagNumber) {
        List<Asset> activeMatches = new ArrayList<>();
        // find all assets matching this tag number
        Map<String, String> params = new HashMap<>();
        params.put(CamsPropertyConstants.Asset.CAMPUS_TAG_NUMBER, campusTagNumber);
        Collection<Asset> tagMatches = businessObjectService.findMatching(Asset.class, params);
        if (tagMatches != null && !tagMatches.isEmpty()) {
            for (Asset asset : tagMatches) {
                // if found matching, check if status is not retired
                if (!isAssetRetired(asset) || parameterService.getParameterValueAsBoolean(CamsConstants.CAM_MODULE_CODE, "Asset", CuCamsConstants.Parameters.RE_USE_RETIRED_ASSET_TAG_NUMBER, Boolean.FALSE)) {
                    activeMatches.add(asset);
                }
            }
        }
        return activeMatches;
    }

    public Asset updateAssetInventory(Asset asset, String conditionCode, String buildingCode, String roomNumber, String netid) {
        asset.setConditionCode(conditionCode);
        asset.setBuildingCode(buildingCode);
        asset.setBuildingRoomNumber(roomNumber);
        Timestamp currentTimestamp = dateTimeService.getCurrentTimestamp();
        asset.setLastInventoryDate(currentTimestamp);
        asset.setLastUpdatedTimestamp(currentTimestamp);

        AssetExtension assetExtension = (AssetExtension) asset.getExtension();
        if (ObjectUtils.isNull(assetExtension)) {
            assetExtension = new AssetExtension();
            asset.setExtension(assetExtension);
        }

        Person person = personService.getPersonByPrincipalName(netid);
        String lastScannedBy = person.getFirstName() + " " + person.getLastName() + " (" + netid + ")";
        assetExtension.setLastScannedBy(lastScannedBy);
        assetExtension.setLastScannedDate(currentTimestamp);
        asset = businessObjectService.save(asset);
        return asset;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(ParameterService parameterService) {
        super.setParameterService(parameterService);
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPersonService(PersonServiceImpl personService) {
        this.personService = personService;
    }

}
