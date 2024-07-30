package edu.cornell.kfs.fp.document.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;

public class CuDisbursementVoucherDefaultDueDateServiceImplTest {
    private CuDisbursementVoucherDefaultDueDateServiceImpl cuDisbursementVoucherDefaultDueDateServiceImpl;
    private TestDateTimeServiceImpl testDateTimeService;

    @BeforeEach
    public void setUp() throws Exception {
        Logger.getLogger(CuDisbursementVoucherDefaultDueDateServiceImpl.class.getName()).setLevel(Level.DEBUG);
        cuDisbursementVoucherDefaultDueDateServiceImpl = new CuDisbursementVoucherDefaultDueDateServiceImpl();
        testDateTimeService = buildDateTimeService();
    }

    @AfterEach
    public void tearDown() throws Exception {
        cuDisbursementVoucherDefaultDueDateServiceImpl = null;
        testDateTimeService = null;
    }

    @Test
    public void testFindDefaultDueDateToday() {
        cuDisbursementVoucherDefaultDueDateServiceImpl.setDateTimeService(buildSpiedDateTimeService(testDateTimeService,
                LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth()));
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("1");
        LocalDate expectedDate = LocalDate.now();
        expectedDate = expectedDate.plusDays(1);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateLeapYear() {
        cuDisbursementVoucherDefaultDueDateServiceImpl
                .setDateTimeService(buildSpiedDateTimeService(testDateTimeService, 2020, Month.FEBRUARY, 28));
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("3");
        LocalDate expectedDate = LocalDate.of(2020, Month.MARCH, 2);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateNewYear() {
        cuDisbursementVoucherDefaultDueDateServiceImpl
                .setDateTimeService(buildSpiedDateTimeService(testDateTimeService, 2020, Month.DECEMBER, 28));
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("5");
        LocalDate expectedDate = LocalDate.of(2021, Month.JANUARY, 2);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateBadParamString() {
        cuDisbursementVoucherDefaultDueDateServiceImpl
                .setDateTimeService(buildSpiedDateTimeService(testDateTimeService, 2020, Month.DECEMBER, 28));

        prepCuDisbursementVoucherDefaultDueDateServiceImpl("foo");
        boolean caughtError = false;
        try {
            Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        } catch (IllegalStateException il) {
            caughtError = true;
        }
        assertTrue("This should have generated an IllegalStateException", caughtError);
    }

    @Test
    public void testFindDefaultDueDateBadParamEmptyNull() {
        cuDisbursementVoucherDefaultDueDateServiceImpl
                .setDateTimeService(buildSpiedDateTimeService(testDateTimeService, 2020, Month.DECEMBER, 28));

        prepCuDisbursementVoucherDefaultDueDateServiceImpl(null);
        boolean caughtError = false;
        try {
            Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        } catch (IllegalStateException il) {
            caughtError = true;
        }
        assertTrue("This should have generated an IllegalStateException", caughtError);
    }

    protected void assertServiceDateEqualsExpectedDate(LocalDate expectedDate) {
        Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        assertEquals(java.sql.Date.valueOf(expectedDate), actualDate);
    }

    protected void prepCuDisbursementVoucherDefaultDueDateServiceImpl(String numberOfDays) {
        cuDisbursementVoucherDefaultDueDateServiceImpl.setParameterService(buildMockParameterService(numberOfDays));
    }

    protected ParameterService buildMockParameterService(String numberOfDays) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(DisbursementVoucherDocument.class,
                CuFPParameterConstants.DisbursementVoucherDocument.NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE))
                .thenReturn(numberOfDays);
        return service;
    }

    protected DateTimeService buildMockDateTimeService(Calendar currentCalendar) {
        DateTimeService service = Mockito.mock(DateTimeService.class);
        Mockito.when(service.getCurrentCalendar()).thenReturn(currentCalendar);
        return service;

    }

    private java.sql.Date getSqlDate(InvocationOnMock invocation) {
        java.time.LocalDate p = invocation.getArgument(0);
        return java.sql.Date.valueOf(p);
    }

    private DateTimeService buildSpiedDateTimeService(TestDateTimeServiceImpl actualDateTimeService, int year,
            Month month, int dayOfMonth) {
        TestDateTimeServiceImpl dateTimeService = Mockito.spy(actualDateTimeService);
        Mockito.doAnswer(invocation -> getMockLocalDateNow(year, month, dayOfMonth)).when(dateTimeService)
                .getLocalDateNow();
        Mockito.doAnswer(invocation -> getSqlDate(invocation)).when(dateTimeService).getSqlDate(any(LocalDate.class));

        return dateTimeService;
    }

    private LocalDate getMockLocalDateNow(int year, Month month, int dayOfMonth) {
        if (year <= 0 || month == null || dayOfMonth <= 0) {
            throw new IllegalStateException("The mocked current local date setting may not have been initialized");
        }
        return LocalDate.of(year, month, dayOfMonth);
    }

    private TestDateTimeServiceImpl buildDateTimeService() throws Exception {
        TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        return dateTimeService;
    }

}
