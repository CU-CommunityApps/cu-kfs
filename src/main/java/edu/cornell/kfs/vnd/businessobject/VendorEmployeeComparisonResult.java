package edu.cornell.kfs.vnd.businessobject;

import java.time.LocalDate;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class VendorEmployeeComparisonResult extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String vendorId;
    private String employeeId;
    private String netId;
    private Boolean active;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private LocalDate terminationDateGreaterThanProcessingDate;

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public boolean isActive() {
        return active != null && active.booleanValue();
    }

    public boolean isInactive() {
        return active != null && !active.booleanValue();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public LocalDate getTerminationDateGreaterThanProcessingDate() {
        return terminationDateGreaterThanProcessingDate;
    }

    public void setTerminationDateGreaterThanProcessingDate(LocalDate terminationDateGreaterThanProcessingDate) {
        this.terminationDateGreaterThanProcessingDate = terminationDateGreaterThanProcessingDate;
    }

}
