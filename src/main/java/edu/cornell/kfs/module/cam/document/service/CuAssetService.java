package edu.cornell.kfs.module.cam.document.service;

import org.kuali.kfs.module.cam.businessobject.Asset;

public interface CuAssetService {
    Asset updateAssetInventory(String capitalAssetNumber, String conditionCode, String buildingCode, String roomNumber);
}
