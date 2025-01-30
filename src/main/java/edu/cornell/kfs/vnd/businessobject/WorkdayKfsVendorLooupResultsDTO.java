package edu.cornell.kfs.vnd.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WorkdayKfsVendorLooupResultsDTO {
    
    private static final String ACTIVE_STATUS_CODE = "1";

    private String NetID;
    private String Active_Status;
    private String Employee_ID;
    private String Termination_Date;
    private String Hire_Date;
    private String Termination_Date_Greater_than_Processing_Date;

    public String getNetID() {
        return NetID;
    }

    public void setNetID(String netID) {
        NetID = netID;
    }
    
    public boolean isActive() {
        return StringUtils.equalsIgnoreCase(Active_Status, ACTIVE_STATUS_CODE);
    }

    public String getActive_Status() {
        return Active_Status;
    }

    public void setActive_Status(String active_Status) {
        Active_Status = active_Status;
    }

    public String getEmployee_ID() {
        return Employee_ID;
    }

    public void setEmployee_ID(String employee_ID) {
        Employee_ID = employee_ID;
    }

    public String getTermination_Date() {
        return Termination_Date;
    }

    public void setTermination_Date(String termination_Date) {
        Termination_Date = termination_Date;
    }

    public String getHire_Date() {
        return Hire_Date;
    }

    public void setHire_Date(String hire_Date) {
        Hire_Date = hire_Date;
    }

    public String getTermination_Date_Greater_than_Processing_Date() {
        return Termination_Date_Greater_than_Processing_Date;
    }

    public void setTermination_Date_Greater_than_Processing_Date(String termination_Date_Greater_than_Processing_Date) {
        Termination_Date_Greater_than_Processing_Date = termination_Date_Greater_than_Processing_Date;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
