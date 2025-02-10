package edu.cornell.kfs.vnd.jsonobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkdayKfsVendorLookupResult {

    private static final String ACTIVE_STATUS_CODE = "1";

    @JsonProperty("NetID")
    private String netID;

    @JsonProperty("Active_Status")
    private String activeStatus;
    
    @JsonProperty("Employee_ID")
    private String employeeID;
    
    @JsonProperty("Termination_Date")
    private String terminationDate;
    
    @JsonProperty("Hire_Date")
    private String hireDate;
    
    @JsonProperty("Termination_Date_Greater_than_Processing_Date")
    private String terminationDateGreaterThanProcessingDate;

    public boolean isActive() {
        return StringUtils.equalsIgnoreCase(activeStatus, ACTIVE_STATUS_CODE);
    }

    public String getNetID() {
        return netID;
    }

    public void setNetID(String netID) {
        this.netID = netID;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public String getTerminationDateGreaterThanProcessingDate() {
        return terminationDateGreaterThanProcessingDate;
    }

    public void setTerminationDateGreaterThanProcessingDate(String terminationDateGreaterThanProcessingDate) {
        this.terminationDateGreaterThanProcessingDate = terminationDateGreaterThanProcessingDate;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
