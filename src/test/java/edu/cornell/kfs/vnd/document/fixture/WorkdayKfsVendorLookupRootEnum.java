package edu.cornell.kfs.vnd.document.fixture;

import java.util.Calendar;

import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.vnd.document.CuVendorMaintainableImplTest;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public enum WorkdayKfsVendorLookupRootEnum {
    EMPTY_LIST(),
    ACTIVE("1", 0, true, true, false),
    RECENT_TERMINATED("0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST - 3, false, true, true),
    TERMINATED_AT_MAX("0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST, false, true, true),
    OLD_TERMINATED("0", CuVendorMaintainableImplTest.TERMINATION_DAYS_IN_THE_PAST + 5, false, false, false);
    
    public final WorkdayKfsVendorLookupRoot lookupRoot;
    public final boolean expectedActive;
    public final boolean expectedActiveOrTerminatedInRange;
    public final boolean expectedTerminatedInRange;
    
    private WorkdayKfsVendorLookupRootEnum() {
        this.lookupRoot =  new WorkdayKfsVendorLookupRoot();
        this.expectedActive = false;
        this.expectedActiveOrTerminatedInRange = false;
        this.expectedTerminatedInRange = false;
    }
    
    private WorkdayKfsVendorLookupRootEnum(String activeStatus, int numberOfDaysInThePast, boolean expectedActive, boolean expectedActiveOrTerminatedInRange, boolean expectedTerminatedInRange) {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus(activeStatus);
        result.setTerminationDate(buildTerminationDate(numberOfDaysInThePast));
        root.getResults().add(result);
        this.lookupRoot =  root;
        this.expectedActive = expectedActive;
        this.expectedActiveOrTerminatedInRange = expectedActiveOrTerminatedInRange;
        this.expectedTerminatedInRange = expectedTerminatedInRange;
    }
    
    private static String buildTerminationDate(int numberOfDays) {
        DateTimeService dateTimeService = new TestDateTimeServiceImpl();
        Calendar currentCalendar = dateTimeService.getCurrentCalendar();
        currentCalendar.add(Calendar.DATE, (-1 * numberOfDays));
        String dateString = dateTimeService.toString(currentCalendar.getTime(), CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
        return dateString;
    }

}
