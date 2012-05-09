package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.BusinessObjectBase;

public class SIPToHRData extends BusinessObjectBase{
    
    private String positionID;
    private String emplID;
    private String preSIPCompRate;
    private String postSIPCompRate;
    private String actionCode;
    private String actionReason;
    private String sipEffectiveDate;
    private String compensationFrequency;
    private String uawPostSIPStep;

    public void refresh() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPositionID() {
        return positionID;
    }

    public void setPositionID(String positionID) {
        this.positionID = positionID;
    }

    public String getEmplID() {
        return emplID;
    }

    public void setEmplID(String emplID) {
        this.emplID = emplID;
    }

    public String getPreSIPCompRate() {
        return preSIPCompRate;
    }

    public void setPreSIPCompRate(String preSIPCompRate) {
        this.preSIPCompRate = preSIPCompRate;
    }

    public String getPostSIPCompRate() {
        return postSIPCompRate;
    }

    public void setPostSIPCompRate(String postSIPCompRate) {
        this.postSIPCompRate = postSIPCompRate;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionReason() {
        return actionReason;
    }

    public void setActionReason(String actionReason) {
        this.actionReason = actionReason;
    }

    public String getSipEffectiveDate() {
        return sipEffectiveDate;
    }

    public void setSipEffectiveDate(String sipEffectiveDate) {
        this.sipEffectiveDate = sipEffectiveDate;
    }

    public String getCompensationFrequency() {
        return compensationFrequency;
    }

    public void setCompensationFrequency(String compensationFrequency) {
        this.compensationFrequency = compensationFrequency;
    }

    public String getUawPostSIPStep() {
        return uawPostSIPStep;
    }

    public void setUawPostSIPStep(String uawPostSIPStep) {
        this.uawPostSIPStep = uawPostSIPStep;
    }

}
