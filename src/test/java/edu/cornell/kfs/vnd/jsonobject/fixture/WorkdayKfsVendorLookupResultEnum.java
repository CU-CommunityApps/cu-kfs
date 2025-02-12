package edu.cornell.kfs.vnd.jsonobject.fixture;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;

public enum WorkdayKfsVendorLookupResultEnum {
    ACTIVE("xyz321", "1", "23644894", null, "1998-06-01", null),
    INACTIVE("abc123", "0", "1234567", "2024-05-27", "2023-09-10", "2024-05-27"),
    EMPTY();
    
    public final String netID;
    public final String activeStatus;
    public final String employeeID;
    public final String terminationDate;
    public final String hireDate;
    public final String terminationDateGreaterThanProcessingDate;
    
    private WorkdayKfsVendorLookupResultEnum() {
        this(null, null, null, null, null, null);
    }
    
    private WorkdayKfsVendorLookupResultEnum(String netID, String activeStatus, String employeeID,
            String terminationDate, String hireDate, String terminationDateGreaterThanProcessingDate) {
        this.netID = netID;
        this.activeStatus = activeStatus;
        this.employeeID = employeeID;
        this.terminationDate = terminationDate;
        this.hireDate = hireDate;
        this.terminationDateGreaterThanProcessingDate = terminationDateGreaterThanProcessingDate;
    }
    
    public WorkdayKfsVendorLookupResult toWorkdayKfsVendorLookupResult() {
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus(activeStatus);
        result.setEmployeeID(employeeID);
        result.setHireDate(hireDate);
        result.setNetID(netID);
        result.setTerminationDate(terminationDate);
        result.setTerminationDateGreaterThanProcessingDate(terminationDateGreaterThanProcessingDate);
        return result;
    }
    
}
