package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * BO for PS_POSITION_T table.
 */
public class PSPositionInfo extends PersistableBusinessObjectBase {
    protected String positionNumber;
    protected String positionType;
    protected String positionUnionCode;
    protected Integer workMonths;
    protected String jobCode;
    protected String fullPartTime;
    protected String classInd;
    protected String addsToActualFte;
    protected String cuStateCert;
    
    protected PSJobCode psJobCode;

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public String getPositionUnionCode() {
        return positionUnionCode;
    }

    public void setPositionUnionCode(String positionUnionCode) {
        this.positionUnionCode = positionUnionCode;
    }

    public Integer getWorkMonths() {
        return workMonths;
    }

    public void setWorkMonths(Integer workMonths) {
        this.workMonths = workMonths;
    }

    public String getFullPartTime() {
        return fullPartTime;
    }

    public void setFullPartTime(String fullPartTime) {
        this.fullPartTime = fullPartTime;
    }

    public String getClassInd() {
        return classInd;
    }

    public void setClassInd(String classInd) {
        this.classInd = classInd;
    }

    public String getAddsToActualFte() {
        return addsToActualFte;
    }

    public void setAddsToActualFte(String addsToActualFte) {
        this.addsToActualFte = addsToActualFte;
    }

    public String getCuStateCert() {
        return cuStateCert;
    }

    public void setCuStateCert(String cuStateCert) {
        this.cuStateCert = cuStateCert;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public PSJobCode getPsJobCode() {
        return psJobCode;
    }

    public void setPsJobCode(PSJobCode psJobCode) {
        this.psJobCode = psJobCode;
    }

}
