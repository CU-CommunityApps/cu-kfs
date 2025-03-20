package edu.cornell.kfs.vnd.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.util.Calendar;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

@Execution(SAME_THREAD)
public class CuVendorMaintainableImplTest {
    private static final int TERMINATION_DAYS_IN_THE_PAST = 5;

    private CuVendorMaintainableImpl vendorMaintainable;

    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(CuVendorMaintainableImpl.class.getName(), Level.DEBUG);
        vendorMaintainable = new CuVendorMaintainableImpl();
        vendorMaintainable.setParameterService(buildMockParameterService());
    }

    private ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(VendorDetail.class,
                CuVendorParameterConstants.EMPLOYEE_TERMINATION_NUMBER_OF_DAYS_FOR_TAX_ID_REVIEW))
                .thenReturn(String.valueOf(TERMINATION_DAYS_IN_THE_PAST));
        return service;
    }

    @AfterEach
    public void tearDown() throws Exception {
        vendorMaintainable = null;
    }

    @ParameterizedTest
    @MethodSource("isActiveEmployeeParameters")
    public void isActiveEmployee(WorkdayKfsVendorLookupRoot root, boolean expectedActive) {
        boolean actualActive = vendorMaintainable.isActiveEmployee(root);
        assertEquals(expectedActive, actualActive);
    }
    
    private static Stream<Arguments> isActiveEmployeeParameters() {
        return Stream.of(
                Arguments.of(buildEmptyRoot(), false),
                Arguments.of(buildRootWithActiveResult(), true),
                Arguments.of(buildRootWithTerminatedEmployeeInRangeResult(), false), 
                Arguments.of(buildRootWithTerminatedEmployeeAtRangeResult(), false),
                Arguments.of(buildRootWithTerminatedEmployeeAfterRangeResult(), false)
        );
    }
    
    @ParameterizedTest
    @MethodSource("isTerminatedWithinDateRangeParameters")
    public void isTerminatedWithinDateRange(WorkdayKfsVendorLookupRoot root, boolean expectedTerminatedInRange) {
        boolean actualTerminated = vendorMaintainable.isTerminatedWithinDateRange(root.getResults().get(0));
        assertEquals(expectedTerminatedInRange, actualTerminated);
    }
    
    private static Stream<Arguments> isTerminatedWithinDateRangeParameters() {
        return Stream.of(
                Arguments.of(buildRootWithActiveResult(), false),
                Arguments.of(buildRootWithTerminatedEmployeeInRangeResult(), true), 
                Arguments.of(buildRootWithTerminatedEmployeeAtRangeResult(), true),
                Arguments.of(buildRootWithTerminatedEmployeeAfterRangeResult(), false)
        );
    }
    
    @ParameterizedTest
    @MethodSource("isActiveOrTerminatedEmployeeWithinDateRangeParameters")
    public void isActiveOrTerminatedEmployeeWithinDateRange(WorkdayKfsVendorLookupRoot root, boolean expectedActiveOrTerminatedInRange) {
        boolean actualActiveOrTerminatedInRange = vendorMaintainable.isActiveOrTerminatedEmployeeWithinDateRange(root);
        assertEquals(expectedActiveOrTerminatedInRange, actualActiveOrTerminatedInRange);
    }
    
    private static Stream<Arguments> isActiveOrTerminatedEmployeeWithinDateRangeParameters() {
        return Stream.of(
                Arguments.of(buildEmptyRoot(), false),
                Arguments.of(buildRootWithActiveResult(), true),
                Arguments.of(buildRootWithTerminatedEmployeeInRangeResult(), true), 
                Arguments.of(buildRootWithTerminatedEmployeeAtRangeResult(), true),
                Arguments.of(buildRootWithTerminatedEmployeeAfterRangeResult(), false)
        );
    }

    private static WorkdayKfsVendorLookupRoot buildEmptyRoot() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        return root;
    }

    private static WorkdayKfsVendorLookupRoot buildRootWithActiveResult() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus("1");
        root.getResults().add(result);
        return root;
    }

    private static WorkdayKfsVendorLookupRoot buildRootWithTerminatedEmployeeInRangeResult() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus("0");
        result.setTerminationDate(buildTerminationDate(TERMINATION_DAYS_IN_THE_PAST - 2));
        root.getResults().add(result);
        return root;
    }

    private static WorkdayKfsVendorLookupRoot buildRootWithTerminatedEmployeeAtRangeResult() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus("0");
        result.setTerminationDate(buildTerminationDate(TERMINATION_DAYS_IN_THE_PAST));
        root.getResults().add(result);
        return root;
    }

    private static WorkdayKfsVendorLookupRoot buildRootWithTerminatedEmployeeAfterRangeResult() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        WorkdayKfsVendorLookupResult result = new WorkdayKfsVendorLookupResult();
        result.setActiveStatus("0");
        result.setTerminationDate(buildTerminationDate(TERMINATION_DAYS_IN_THE_PAST + 5));
        root.getResults().add(result);
        return root;
    }

    private static String buildTerminationDate(int numberOfDays) {
        DateTimeService dateTimeService = new TestDateTimeServiceImpl();
        Calendar currentCalendar = dateTimeService.getCurrentCalendar();
        currentCalendar.add(Calendar.DATE, (-1 * numberOfDays));
        String dateString = dateTimeService.toString(currentCalendar.getTime(), CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
        return dateString;
    }

}
