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

	@Test
	public void testGenerateEndDateDaily() {
		Date expectedDate = getExpectedDate(Calendar.DATE, 0);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "1");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateDailyTwice() {
		Date expectedDate = getExpectedDate(Calendar.DATE, 1);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.DAILY, "2");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateWeekly() {
		Date expectedDate = getExpectedDate(Calendar.DATE, 0);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.WEEKLY, "1");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateWeeklyTwice() {
		Date expectedDate = getExpectedDate(Calendar.DATE, 7);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.WEEKLY, "2");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateWeeklyTrice() {
		Date expectedDate = getExpectedDate(Calendar.DATE, 14);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.WEEKLY, "3");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateMonthly() {
		Date expectedDate = getExpectedDate(Calendar.MONTH, 0);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "1");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateMonthlyTwice() {
		Date expectedDate = getExpectedDate(Calendar.MONTH, 1);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY, "2");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateYearly() {
		Date expectedDate = getExpectedDate(Calendar.YEAR, 0);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "1");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}
	
	@Test
	public void testGenerateEndDateYearlyTwice() {
		Date expectedDate = getExpectedDate(Calendar.YEAR, 1);
		Date results = getDateResult(CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.YEARLY, "2");
		assertEquals("Dates aren't what we expected.", expectedDate, results);
	}

}
