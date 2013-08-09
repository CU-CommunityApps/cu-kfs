package edu.cornell.kfs.module.bc.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;







import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

import edu.cornell.kfs.module.bc.CUBCConstants;

@SuppressWarnings("serial")
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
    protected Date reHireDate;
    protected String sipEligibility;
    protected String employeeType;
    protected CUBCConstants.PSEntryStatus status;

    protected PSPositionInfo positionInfo;

    @SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
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

    public String getSipEligibility() {
        return sipEligibility;
    }

    public void setSipEligibility(String sipEligibility) {
        this.sipEligibility = sipEligibility;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public Date getReHireDate() {
        return reHireDate;
    }

    public void setReHireDate(Date reHireDate) {
        this.reHireDate = reHireDate;
    }

}
