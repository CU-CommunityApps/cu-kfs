package edu.cornell.kfs.cemi.module.cam.batch.translatetable;

import java.util.Map;

public class CemiRegisterAssetTranslateTableMaps {
    
    private Map<String, String> assetTypeMap;
    private Map<String, String> accountingTreatmentMap;
    private Map<String, String> assetClassMap;
    private Map<String, String> depreciationProfileMap;

    public CemiRegisterAssetTranslateTableMaps() {
        this.assetTypeMap = null;
        this.accountingTreatmentMap = null;
        this.assetClassMap = null;
        this.depreciationProfileMap = null;
    }

    public Map<String, String> getAssetTypeMap() {
        return assetTypeMap;
    }

    public void setAssetTypeMap(Map<String, String> assetTypeMap) {
        this.assetTypeMap = assetTypeMap;
    }

    public Map<String, String> getAccountingTreatmentMap() {
        return accountingTreatmentMap;
    }

    public void setAccountingTreatmentMap(Map<String, String> accountingTreatmentMap) {
        this.accountingTreatmentMap = accountingTreatmentMap;
    }

    public Map<String, String> getAssetClassMap() {
        return assetClassMap;
    }

    public void setAssetClassMap(Map<String, String> assetClassMap) {
        this.assetClassMap = assetClassMap;
    }

    public Map<String, String> getDepreciationProfileMap() {
        return depreciationProfileMap;
    }

    public void setDepreciationProfileMap(Map<String, String> depreciationProfileMap) {
        this.depreciationProfileMap = depreciationProfileMap;
    }

}
