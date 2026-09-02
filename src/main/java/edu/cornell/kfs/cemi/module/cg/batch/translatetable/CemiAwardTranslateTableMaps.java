package edu.cornell.kfs.cemi.module.cg.batch.translatetable;

import java.util.Map;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiAwardTranslateTableMaps extends TransientBusinessObjectBase {

    private Map<String, String> awardLineLifecycleStatusMap;
    private Map<String, String> awardLineTypesMap;
    private Map<String, String> awardPurposeMap;
    private Map<String, String> sponsorAwardTypesMap;
    
    public CemiAwardTranslateTableMaps() {
        this.awardLineLifecycleStatusMap = null;
        this.awardLineTypesMap = null;
        this.awardPurposeMap = null;
        this.sponsorAwardTypesMap = null;
    }

    public Map<String, String> getAwardLineLifecycleStatusMap() {
        return awardLineLifecycleStatusMap;
    }

    public void setAwardLineLifecycleStatusMap(Map<String, String> awardLineLifecycleStatusMap) {
        this.awardLineLifecycleStatusMap = awardLineLifecycleStatusMap;
    }

    public Map<String, String> getAwardLineTypesMap() {
        return awardLineTypesMap;
    }

    public void setAwardLineTypesMap(Map<String, String> awardLineTypesMap) {
        this.awardLineTypesMap = awardLineTypesMap;
    }

    public Map<String, String> getAwardPurposeMap() {
        return awardPurposeMap;
    }

    public void setAwardPurposeMap(Map<String, String> awardPurposeMap) {
        this.awardPurposeMap = awardPurposeMap;
    }

    public Map<String, String> getSponsorAwardTypesMap() {
        return sponsorAwardTypesMap;
    }

    public void setSponsorAwardTypesMap(Map<String, String> sponsorAwardTypesMap) {
        this.sponsorAwardTypesMap = sponsorAwardTypesMap;
    }

}
