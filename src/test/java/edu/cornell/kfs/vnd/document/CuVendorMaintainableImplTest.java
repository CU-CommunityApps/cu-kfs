package edu.cornell.kfs.vnd.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.mockito.Mockito;

import edu.cornell.kfs.vnd.CuVendorParameterConstants;
import edu.cornell.kfs.vnd.document.fixture.WorkdayKfsVendorLookupRootEnum;

@Execution(SAME_THREAD)
public class CuVendorMaintainableImplTest {
    public static final int TERMINATION_DAYS_IN_THE_PAST = 5;

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
    @EnumSource
    public void isActiveEmployee(WorkdayKfsVendorLookupRootEnum rootEnum) {
        boolean actualActive = vendorMaintainable.isActiveEmployee(rootEnum.lookupRoot);
        assertEquals(rootEnum.expectedActive, actualActive);
    }
    
    @ParameterizedTest
    @EnumSource(
            value = WorkdayKfsVendorLookupRootEnum.class,
            names = {"EMPTY_LIST"},
            mode = EnumSource.Mode.EXCLUDE)
    public void isTerminatedWithinDateRange(WorkdayKfsVendorLookupRootEnum rootEnum) {
        boolean actualTerminatedInRange = vendorMaintainable.isTerminatedWithinDateRange(rootEnum.lookupRoot.getResults().get(0));
        assertEquals(rootEnum.expectedTerminatedInRange, actualTerminatedInRange);
    }
    
    @ParameterizedTest
    @EnumSource
    public void isActiveOrTerminatedEmployeeWithinDateRange(WorkdayKfsVendorLookupRootEnum rootEnum) {
        boolean actualActiveOrTerminatedInRange = vendorMaintainable.isActiveOrTerminatedEmployeeWithinDateRange(rootEnum.lookupRoot);
        assertEquals(rootEnum.expectedActiveOrTerminatedInRange, actualActiveOrTerminatedInRange);
    }
}
