package edu.cornell.kfs.gl.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes;
import edu.cornell.kfs.fp.businessobject.ScheduledSourceAccountingLine;
import edu.cornell.kfs.fp.service.impl.ScheduledAccountingLineServiceImpl;

public class ScheduledAccountingLineServiceImplTest {
	
	ScheduledAccountingLineServiceImpl scheduledAccountingLineUtil;
	Date startingPointDate;
	Calendar startingPointCal;
	ScheduledSourceAccountingLine accountingLine;

	@Before
	public void setUp() throws Exception {
		scheduledAccountingLineUtil = new ScheduledAccountingLineServiceImpl();
		
		startingPointDate = new Date(Calendar.getInstance().getTimeInMillis());
		startingPointCal = Calendar.getInstance();
		startingPointCal.setTimeInMillis(startingPointDate.getTime());
		
		accountingLine = new ScheduledSourceAccountingLine();
		accountingLine.setStartDate(startingPointDate);
	}

	@After
	public void tearDown() throws Exception {
		scheduledAccountingLineUtil = null;
		startingPointDate = null;
		startingPointCal = null;
		accountingLine = null;
	}

	@Test
	public void testGenerateEndDateDaily() {
		doTest(Calendar.DATE, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "1");
	}
	
	@Test
	public void testGenerateEndDateDailyTwice() {
		doTest(Calendar.DATE, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "2");
	}
	
	@Test
	public void testGenerateEndDateWeekly() {
		doTest(Calendar.DATE, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "1");
	}
	
	@Test
	public void testGenerateEndDateWeeklyTwice() {
		doTest(Calendar.DATE, 14, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "2");
	}
	
	@Test
	public void testGenerateEndDateWeeklyTrice() {
		doTest(Calendar.DATE, 21, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.BIWEEKLY, "3");
	}
	
	@Test
	public void testGenerateEndDateMonthly() {
		doTest(Calendar.MONTH, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "1");
	}
	
	@Test
	public void testGenerateEndDateMonthlyTwice() {
		doTest(Calendar.MONTH, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "2");
	}
	
	@Test
	public void testGenerateEndDateSemiMontlhy() {
		doTest(Calendar.YEAR, 0, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "1");
	}
	
	@Test
	public void testGenerateEndDateSemiMonthlyTwice() {
		doTest(Calendar.YEAR, 1, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "2");
	}
	
	private void doTest(int expectedCalendarType, int expectedAmount, CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes resultType, String resultAmount) {
		Date expectedDate = getExpectedDate(expectedCalendarType, expectedAmount);
		Date results = getDateResult(resultType, resultAmount);
		assertEquals("Day is not what we expected.", expectedDate.getDay(), results.getDay());
		assertEquals("Month is not what we expected.", expectedDate.getMonth(), results.getMonth());
		assertEquals("Year is not what we expected.", expectedDate.getYear(), results.getYear());
	}
	
	private Date getDateResult(ScheduleTypes scheduleType, String count) {
		accountingLine.setScheduleType(scheduleType.name);
		accountingLine.setPartialTransactionCount(count);;
		Date results = scheduledAccountingLineUtil.generateEndDate(accountingLine);
		return results;
	}
	
	private Date getExpectedDate(int dateAddElement, int addAmount) {
		Calendar calcCal = startingPointCal.getInstance();
		calcCal.add(dateAddElement, addAmount);
		Date expectedDate = new Date(calcCal.getTimeInMillis());
		return expectedDate;
	}

}
