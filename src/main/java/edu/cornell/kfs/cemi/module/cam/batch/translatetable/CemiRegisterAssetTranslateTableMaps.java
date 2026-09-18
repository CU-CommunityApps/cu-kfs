package edu.cornell.kfs.cemi.module.cam.batch.translatetable;

import java.util.Map;

public class CemiRegisterAssetTranslateTableMaps {
    
    private Map<String, String> assetTypeMap;
    private Map<String, String> accountingTreatmentap;
    private Map<String, String> assetClassMap;
    private Map<String, String> depreciationProfileMap;

    public CemiRegisterAssetTranslateTableMaps() {
        this.assetTypeMap = null;
        this.accountingTreatmentap = null;
        this.assetClassMap = null;
        this.depreciationProfileMap = null;
    }

    public Map<String, String> getAssetTypeMap() {
        return assetTypeMap;
    }

    public void setAssetTypeMap(Map<String, String> assetTypeMap) {
        this.assetTypeMap = assetTypeMap;
    }

    public Map<String, String> getAccountingTreatmentap() {
        return accountingTreatmentap;
    }

    public void setAccountingTreatmentap(Map<String, String> accountingTreatmentap) {
        this.accountingTreatmentap = accountingTreatmentap;
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
