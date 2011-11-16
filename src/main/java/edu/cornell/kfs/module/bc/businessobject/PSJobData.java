package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.bc.CUBCConstants;

public class PSJobData extends PersistableBusinessObjectBase {
    protected String positionNumber;
    protected String emplid;
    protected String employeeRecord;
    protected String employeeStatus;
    protected KualiDecimal jobStandardHours;
    protected String employeeClass;
    protected String earningDistributionType;
    protected KualiDecimal compRate;
    protected KualiDecimal annualBenefitBaseRate;
    protected String cuAbbrFlag;
    protected KualiDecimal annualRate;
    protected String employeeName;
    protected KualiDecimal cuPlannedFTE;
    protected CUBCConstants.PSEntryStatus status;

    protected PSPositionInfo positionInfo;

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

    public String getEmplid() {
        return emplid;
    }

    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    public String getEmployeeRecord() {
        return employeeRecord;
    }

    public void setEmployeeRecord(String employeeRecord) {
        this.employeeRecord = employeeRecord;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public KualiDecimal getJobStandardHours() {
        return jobStandardHours;
    }

    public void setJobStandardHours(KualiDecimal jobStandardHours) {
        this.jobStandardHours = jobStandardHours;
    }

    public String getEmployeeClass() {
        return employeeClass;
    }

    public void setEmployeeClass(String employeeClass) {
        this.employeeClass = employeeClass;
    }

    public String getEarningDistributionType() {
        return earningDistributionType;
    }

    public void setEarningDistributionType(String earningDistributionType) {
        this.earningDistributionType = earningDistributionType;
    }

    public KualiDecimal getCompRate() {
        return compRate;
    }

    public void setCompRate(KualiDecimal compRate) {
        this.compRate = compRate;
    }

    public KualiDecimal getAnnualBenefitBaseRate() {
        return annualBenefitBaseRate;
    }

    public void setAnnualBenefitBaseRate(KualiDecimal annualBenefitBaseRate) {
        this.annualBenefitBaseRate = annualBenefitBaseRate;
    }

    public String getCuAbbrFlag() {
        return cuAbbrFlag;
    }

    public void setCuAbbrFlag(String cuAbbrFlag) {
        this.cuAbbrFlag = cuAbbrFlag;
    }

    public KualiDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(KualiDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public PSPositionInfo getPositionInfo() {
        return positionInfo;
    }

    public void setPositionInfo(PSPositionInfo positionInfo) {
        this.positionInfo = positionInfo;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public CUBCConstants.PSEntryStatus getStatus() {
        return status;
    }

    public void setStatus(CUBCConstants.PSEntryStatus status) {
        this.status = status;
    }

    public KualiDecimal getCuPlannedFTE() {
        return cuPlannedFTE;
    }

    public void setCuPlannedFTE(KualiDecimal cuPlannedFTE) {
        this.cuPlannedFTE = cuPlannedFTE;
    }

}
