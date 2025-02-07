package edu.cornell.kfs.vnd.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.vnd.CuVendorParameterConstants;

class CuVendorWorkDayServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    private static final String WORKDAY_URL_STARTER = "https://workday.cornell.edu/service?";
    private static final String WORKDAY_URL_TERMINATED_WORKERS = "Include_Terminated_Workers=";
    private static final String WORKDAY_URL_SSN = "&Social_Security_Number=";
    private static final String WORKDAY_URL_END = "&format=json";
    private static final String WORKDAY_CREDENTIAL_VALUE = "username:password";

    private CuVendorWorkDayServiceImpl cuVendorWorkDayService;

    @BeforeEach
    void setUp() throws Exception {
        cuVendorWorkDayService = new CuVendorWorkDayServiceImpl();
        cuVendorWorkDayService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
    }

    @AfterEach
    void tearDown() throws Exception {
        cuVendorWorkDayService = null;
    }

    private ParameterService buildMockParameterService(String terminatedWorkers) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_ENDPOINT)).thenReturn(WORKDAY_URL_STARTER);
        Mockito.when(service.getParameterValueAsString(CuVendorWorkDayServiceImpl.class,
                CuVendorParameterConstants.WORKDAY_INCLUDE_TERMINDATED_WORKERS)).thenReturn(terminatedWorkers);
        return service;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService service = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(service.getWebServiceCredentialValue(
                CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_GROUP_CODE,
                CuVendorParameterConstants.WORKDAY_WEBSERVICE_CREDENTIAL_KEY)).thenReturn(WORKDAY_CREDENTIAL_VALUE);
        return service;
    }

    @ParameterizedTest
    @MethodSource("workdayServiceArguemnts")
    public void testBuildWorkdayServiceCall(String ssn, String terminatedWorkers) {
        cuVendorWorkDayService.setParameterService(buildMockParameterService(terminatedWorkers));
        String actualResults = cuVendorWorkDayService.buildWorkdayServiceCall(ssn);
        LOG.info("testBuildWorkdayServiceCall, actualResultsL {}", actualResults);
        assertEquals(buildTestingServiceCallUrl(ssn, terminatedWorkers), actualResults);
    }

    @ParameterizedTest
    @MethodSource("workdayServiceArguemnts")
    public void testBuildInvocation(String ssn, String terminatedWorkers) throws URISyntaxException {
        cuVendorWorkDayService.setParameterService(buildMockParameterService(terminatedWorkers));
        assertDoesNotThrow(() -> cuVendorWorkDayService.buildInvocation(ssn));
    }

    private static Stream<Arguments> workdayServiceArguemnts() {
        return Stream.of(
                Arguments.of("000000000", "1"), 
                Arguments.of("111-22-3333", "1"),
                Arguments.of("111111111", "0"), 
                Arguments.of("222-77-9999", null), 
                Arguments.of(null, "1"),
                Arguments.of(StringUtils.EMPTY, "1"), 
                Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
                Arguments.of(null, null));
    }

    @Test
    public void testBuildAuthenticationValue() {
        String actualResults = cuVendorWorkDayService.buildAuthenticationValue();
        assertEquals(buildTestingAuthenticationValue(), actualResults);
    }

    private String buildTestingServiceCallUrl(String ssn, String terminatedWorkers) {
        String url = WORKDAY_URL_STARTER + WORKDAY_URL_TERMINATED_WORKERS + terminatedWorkers + WORKDAY_URL_SSN + ssn
                + WORKDAY_URL_END;
        return url;
    }

    private String buildTestingAuthenticationValue() {
        byte[] byteArrayEncodedCreds = Base64.encodeBase64(WORKDAY_CREDENTIAL_VALUE.getBytes());
        String stringEncodedCreds = new String(byteArrayEncodedCreds, StandardCharsets.UTF_8);
        String expectedResults = CUKFSConstants.BASIC_AUTHENTICATION_STARTER + stringEncodedCreds;
        return expectedResults;
    }

}
