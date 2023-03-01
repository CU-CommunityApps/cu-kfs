package org.kuali.kfs.module.ar.businessobject;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.module.ar.ArConstants.BillingFrequencyValues;
import org.mockito.Mockito;

public class BillingPeriodTest {

    private BillingPeriod billingPeriod;
    private BillingFrequencyValues billingFrequency;
    private Date awardStartDate;
    private Date currentDate;
    private AccountingPeriodService accountingPeriodService;

    @Before
    public void setUp() {
        billingFrequency = BillingFrequencyValues.MONTHLY;
        awardStartDate = buildDate(2018, Calendar.JANUARY, 1);
        currentDate = new Date(Calendar.getInstance().getTimeInMillis());
        accountingPeriodService = Mockito.mock(AccountingPeriodService.class);
    }

    @After
    public void tearDown() throws Exception {
        billingPeriod = null;
        billingFrequency = null;
        awardStartDate = null;
        currentDate = null;
        accountingPeriodService = null;
    }

    @Test
    public void testDetermineStartDateByFrequencyBasic() {
        Date lastBilledDate = buildDate(2018, Calendar.NOVEMBER, 1);
        Date expectedNextDate = buildDate(2018, Calendar.NOVEMBER, 2);
        assertEquals(expectedNextDate.toString(), findNextBillingDate(lastBilledDate).toString());
    }

    @Test
    public void testDetermineStartDateByFrequencyNewYears() {
        Date lastBilledDate = buildDate(2018, Calendar.DECEMBER, 31);
        Date expectedNextDate = buildDate(2019, Calendar.JANUARY, 1);
        assertEquals(expectedNextDate.toString(), findNextBillingDate(lastBilledDate).toString());
    }

    @Test
    public void testDetermineStartDateByFrequencyLeapYear() {
        Date lastBilledDate = buildDate(2020, Calendar.FEBRUARY, 28);
        Date expectedNextDate = buildDate(2020, Calendar.FEBRUARY, 29);
        assertEquals(expectedNextDate.toString(), findNextBillingDate(lastBilledDate).toString());
    }

    @Test
    public void testDetermineStartDateByFrequencyNonLeapYear() {
        Date lastBilledDate = buildDate(2021, Calendar.FEBRUARY, 28);
        Date expectedNextDate = buildDate(2021, Calendar.MARCH, 1);
        assertEquals(expectedNextDate.toString(), findNextBillingDate(lastBilledDate).toString());
    }

    @Test
    public void testDetermineStartDateByFrequencyNullLastBilledDate() {
        Date lastBilledDate = null;
        Date expectedNextDate = awardStartDate;
        assertEquals(expectedNextDate.toString(), findNextBillingDate(lastBilledDate).toString());
    }

    private Date buildDate(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, date);
        return new Date(calendar.getTimeInMillis());
    }

    private Date findNextBillingDate(Date lastBilledDate) {
        billingPeriod = new BillingPeriod(billingFrequency, awardStartDate, currentDate,
                lastBilledDate, accountingPeriodService);
        return billingPeriod.determineStartDateByFrequency();
    }

}