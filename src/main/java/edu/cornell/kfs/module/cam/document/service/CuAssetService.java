package edu.cornell.kfs.module.cam.document.service;

import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.document.service.AssetService;

public interface CuAssetService extends AssetService {
    Asset updateAssetInventory(String capitalAssetNumber, String conditionCode, String buildingCode, String roomNumber);
}
