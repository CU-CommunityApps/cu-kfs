package edu.cornell.kfs.fp.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;

public class CuDisbursementVoucherDefaultDueDateServiceImplTest {
    private CuDisbursementVoucherDefaultDueDateServiceImpl cuDisbursementVoucherDefaultDueDateServiceImpl;

    @BeforeEach
    public void setUp() throws Exception {
        Logger.getLogger(CuDisbursementVoucherDefaultDueDateServiceImpl.class.getName()).setLevel(Level.DEBUG);
        cuDisbursementVoucherDefaultDueDateServiceImpl = new CuDisbursementVoucherDefaultDueDateServiceImpl();
    }

    @AfterEach
    public void tearDown() throws Exception {
        cuDisbursementVoucherDefaultDueDateServiceImpl = null;
    }

    @Test
    public void testFindDefaultDueDateToday() throws Exception {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("1", LocalDate.now());
        LocalDate expectedDate = LocalDate.now();
        expectedDate = expectedDate.plusDays(1);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateLeapYear() throws Exception {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("3", LocalDate.of(2020, Month.FEBRUARY, 28));
        LocalDate expectedDate = LocalDate.of(2020, Month.MARCH, 2);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateNewYear() throws Exception {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("5", LocalDate.of(2020, Month.DECEMBER, 28));
        LocalDate expectedDate = LocalDate.of(2021, Month.JANUARY, 2);

        assertServiceDateEqualsExpectedDate(expectedDate);
    }

    @Test
    public void testFindDefaultDueDateBadParamString() throws Exception {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl("foo", LocalDate.of(2020, Month.DECEMBER, 28));
        assertThrows(IllegalStateException.class,
                () -> cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate(),
                "This should have generated an IllegalStateException");
    }

    @Test
    public void testFindDefaultDueDateBadParamEmptyNull() throws Exception {
        prepCuDisbursementVoucherDefaultDueDateServiceImpl(null, LocalDate.of(2020, Month.DECEMBER, 28));
        assertThrows(IllegalStateException.class,
                () -> cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate(),
                "This should have generated an IllegalStateException");
    }

    protected void assertServiceDateEqualsExpectedDate(LocalDate expectedDate) {
        Date actualDate = cuDisbursementVoucherDefaultDueDateServiceImpl.findDefaultDueDate();
        assertEquals(java.sql.Date.valueOf(expectedDate), actualDate);
    }

    protected void prepCuDisbursementVoucherDefaultDueDateServiceImpl(String numberOfDays, LocalDate currentDate) throws Exception {
        cuDisbursementVoucherDefaultDueDateServiceImpl.setDateTimeService(buildSpiedDateTimeService(currentDate));
        cuDisbursementVoucherDefaultDueDateServiceImpl.setParameterService(buildMockParameterService(numberOfDays));
    }

    protected ParameterService buildMockParameterService(String numberOfDays) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(DisbursementVoucherDocument.class,
                CuFPParameterConstants.DisbursementVoucherDocument.NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE))
                .thenReturn(numberOfDays);
        return service;
    }

    private DateTimeService buildSpiedDateTimeService(LocalDate currentDate) throws Exception{
        TestDateTimeServiceImpl dateTimeService = Mockito.spy(buildDateTimeService());
        Mockito.doAnswer(invocation -> currentDate).when(dateTimeService)
                .getLocalDateNow();

        return dateTimeService;
    }

    private TestDateTimeServiceImpl buildDateTimeService() throws Exception {
        TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        return dateTimeService;
    }

}
