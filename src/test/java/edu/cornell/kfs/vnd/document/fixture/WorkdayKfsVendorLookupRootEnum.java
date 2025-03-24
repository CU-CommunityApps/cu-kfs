package edu.cornell.kfs.vnd.document.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import edu.cornell.kfs.vnd.document.CuVendorMaintainableImpl;
import edu.cornell.kfs.vnd.document.CuVendorMaintainableImplTest;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public enum WorkdayKfsVendorLookupRootEnum {
    EMPTY_LIST(), ACTIVE("active1", "1", true, true, false), INACTIVE_NO_TERM_DATE("inactive1", "0", false, true, true),
    RECENT_TERMINATED("inactive2", "0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST - 3, false, true, true),
    TERMINATED_AT_MAX("inactive3", "0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST, false, true, true),
    OLD_TERMINATED("inactive4", "0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST + 5, false, false, false);

    public final boolean expectedActive;
    public final boolean expectedActiveOrTerminatedInRange;
    public final boolean expectedTerminatedInRange;

    private final String netId;
    private final String activeStatus;
    private final int numberOfDaysInThePast;
    private final boolean buildWorkdayKfsVendorLookupResult;
    private final boolean includeTerminationDate;

    private WorkdayKfsVendorLookupRootEnum() {
        this(false, false, false, null, null, 0, false, false);
    }

    private WorkdayKfsVendorLookupRootEnum(String netId, String activeStatus, boolean expectedActive,
            boolean expectedActiveOrTerminatedInRange, boolean expectedTerminatedInRange) {
        this(expectedActive, expectedActiveOrTerminatedInRange, expectedTerminatedInRange, netId, activeStatus, 0, true,
                false);
    }

    private WorkdayKfsVendorLookupRootEnum(String netId, String activeStatus, int numberOfDaysInThePast,
            boolean expectedActive, boolean expectedActiveOrTerminatedInRange, boolean expectedTerminatedInRange) {
        this(expectedActive, expectedActiveOrTerminatedInRange, expectedTerminatedInRange, netId, activeStatus,
                numberOfDaysInThePast, true, true);
    }

    private WorkdayKfsVendorLookupRootEnum(boolean expectedActive, boolean expectedActiveOrTerminatedInRange,
            boolean expectedTerminatedInRange, String netId, String activeStatus, int numberOfDaysInThePast,
            boolean buildWorkdayKfsVendorLookupResult, boolean includeTerminationDate) {
        this.expectedActive = expectedActive;
        this.expectedActiveOrTerminatedInRange = expectedActiveOrTerminatedInRange;
        this.expectedTerminatedInRange = expectedTerminatedInRange;
        this.netId = netId;
        this.activeStatus = activeStatus;
        this.numberOfDaysInThePast = numberOfDaysInThePast;
        this.buildWorkdayKfsVendorLookupResult = buildWorkdayKfsVendorLookupResult;
        this.includeTerminationDate = includeTerminationDate;
    }

    public WorkdayKfsVendorLookupRoot buildWorkdayKfsVendorLookupRoot() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        if (buildWorkdayKfsVendorLookupResult) {
            WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
            result.setNetID(netId);
            result.setActiveStatus(activeStatus);
            if (includeTerminationDate) {
                result.setTerminationDate(buildTerminationDate(numberOfDaysInThePast));
            }
            root.getResults().add(result);
        }
        return root;
    }

    private static String buildTerminationDate(int numberOfDays) {
        LocalDateTime minimumTerminationDate = LocalDate.now().minus(numberOfDays, ChronoUnit.DAYS).atStartOfDay();
        String dateString = minimumTerminationDate.format(CuVendorMaintainableImpl.DATE_FORMATTER);
        System.err.println("dateString: " + dateString);
        return dateString;
    }

}
