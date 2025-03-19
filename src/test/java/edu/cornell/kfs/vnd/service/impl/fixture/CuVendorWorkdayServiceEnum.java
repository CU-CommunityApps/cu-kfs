package edu.cornell.kfs.vnd.service.impl.fixture;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public enum CuVendorWorkdayServiceEnum {

    SSN_NO_HYPHENS_INCLUDE_TERMINATED("000000000", "1", "001", true, true, "xyz321", "1", "123456789", "06-01-1998"),
    SSN_WITH_HYPHENS_INCLUDE_TERMINATED("111-22-3333", "1", "002", true, true, "abc123", "1", "98654321", "06-01-2019"),
    SSN_NO_HYPHENS_EXCLUDE_TERMINATED("111111111", "0", "003", true, false, "def987", "0", "456789321", "07-01-2024", "08-01-2024", "08-01-2024"),
    SSN_WITH_HYPHENS_EXCLUDE_TERMINATED("111-22-3333", "0", "004", true, false, "stu666", "0", "66666", "01-01-2025", "01-02-2025", "01-02-2025"),
    EMPTY_SSN_INCLUDE_TERMINATED(StringUtils.EMPTY, "1", "005", false, false),
    EMPTY_SSN_EMPTY_TERMINATED(StringUtils.EMPTY, StringUtils.EMPTY, "006", false, false),
    SSN_WITH_HYPHENS_NULL_TERMINATED("222-77-9999", null, "007", false, false), 
    NULL_SSN_INCLUDE_TERMINATED(null, "1", "008", false, false),
    NULL_SSN_NULL_TERMINATED(null, null, "009", false, false);

    public final String ssn;
    public final String includeTerminatedWorkers;
    public final String documentId;
    public final boolean activeOrInactiveEmployee;
    public final boolean activeEmployee;
    public final String netID;
    public final String activeStatus;
    public final String employeeID;
    public final String hireDate;
    public final String terminationDate;
    public final String terminationDateGreaterThanProcessingDate;

    private CuVendorWorkdayServiceEnum(String ssn, String includeTerminatedWorkers, String documentId,
            boolean activeOrInactiveEmployee, boolean activeEmployee) {
        this(ssn, includeTerminatedWorkers, documentId, activeOrInactiveEmployee, activeEmployee, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY);
    }

    private CuVendorWorkdayServiceEnum(String ssn, String includeTerminatedWorkers, String documentId,
            boolean activeOrInactiveEmployee, boolean activeEmployee, String netID, String activeStatus, String employeeID, String hireDate) {
        this(ssn,includeTerminatedWorkers, documentId, activeOrInactiveEmployee, activeEmployee, netID, activeStatus, employeeID, hireDate, null, null);
    }
    
    private CuVendorWorkdayServiceEnum(String ssn, String includeTerminatedWorkers, String documentId,
            boolean activeOrInactiveEmployee, boolean activeEmployee, String netID, String activeStatus, String employeeID, String hireDate,
            String terminationDate, String terminationDateGreaterThanProcessingDate) {
        this.ssn = ssn;
        this.includeTerminatedWorkers = includeTerminatedWorkers;
        this.documentId = documentId;
        this.activeOrInactiveEmployee = activeOrInactiveEmployee;
        this.activeEmployee = activeEmployee;
        this.netID = netID;
        this.activeStatus = activeStatus;
        this.employeeID = employeeID;
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.terminationDateGreaterThanProcessingDate = terminationDateGreaterThanProcessingDate;
    }
    
    public WorkdayKfsVendorLookupRoot toWorkdayKfsVendorLookupRoot() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        if (StringUtils.isNotBlank(netID)) {
            WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
            result.setActiveStatus(activeStatus);
            result.setEmployeeID(employeeID);
            result.setHireDate(hireDate);
            result.setNetID(netID);
            result.setTerminationDate(terminationDate);
            result.setTerminationDateGreaterThanProcessingDate(terminationDateGreaterThanProcessingDate);
            root.getResults().add(result);
        }
        return root;
    }

    public static CuVendorWorkdayServiceEnum findCuVendorWorkdayServiceEnum(String includedTerminatedWorkers,
            String ssn) {
        for (CuVendorWorkdayServiceEnum serviceEnum : values()) {
            if (StringUtils.equals(includedTerminatedWorkers, serviceEnum.includeTerminatedWorkers) && 
                    StringUtils.equals(ssn, serviceEnum.ssn)) {
                return serviceEnum;
            }
        }
        throw new IllegalArgumentException("Unable to find a CuVendorWorkdayServiceEnum with an SSN " + ssn
                + " and include terminated workers " + includedTerminatedWorkers);
    }

}
