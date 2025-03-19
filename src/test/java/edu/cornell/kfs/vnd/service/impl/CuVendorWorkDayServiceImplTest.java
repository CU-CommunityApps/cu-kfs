package edu.cornell.kfs.vnd.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;
import edu.cornell.kfs.vnd.service.impl.fixture.CuVendorWorkdayServiceEnum;

@Execution(SAME_THREAD)
public class CuVendorWorkDayServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();

    @RegisterExtension
    MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private static final String WORKDAY_URL_SERVICE_PATH = "/service/customreport2/cornell/intsys-HRIS/CRINT127C_KFS_Vendor_Lookup?";
    private static final String WORKDAY_URL_TERMINATED_WORKERS = "Include_Terminated_Workers=";
    private static final String WORKDAY_URL_SSN = "&Social_Security_Number=";
    private static final String WORKDAY_URL_END = "&format=json";
    private static final String WORKDAY_CREDENTIAL_VALUE = "username:password";

    private CuVendorWorkDayServiceImpl cuVendorWorkDayService;
    private MockWorkdayEndpointController workdayEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        cuVendorWorkDayService = new CuVendorWorkDayServiceImpl();
        cuVendorWorkDayService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        workdayEndpoint = new MockWorkdayEndpointController();
        webServerExtension.initializeStandaloneMockMvcWithControllers(workdayEndpoint);
    }

    @AfterEach
    void tearDown() throws Exception {
        cuVendorWorkDayService = null;
        workdayEndpoint = null;
    }

    private ParameterService buildMockParameterService(String terminatedWorkers) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_ENDPOINT)).thenReturn(buildWorkdayUrlStarter());
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_INCLUDE_TERMINATED_WORKERS)).thenReturn(terminatedWorkers);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_SERVICE_RETRY_COUNT)).thenReturn("2");
        return service;
    }

    private String buildWorkdayUrlStarter() {
        return webServerExtension.getServerUrl() + WORKDAY_URL_SERVICE_PATH;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService service = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(service.getWebServiceCredentialValue(
                CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_GROUP_CODE,
                CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_KEY)).thenReturn(WORKDAY_CREDENTIAL_VALUE);
        return service;
    }

    @ParameterizedTest
    @EnumSource
    public void testBuildWorkdayServiceCall(CuVendorWorkdayServiceEnum serviceValues) {
        cuVendorWorkDayService.setParameterService(buildMockParameterService(serviceValues.includeTerminatedWorkers));
        String actualResults = cuVendorWorkDayService.buildWorkdayServiceCall(serviceValues.ssn);
        LOG.info("testBuildWorkdayServiceCall, actualResultsL {}", actualResults);
        assertEquals(buildTestingServiceCallUrl(serviceValues.ssn, serviceValues.includeTerminatedWorkers),
                actualResults);
    }
    
    private String buildTestingServiceCallUrl(String ssn, String terminatedWorkers) {
        StringBuilder sb = new StringBuilder(buildWorkdayUrlStarter());
        sb.append(WORKDAY_URL_TERMINATED_WORKERS).append(terminatedWorkers);
        sb.append(WORKDAY_URL_SSN).append(ssn).append(WORKDAY_URL_END);
        return sb.toString();
    }

    @ParameterizedTest
    @EnumSource
    public void testBuildInvocation(CuVendorWorkdayServiceEnum serviceValues) throws URISyntaxException {
        cuVendorWorkDayService.setParameterService(buildMockParameterService(serviceValues.includeTerminatedWorkers));
        assertDoesNotThrow(() -> cuVendorWorkDayService.buildInvocation(serviceValues.ssn));
    }

    @ParameterizedTest
    @EnumSource(
            value = CuVendorWorkdayServiceEnum.class,
            names = {"SSN_WITH_HYPHENS_NULL_TERMINATED", "NULL_SSN_INCLUDE_TERMINATED", "NULL_SSN_NULL_TERMINATED"},
            mode = EnumSource.Mode.EXCLUDE)
    public void testFindEmployeeBySocialSecurityNumber(CuVendorWorkdayServiceEnum serviceEnum)
            throws URISyntaxException {
        cuVendorWorkDayService.setParameterService(buildMockParameterService(serviceEnum.includeTerminatedWorkers));
        WorkdayKfsVendorLookupRoot actualRoot = cuVendorWorkDayService
                .findEmployeeBySocialSecurityNumber(serviceEnum.ssn, serviceEnum.documentId);
        WorkdayKfsVendorLookupRoot expectedRoot = serviceEnum.toWorkdayKfsVendorLookupRoot();
        LOG.info("testFindEmployeeBySocialSecurityNumber, actualRoot: {}", actualRoot.toString());
        assertEquals(expectedRoot, actualRoot);
        assertEquals(serviceEnum.activeEmployee, actualRoot.isActiveEmployee());
        assertEquals(serviceEnum.activeOrInactiveEmployee, actualRoot.isActiveOrInactiveEmployee());
    }
    
    @Test
    public void testBuildAuthenticationValue() {
        String actualResults = cuVendorWorkDayService.buildAuthenticationValue();
        assertEquals(buildTestingAuthenticationValue(), actualResults);
    }

    private String buildTestingAuthenticationValue() {
        byte[] byteArrayEncodedCreds = Base64.encodeBase64(WORKDAY_CREDENTIAL_VALUE.getBytes());
        String stringEncodedCreds = new String(byteArrayEncodedCreds, StandardCharsets.UTF_8);
        String expectedResults = CUKFSConstants.BASIC_AUTHENTICATION_STARTER + stringEncodedCreds;
        return expectedResults;
    }

}
