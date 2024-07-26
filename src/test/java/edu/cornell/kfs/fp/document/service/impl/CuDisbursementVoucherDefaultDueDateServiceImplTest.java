package edu.cornell.kfs.fp.document.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CuDisbursementVoucherDefaultDueDateServiceImplTest {
    private CuDisbursementVoucherDefaultDueDateServiceImpl cuDisbursementVoucherDefaultDueDateServiceImpl;
    private SimpleDateFormat dateFormat;

    @Before
    public void setUp() throws Exception {
        Logger.getLogger(CuDisbursementVoucherDefaultDueDateServiceImpl.class.getName()).setLevel(Level.DEBUG);
        cuDisbursementVoucherDefaultDueDateServiceImpl = new CuDisbursementVoucherDefaultDueDateServiceImpl();
        dateFormat = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd, Locale.US);
    }

    @After
    public void tearDown() throws Exception {
        cuDisbursementVoucherDefaultDueDateServiceImpl = null;
        dateFormat = null;
    }

    @Test
    public void testFindDefaultDueDateToday() {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("1");
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.add(Calendar.DAY_OF_MONTH, 1);

        assertServiceDateEqualsExpectedDate(new Date(expectedCalendar.getTimeInMillis()));
    }
    

    
    @Test
    public void testFindDefaultDueDateBadParamString() {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("foo");
        boolean caughtError = false;
        try {
            Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        } catch (IllegalStateException il) {
            caughtError = true;
        }
        assertTrue("This should have generated a NumberFormatException", caughtError);
    }
    
    @Test
    public void testFindDefaultDueDateBadParamEmptyNull() {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl(null);
        boolean caughtError = false;
        try {
            Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        } catch (IllegalStateException il) {
            caughtError = true;
        }
        assertTrue("This should have generated an IllegalStateException", caughtError);
    }
    
    protected Calendar buildCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }
    
    protected void assertServiceDateEqualsExpectedDate(Date expectedDate) {
        Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        assertEquals(dateFormat.format(expectedDate), dateFormat.format(actualDate));
    }
    
    protected void prepCuDisbursementVoucherDefaultDueDateServiceImpl(String numberOfDays) {
        cuDisbursementVoucherDefaultDueDateServiceImpl.setDateTimeService(buildMockDateTimeService());
        cuDisbursementVoucherDefaultDueDateServiceImpl.setParameterService(buildMockParameterService(numberOfDays));
    }
    
    protected ParameterService buildMockParameterService(String numberOfDays) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(DisbursementVoucherDocument.class, 
                CuFPParameterConstants.DisbursementVoucherDocument.NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE)).thenReturn(numberOfDays);
        return service;
    }
    
    protected DateTimeService buildMockDateTimeService() {
        DateTimeService service = Mockito.mock(DateTimeService.class);
        Mockito.when(service.getLocalDateNow()).thenReturn(java.time.LocalDate.now(Clock.systemDefaultZone()));
        when(service.getSqlDate(any(LocalDate.class))).then(this::getSqlDate);
        
        return service;
        
    }
    
    private java.sql.Date getSqlDate(InvocationOnMock invocation) {
        java.time.LocalDate p = invocation.getArgument(0);
        return java.sql.Date.valueOf(p);
    }

}
