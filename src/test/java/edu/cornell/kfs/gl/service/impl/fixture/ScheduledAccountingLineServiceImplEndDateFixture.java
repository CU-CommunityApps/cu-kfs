package edu.cornell.kfs.gl.service.impl.fixture;

import java.util.Calendar;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes;

public enum ScheduledAccountingLineServiceImplEndDateFixture {
    DAILY(Calendar.DATE, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "1"),
    DAILY_TWICE(Calendar.DATE, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "2"),
    WEEKLY(Calendar.DATE, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "1"),
    WEEKLY_TWICE(Calendar.DATE, 14, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "2"),
    WEEKLY_TRICE(Calendar.DATE, 28, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "3"),
    MONTHLY(Calendar.MONTH, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "1"),
    MONTHLY_TWICE(Calendar.MONTH, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "2"),
    MONTHLY_SEMI(Calendar.YEAR, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "1"),
    MONTHLY_SEMI_TWICE(Calendar.YEAR, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "2");
    
    public final int expectedCalendarType;
    public final int expectedAmount; 
    public final CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes resultType;
    public final String resultAmount;

    private ScheduledAccountingLineServiceImplEndDateFixture(int expectedCalendarType, int expectedAmount,
            ScheduleTypes resultType, String resultAmount) {
        this.expectedCalendarType = expectedCalendarType;
        this.expectedAmount = expectedAmount;
        this.resultType = resultType;
        this.resultAmount = resultAmount;
    }
}
