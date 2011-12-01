package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.rice.kns.bo.BusinessObjectBase;
import org.kuali.rice.kns.util.TypedArrayList;

import edu.cornell.kfs.module.bc.CUBCConstants;
import edu.cornell.kfs.module.bc.CUBCConstants.StatusFlag;

/**
 * A class that holds the fields from the PS position/job extract file to be loaded in
 * KFS.
 */
public class PSPositionJobExtractEntry extends BusinessObjectBase {

    public CUBCConstants.PSEntryStatus deleteStatus;
    public StatusFlag changeStatus;

    private String positionNumber;
    private String emplid;
    private String name;
    private String employeeType;
    private String defaultObjectCode;
    private String positionUnionCode;
    private String workMonths;
    private String jobCode;
    protected String jobCodeDesc;
    protected String jobCodeDescShrt;
    protected String company;
    protected String fullPartTime;
    protected String classInd;
    protected String addsToActualFte;
    protected String cuStateCert;
    protected String employeeRecord;
    protected String employeeStatus;
    protected String jobStandardHours;
    protected String jobCodeStandardHours;
    protected String employeeClass;
    protected String earningDistributionType;
    protected String compRate;
    protected String annualBenefitBaseRate;
    protected String cuAbbrFlag;
    protected String annualRate;
    protected String jobFamily;
    protected String compFreq;
    protected String jobFunction;
    protected String jobFunctionDesc;
    protected String cuPlannedFTE;

    private List<PSPositionJobExtractAccountingInfo> csfAccountingInfoList;
    private List<PSPositionJobExtractAccountingInfo> posAccountingInfoList;

    public PSPositionJobExtractEntry() {
        csfAccountingInfoList = new TypedArrayList(PSPositionJobExtractAccountingInfo.class);
        posAccountingInfoList = new TypedArrayList(PSPositionJobExtractAccountingInfo.class);
    }

    /**
     * Gets the positionNumber.
     * 
     * @return positionNumber
     */
    public String getPositionNumber() {
        return CUBCConstants.POSITION_NUMBER_PREFIX + positionNumber;
    }

    /**
     * Sets the positionNumber.
     * 
     * @param positionNumber
     */
    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    /**
     * Gets the emplid.
     * 
     * @return emplid
     */
    public String getEmplid() {
        return emplid;
    }

    /**
     * Sets the emplid.
     * 
     * @param emplid
     */
    public void setEmplid(String emplid) {
        this.emplid = emplid;
    }

    /**
     * Gets the name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the key for the current entry which is positionNumber + emplid;
     * 
     * @return
     */
    public String getKey() {
        return this.getPositionNumber() + this.getEmplid();
    }

    /**
     * Gets the employeeType.
     * 
     * @return employeeType
     */
    public String getEmployeeType() {
        return employeeType;
    }

    /**
     * Sets the employeeType.
     * 
     * @param employeeType
     */
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    /**
     * Gets the defaultObjectCode.
     * 
     * @return defaultObjectCode
     */
    public String getDefaultObjectCode() {
        return defaultObjectCode;
    }

    /**
     * Sets the defaultObjectCode
     * 
     * @param defaultObjectCode
     */
    public void setDefaultObjectCode(String defaultObjectCode) {
        this.defaultObjectCode = defaultObjectCode;
    }

    /**
     * Gets the workMonths.
     * 
     * @return workMonths
     */
    public String getWorkMonths() {
        return workMonths;
    }

    /**
     * Sets the workMonths.
     * 
     * @param workMonths
     */
    public void setWorkMonths(String workMonths) {
        this.workMonths = workMonths;
    }

    /**
     * Gets the positionUnionCode.
     * 
     * @return positionUnionCode
     */
    public String getPositionUnionCode() {
        return positionUnionCode;
    }

    /**
     * Sets the positionUnionCode.
     * 
     * @param positionUnionCode
     */
    public void setPositionUnionCode(String positionUnionCode) {
        this.positionUnionCode = positionUnionCode;
    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    public String getJobCodeDesc() {
        return jobCodeDesc;
    }

    public void setJobCodeDesc(String jobCodeDesc) {
        this.jobCodeDesc = jobCodeDesc;
    }

    public String getJobCodeDescShrt() {
        return jobCodeDescShrt;
    }

    public void setJobCodeDescShrt(String jobCodeDescShrt) {
        this.jobCodeDescShrt = jobCodeDescShrt;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public String getJobStandardHours() {
        return jobStandardHours;
    }

    public void setJobStandardHours(String jobStandardHours) {
        this.jobStandardHours = jobStandardHours;
    }

    public String getJobCodeStandardHours() {
        return jobCodeStandardHours;
    }

    public void setJobCodeStandardHours(String jobCodeStandardHours) {
        this.jobCodeStandardHours = jobCodeStandardHours;
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

    public String getCompRate() {
        return compRate;
    }

    public void setCompRate(String compRate) {
        this.compRate = compRate;
    }

    public String getAnnualBenefitBaseRate() {
        return annualBenefitBaseRate;
    }

    public void setAnnualBenefitBaseRate(String annualBenefitBaseRate) {
        this.annualBenefitBaseRate = annualBenefitBaseRate;
    }

    public String getJobFamily() {
        return jobFamily;
    }

    public void setJobFamily(String jobFamily) {
        this.jobFamily = jobFamily;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getCuAbbrFlag() {
        return cuAbbrFlag;
    }

    public void setCuAbbrFlag(String cuAbbrFlag) {
        this.cuAbbrFlag = cuAbbrFlag;
    }

    public String getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(String annualRate) {
        this.annualRate = annualRate;
    }

    public CUBCConstants.PSEntryStatus getStatus() {
        return deleteStatus;
    }

    public void setStatus(CUBCConstants.PSEntryStatus status) {
        this.deleteStatus = status;
    }

    public String getJobFunction() {
        return jobFunction;
    }

    public void setJobFunction(String jobFunction) {
        this.jobFunction = jobFunction;
    }

    public String getJobFunctionDesc() {
        return jobFunctionDesc;
    }

    public void setJobFunctionDesc(String jobFunctionDesc) {
        this.jobFunctionDesc = jobFunctionDesc;
    }

    public String getCompFreq() {
        return compFreq;
    }

    public void setCompFreq(String compFreq) {
        this.compFreq = compFreq;
    }

    public String getCuPlannedFTE() {
        return cuPlannedFTE;
    }

    public void setCuPlannedFTE(String cuPlannedFTE) {
        this.cuPlannedFTE = cuPlannedFTE;
    }

    public CUBCConstants.PSEntryStatus getDeleteStatus() {
        return deleteStatus;
    }

    public void setDeleteStatus(CUBCConstants.PSEntryStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    public StatusFlag getChangeStatus() {
        return changeStatus;
    }

    public void setChangeStatus(StatusFlag changeStatus) {
        this.changeStatus = changeStatus;
    }

    /**
     * @return the csfAccountingInfoList
     */
    public List<PSPositionJobExtractAccountingInfo> getCsfAccountingInfoList() {
        return csfAccountingInfoList;
    }

    /**
     * @return the posAccountingInfoList
     */
    public List<PSPositionJobExtractAccountingInfo> getPosAccountingInfoList() {
        return posAccountingInfoList;
    }

    /**
     * @param csfAccountingInfoList the csfAccountingInfoList to set
     */
    public void setCsfAccountingInfoList(List<PSPositionJobExtractAccountingInfo> csfAccountingInfoList) {
        this.csfAccountingInfoList = csfAccountingInfoList;
    }

    /**
     * @param posAccountingInfoList the posAccountingInfoList to set
     */
    public void setPosAccountingInfoList(List<PSPositionJobExtractAccountingInfo> posAccountingInfoList) {
        this.posAccountingInfoList = posAccountingInfoList;
    }

    /**
     * This overridden method ...
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addsToActualFte == null) ? 0 : addsToActualFte.hashCode());
        result = prime * result + ((annualBenefitBaseRate == null) ? 0 : annualBenefitBaseRate.hashCode());
        result = prime * result + ((annualRate == null) ? 0 : annualRate.hashCode());
        result = prime * result + ((changeStatus == null) ? 0 : changeStatus.hashCode());
        result = prime * result + ((classInd == null) ? 0 : classInd.hashCode());
        result = prime * result + ((compFreq == null) ? 0 : compFreq.hashCode());
        result = prime * result + ((compRate == null) ? 0 : compRate.hashCode());
        result = prime * result + ((company == null) ? 0 : company.hashCode());
        result = prime * result + ((csfAccountingInfoList == null) ? 0 : csfAccountingInfoList.hashCode());
        result = prime * result + ((cuAbbrFlag == null) ? 0 : cuAbbrFlag.hashCode());
        result = prime * result + ((cuPlannedFTE == null) ? 0 : cuPlannedFTE.hashCode());
        result = prime * result + ((cuStateCert == null) ? 0 : cuStateCert.hashCode());
        result = prime * result + ((defaultObjectCode == null) ? 0 : defaultObjectCode.hashCode());
        result = prime * result + ((deleteStatus == null) ? 0 : deleteStatus.hashCode());
        result = prime * result + ((earningDistributionType == null) ? 0 : earningDistributionType.hashCode());
        result = prime * result + ((emplid == null) ? 0 : emplid.hashCode());
        result = prime * result + ((employeeClass == null) ? 0 : employeeClass.hashCode());
        result = prime * result + ((employeeRecord == null) ? 0 : employeeRecord.hashCode());
        result = prime * result + ((employeeStatus == null) ? 0 : employeeStatus.hashCode());
        result = prime * result + ((employeeType == null) ? 0 : employeeType.hashCode());
        result = prime * result + ((fullPartTime == null) ? 0 : fullPartTime.hashCode());
        result = prime * result + ((jobCode == null) ? 0 : jobCode.hashCode());
        result = prime * result + ((jobCodeDesc == null) ? 0 : jobCodeDesc.hashCode());
        result = prime * result + ((jobCodeDescShrt == null) ? 0 : jobCodeDescShrt.hashCode());
        result = prime * result + ((jobCodeStandardHours == null) ? 0 : jobCodeStandardHours.hashCode());
        result = prime * result + ((jobFamily == null) ? 0 : jobFamily.hashCode());
        result = prime * result + ((jobFunction == null) ? 0 : jobFunction.hashCode());
        result = prime * result + ((jobFunctionDesc == null) ? 0 : jobFunctionDesc.hashCode());
        result = prime * result + ((jobStandardHours == null) ? 0 : jobStandardHours.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((posAccountingInfoList == null) ? 0 : posAccountingInfoList.hashCode());
        result = prime * result + ((positionNumber == null) ? 0 : positionNumber.hashCode());
        result = prime * result + ((positionUnionCode == null) ? 0 : positionUnionCode.hashCode());
        result = prime * result + ((workMonths == null) ? 0 : workMonths.hashCode());
        return result;
    }

    /**
     * This overridden method ...
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PSPositionJobExtractEntry other = (PSPositionJobExtractEntry) obj;
        if (addsToActualFte == null) {
            if (other.addsToActualFte != null)
                return false;
        } else if (!addsToActualFte.equals(other.addsToActualFte))
            return false;
        if (annualBenefitBaseRate == null) {
            if (other.annualBenefitBaseRate != null)
                return false;
        } else if (!annualBenefitBaseRate.equals(other.annualBenefitBaseRate))
            return false;
        if (annualRate == null) {
            if (other.annualRate != null)
                return false;
        } else if (!annualRate.equals(other.annualRate))
            return false;
        if (changeStatus != other.changeStatus)
            return false;
        if (classInd == null) {
            if (other.classInd != null)
                return false;
        } else if (!classInd.equals(other.classInd))
            return false;
        if (compFreq == null) {
            if (other.compFreq != null)
                return false;
        } else if (!compFreq.equals(other.compFreq))
            return false;
        if (compRate == null) {
            if (other.compRate != null)
                return false;
        } else if (!compRate.equals(other.compRate))
            return false;
        if (company == null) {
            if (other.company != null)
                return false;
        } else if (!company.equals(other.company))
            return false;
        if (csfAccountingInfoList == null) {
            if (other.csfAccountingInfoList != null)
                return false;
        } else if (!equalAccountingStringsLists(csfAccountingInfoList, other.csfAccountingInfoList))
            return false;
        if (cuAbbrFlag == null) {
            if (other.cuAbbrFlag != null)
                return false;
        } else if (!cuAbbrFlag.equals(other.cuAbbrFlag))
            return false;
        if (cuPlannedFTE == null) {
            if (other.cuPlannedFTE != null)
                return false;
        } else if (!cuPlannedFTE.equals(other.cuPlannedFTE))
            return false;
        if (cuStateCert == null) {
            if (other.cuStateCert != null)
                return false;
        } else if (!cuStateCert.equals(other.cuStateCert))
            return false;
        if (defaultObjectCode == null) {
            if (other.defaultObjectCode != null)
                return false;
        } else if (!defaultObjectCode.equals(other.defaultObjectCode))
            return false;
        if (deleteStatus != other.deleteStatus)
            return false;
        if (earningDistributionType == null) {
            if (other.earningDistributionType != null)
                return false;
        } else if (!earningDistributionType.equals(other.earningDistributionType))
            return false;
        if (emplid == null) {
            if (other.emplid != null)
                return false;
        } else if (!emplid.equals(other.emplid))
            return false;
        if (employeeClass == null) {
            if (other.employeeClass != null)
                return false;
        } else if (!employeeClass.equals(other.employeeClass))
            return false;
        if (employeeRecord == null) {
            if (other.employeeRecord != null)
                return false;
        } else if (!employeeRecord.equals(other.employeeRecord))
            return false;
        if (employeeStatus == null) {
            if (other.employeeStatus != null)
                return false;
        } else if (!employeeStatus.equals(other.employeeStatus))
            return false;
        if (employeeType == null) {
            if (other.employeeType != null)
                return false;
        } else if (!employeeType.equals(other.employeeType))
            return false;
        if (fullPartTime == null) {
            if (other.fullPartTime != null)
                return false;
        } else if (!fullPartTime.equals(other.fullPartTime))
            return false;
        if (jobCode == null) {
            if (other.jobCode != null)
                return false;
        } else if (!jobCode.equals(other.jobCode))
            return false;
        if (jobCodeDesc == null) {
            if (other.jobCodeDesc != null)
                return false;
        } else if (!jobCodeDesc.equals(other.jobCodeDesc))
            return false;
        if (jobCodeDescShrt == null) {
            if (other.jobCodeDescShrt != null)
                return false;
        } else if (!jobCodeDescShrt.equals(other.jobCodeDescShrt))
            return false;
        if (jobCodeStandardHours == null) {
            if (other.jobCodeStandardHours != null)
                return false;
        } else if (!jobCodeStandardHours.equals(other.jobCodeStandardHours))
            return false;
        if (jobFamily == null) {
            if (other.jobFamily != null)
                return false;
        } else if (!jobFamily.equals(other.jobFamily))
            return false;
        if (jobFunction == null) {
            if (other.jobFunction != null)
                return false;
        } else if (!jobFunction.equals(other.jobFunction))
            return false;
        if (jobFunctionDesc == null) {
            if (other.jobFunctionDesc != null)
                return false;
        } else if (!jobFunctionDesc.equals(other.jobFunctionDesc))
            return false;
        if (jobStandardHours == null) {
            if (other.jobStandardHours != null)
                return false;
        } else if (!jobStandardHours.equals(other.jobStandardHours))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (posAccountingInfoList == null) {
            if (other.posAccountingInfoList != null)
                return false;
        } else if (!equalAccountingStringsLists(posAccountingInfoList, other.posAccountingInfoList))
            return false;
        if (positionNumber == null) {
            if (other.positionNumber != null)
                return false;
        } else if (!positionNumber.equals(other.positionNumber))
            return false;
        if (positionUnionCode == null) {
            if (other.positionUnionCode != null)
                return false;
        } else if (!positionUnionCode.equals(other.positionUnionCode))
            return false;
        if (workMonths == null) {
            if (other.workMonths != null)
                return false;
        } else if (!workMonths.equals(other.workMonths))
            return false;
        return true;
    }

    /**
     * This overridden method ...
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PSPositionJobExtractEntry [");
        builder.append(positionNumber);
        builder.append(", ");
        builder.append(emplid);
        builder.append(", ");
        builder.append(name);
        builder.append(", ");
        builder.append(employeeType);
        builder.append(", ");
        builder.append(defaultObjectCode);
        builder.append(", ");
        builder.append(positionUnionCode);
        builder.append(", ");
        builder.append(workMonths);
        builder.append(", ");
        builder.append(jobCode);
        builder.append(", ");
        builder.append(jobCodeDesc);
        builder.append(", ");
        builder.append(jobCodeDescShrt);
        builder.append(", ");
        builder.append(company);
        builder.append(", ");
        builder.append(fullPartTime);
        builder.append(", ");
        builder.append(classInd);
        builder.append(", ");
        builder.append(addsToActualFte);
        builder.append(", ");
        builder.append(cuStateCert);
        builder.append(", ");
        builder.append(employeeRecord);
        builder.append(", ");
        builder.append(employeeStatus);
        builder.append(", ");
        builder.append(jobStandardHours);
        builder.append(", ");
        builder.append(jobCodeStandardHours);
        builder.append(", ");
        builder.append(employeeClass);
        builder.append(", ");
        builder.append(earningDistributionType);
        builder.append(", ");
        builder.append(compRate);
        builder.append(", ");
        builder.append(annualBenefitBaseRate);
        builder.append(", ");
        builder.append(cuAbbrFlag);
        builder.append(", ");
        builder.append(annualRate);
        builder.append(", ");
        builder.append(jobFamily);
        builder.append(", ");
        builder.append(compFreq);
        builder.append(", ");
        builder.append(jobFunction);
        builder.append(", ");
        builder.append(jobFunctionDesc);
        builder.append(", ");
        builder.append(cuPlannedFTE);
        builder.append(", ");
        builder.append(csfAccountingInfoList);
        builder.append(", ");
        builder.append(posAccountingInfoList);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Checks to see if the current accounting strings list equals another accounting
     * strings list.
     * 
     * @param thisAccountingStringList
     * @param otherAccountingStringList
     * @return true if equal false otherwise
     */
    private boolean equalAccountingStringsLists(List<PSPositionJobExtractAccountingInfo> thisAccountingStringList,
            List<PSPositionJobExtractAccountingInfo> otherAccountingStringList) {

        boolean result = true;

        StringBuffer thisAccStrings = new StringBuffer();

        for (PSPositionJobExtractAccountingInfo accountingInfo : thisAccountingStringList) {
            thisAccStrings.append(accountingInfo.toString());
        }

        StringBuffer otherAccStrings = new StringBuffer();

        for (PSPositionJobExtractAccountingInfo accountingInfo : otherAccountingStringList) {
            otherAccStrings.append(accountingInfo.toString());
        }

        if (!thisAccStrings.toString().equalsIgnoreCase(otherAccStrings.toString())) {
            result = false;
        }

        return result;
    }

}
