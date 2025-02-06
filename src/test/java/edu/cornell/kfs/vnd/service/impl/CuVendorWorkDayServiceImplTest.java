package edu.cornell.kfs.vnd.service.impl;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;

class CuVendorWorkDayServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final String WORKDAY_URL_STARTER = "https://workday.cornell.edu/service?";
    private static final String WORKDAY_URL_MIDDLE = "Include_Terminated_Workers=1&Social_Security_Number=";
    private static final String WORKDAY_URL_END = "&format=json";
    private static final String WORKDAY_CREDENTIAL_VALE = "username:password";

    private CuVendorWorkDayServiceImpl cuVendorWorkDayService;

    @BeforeEach
    void setUp() throws Exception {
        cuVendorWorkDayService = new CuVendorWorkDayServiceImpl();
        cuVendorWorkDayService.setParameterService(buildMockParameterService());
        cuVendorWorkDayService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
    }

    @AfterEach
    void tearDown() throws Exception {
        cuVendorWorkDayService = null;
    }

    private ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_ENDPOINT)).thenReturn(WORKDAY_URL_STARTER);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_INCLUDE_TERMINDATED_WORKERS)).thenReturn("1");
        return service;
    }
    
    public WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService service = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(service.getWebServiceCredentialValue(CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_GROUP_CODE,
                CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_KEY)).thenReturn(WORKDAY_CREDENTIAL_VALE);
        return service;
    }

    @Test
    void testBuildWorkdayServiceCall() {
        String ssn = "xyz";
        String actualResults = cuVendorWorkDayService.buildWorkdayServiceCall(ssn);
        String expectedResults = WORKDAY_URL_STARTER + WORKDAY_URL_MIDDLE + ssn + WORKDAY_URL_END;
        assertEquals(expectedResults, actualResults);
    }

}
