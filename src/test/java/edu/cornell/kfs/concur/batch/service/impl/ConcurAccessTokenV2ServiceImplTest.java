package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.CredentialTestValues;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.util.MockConcurUtils;
import edu.cornell.kfs.concur.web.mock.MockConcurAuthenticationEndpointController;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurAccessTokenV2ServiceImplTest {

    private static final String INVALID_GEOLOCATION_SUFFIX = "/concur.cornell.edu";

    @RegisterExtension
    MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private WebServiceCredentialService mockWebServiceCredentialService;
    private ConcurBatchUtilityService mockConcurBatchUtilityService;
    private ConcurAccessTokenV2ServiceImpl concurAccessTokenV2Service;
    private MockConcurAuthenticationEndpointController mockEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        String defaultRefreshToken = CredentialTestValues.REFRESH_TOKEN;
        String defaultGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX;
        this.mockWebServiceCredentialService = createMockWebServiceCredentialService(defaultRefreshToken);
        this.mockConcurBatchUtilityService = createMockConcurBatchUtilityService(defaultGeolocation);
        
        this.concurAccessTokenV2Service = new ConcurAccessTokenV2ServiceImpl();
        concurAccessTokenV2Service.setWebServiceCredentialService(mockWebServiceCredentialService);
        concurAccessTokenV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        concurAccessTokenV2Service.setConcurGeolocationUrlPattern(
                ConcurTestConstants.CONCUR_GEOLOCATION_PATTERN_LOCALHOST);
        
        this.mockEndpoint = new MockConcurAuthenticationEndpointController();
        mockEndpoint.setCurrentRefreshToken(defaultRefreshToken);
        mockEndpoint.setCurrentGeolocation(defaultGeolocation);
        
        webServerExtension.initializeStandaloneMockMvcWithControllers(mockEndpoint);
    }

    private WebServiceCredentialService createMockWebServiceCredentialService(String defaultRefreshToken) {
        return MockConcurUtils.createMockWebServiceCredentialServiceBackedByCredentials(
                WebServiceCredentialKeys.GROUP_CODE,
                Map.entry(WebServiceCredentialKeys.CLIENT_ID, CredentialTestValues.CLIENT_ID),
                Map.entry(WebServiceCredentialKeys.SECRET_ID, CredentialTestValues.CLIENT_SECRET),
                Map.entry(WebServiceCredentialKeys.USER_NAME, CredentialTestValues.USERNAME),
                Map.entry(WebServiceCredentialKeys.REQUEST_TOKEN, CredentialTestValues.PASSWORD),
                Map.entry(WebServiceCredentialKeys.REFRESH_TOKEN, defaultRefreshToken));
    }

    private ConcurBatchUtilityService createMockConcurBatchUtilityService(String defaultGeolocation) {
        return MockConcurUtils.createMockConcurBatchUtilityServiceBackedByParameters(
                Map.entry(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, String.valueOf(1)),
                Map.entry(ConcurParameterConstants.CONCUR_ACCESS_TOKEN_ENDPOINT,
                        ConcurTestConstants.CONCUR_TOKEN_API_PATH),
                Map.entry(ConcurParameterConstants.CONCUR_GEOLOCATION_URL, defaultGeolocation));
    }

    @AfterEach
    void tearDown() throws Exception {
        mockEndpoint = null;
        concurAccessTokenV2Service = null;
        mockConcurBatchUtilityService = null;
        mockWebServiceCredentialService = null;
    }

    @Test
    void testRetrieveAccessToken() throws Exception {
        assertAccessTokenRetrievalSucceeds(false, false);
    }

    @Test
    void testRetrieveAccessTokenAndNewGeolocation() throws Exception {
        String oldGeolocation = getCurrentGeolocation();
        String newGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2;
        
        mockEndpoint.setCurrentGeolocation(newGeolocation);
        assertAccessTokenRetrievalSucceeds(false, true);
        
        setCurrentGeolocation(oldGeolocation);
        assertAccessTokenRetrievalFails();
        
        setCurrentGeolocation(newGeolocation);
        assertAccessTokenRetrievalSucceeds(false, false);
    }

    @Test
    void testRetrieveAccessTokenAndNewRefreshToken() throws Exception {
        testRetrieveAccessTokenAndNewRefreshToken(false);
    }

    @Test
    void testRetrieveAccessTokenAndNewRefreshTokenAndNewGeolocation() throws Exception {
        String newGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2;
        mockEndpoint.setCurrentGeolocation(newGeolocation);
        testRetrieveAccessTokenAndNewRefreshToken(true);
    }

    private void testRetrieveAccessTokenAndNewRefreshToken(boolean alsoExpectNewGeolocation) {
        String oldRefreshToken = getCurrentRefreshToken();
        String newRefreshToken;
        
        mockEndpoint.setForceNewRefreshToken(true);
        assertAccessTokenRetrievalSucceeds(true, alsoExpectNewGeolocation);
        
        newRefreshToken = getCurrentRefreshToken();
        mockEndpoint.setForceNewRefreshToken(false);
        setCurrentRefreshToken(oldRefreshToken);
        assertAccessTokenRetrievalFails();
        
        setCurrentRefreshToken(newRefreshToken);
        assertAccessTokenRetrievalSucceeds(false, false);
    }

    @Test
    void testInternalServerErrorWhenRetrievingAccessToken() throws Exception {
        mockEndpoint.setForceInternalServerError(true);
        assertAccessTokenRetrievalFails();
    }

    static Stream<Arguments> invalidCredentialsForRetrievingAccessToken() {
        return Stream.of(
                Arguments.of(WebServiceCredentialKeys.CLIENT_ID, "AAAAAAAbbCCCC"),
                Arguments.of(WebServiceCredentialKeys.SECRET_ID, "ZYXzyxZYXzyx"),
                Arguments.of(WebServiceCredentialKeys.REFRESH_TOKEN, "999999888888777777666666")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialsForRetrievingAccessToken")
    void testBadClientRequestWhenRetrievingAccessToken(String credentialKey, String credentialValue) throws Exception {
        mockWebServiceCredentialService.updateWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, credentialKey, credentialValue);
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testCannotRetrieveAccessTokenFromInvalidEndpoint() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        setCurrentGeolocation(invalidGeolocation);
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testInvalidGeolocationReturnedFromServerWhenRetrievingAccessToken() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertAccessTokenRetrievalFails();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            KFSConstants.EMPTY_STRING,
            KFSConstants.BLANK_SPACE
    })
    void testBlankGeolocationReturnedFromServerWhenRetrievingAccessToken(String invalidGeolocation) throws Exception {
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testRetrieveNewRefreshToken() throws Exception {
        assertNewRefreshTokenRetrievalSucceeds(false);
    }

    @Test
    void testRetrieveNewRefreshTokenAndNewGeolocation() throws Exception {
        String oldGeolocation = getCurrentGeolocation();
        String newGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2;
        
        mockEndpoint.setCurrentGeolocation(newGeolocation);
        assertNewRefreshTokenRetrievalSucceeds(true);
        
        setCurrentGeolocation(oldGeolocation);
        assertNewRefreshTokenRetrievalFails();
        
        setCurrentGeolocation(newGeolocation);
        assertNewRefreshTokenRetrievalSucceeds(false);
    }

    @Test
    void testClientErrorWhenRequestForNewRefreshTokenReturnsExistingToken() throws Exception {
        assertNewRefreshTokenRetrievalFails(false);
    }

    @Test
    void testInternalServerErrorWhenRetrievingNewRefreshToken() throws Exception {
        mockEndpoint.setForceInternalServerError(true);
        assertNewRefreshTokenRetrievalFails();
    }

    static Stream<Arguments> invalidCredentialsForRetrievingNewRefreshToken() {
        return Stream.of(
                Arguments.of(WebServiceCredentialKeys.CLIENT_ID, "AAAAAAAbbCCCC"),
                Arguments.of(WebServiceCredentialKeys.SECRET_ID, "ZYXzyxZYXzyx"),
                Arguments.of(WebServiceCredentialKeys.USER_NAME, "john-doe"),
                Arguments.of(WebServiceCredentialKeys.REQUEST_TOKEN, "BAD-pass-WoRd")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCredentialsForRetrievingNewRefreshToken")
    void testBadClientRequestWhenRetrievingNewRefreshToken(String credentialKey, String credentialValue)
            throws Exception {
        mockWebServiceCredentialService.updateWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, credentialKey, credentialValue);
        assertNewRefreshTokenRetrievalFails();
    }

    @Test
    void testCannotRetrieveNewRefreshTokenFromInvalidEndpoint() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        setCurrentGeolocation(invalidGeolocation);
        assertNewRefreshTokenRetrievalFails();
    }

    @Test
    void testInvalidGeolocationReturnedFromServerWhenRetrievingNewRefreshToken() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertNewRefreshTokenRetrievalFails();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            KFSConstants.EMPTY_STRING,
            KFSConstants.BLANK_SPACE
    })
    void testBlankGeolocationReturnedFromServerWhenRetrievingNewRefreshToken(String invalidGeolocation)
            throws Exception {
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertNewRefreshTokenRetrievalFails();
    }

    private void assertAccessTokenRetrievalSucceeds(boolean expectRefreshTokenToChange,
            boolean expectGeolocationToChange) {
        String oldGeolocation = getCurrentGeolocation();
        String oldRefreshToken = getCurrentRefreshToken();
        
        String newAccessToken = concurAccessTokenV2Service.retrieveNewAccessBearerToken();
        
        String newGeolocation = getCurrentGeolocation();
        String newRefreshToken = getCurrentRefreshToken();
        
        assertTrue(mockEndpoint.isAccessTokenActive(newAccessToken),
                "The retrieved access token does not match the one recorded by the mock server");
        assertEquals(mockEndpoint.getCurrentRefreshToken(), newRefreshToken,
                "Mismatched refresh tokens between client and server");
        assertEquals(mockEndpoint.getCurrentGeolocation(), newGeolocation,
                "Mismatched geolocations between client and server");
        
        if (expectRefreshTokenToChange) {
            assertNotEquals(oldRefreshToken, newRefreshToken, "A new refresh token should have been returned");
        } else {
            assertEquals(oldRefreshToken, newRefreshToken, "The refresh token in storage should not have changed");
        }
        
        if (expectGeolocationToChange) {
            assertNotEquals(oldGeolocation, newGeolocation, "A new geolocation should have been returned");
        } else {
            assertEquals(oldGeolocation, newGeolocation, "The geolocation in storage should not have changed");
        }
    }

    private void assertAccessTokenRetrievalFails() {
        String oldRefreshToken = getCurrentRefreshToken();
        String oldGeolocation = getCurrentGeolocation();
        assertThrows(RuntimeException.class, () -> concurAccessTokenV2Service.retrieveNewAccessBearerToken(),
                "The service should have failed to retrieve a new access token");
        assertEquals(oldRefreshToken, getCurrentRefreshToken(),
                "The stored refresh token should not have changed after failing to retrieve a new access token");
        assertEquals(oldGeolocation, getCurrentGeolocation(),
                "The stored geolocation should not have changed after failing to retrieve a new access token");
    }

    private void assertNewRefreshTokenRetrievalSucceeds(boolean expectGeolocationToChange) {
        String oldGeolocation = getCurrentGeolocation();
        String oldRefreshToken = getCurrentRefreshToken();
        mockEndpoint.setForceNewRefreshToken(true);
        
        concurAccessTokenV2Service.retrieveAndPersistNewRefreshToken();
        
        String newGeolocation = getCurrentGeolocation();
        String newRefreshToken = getCurrentRefreshToken();
        
        assertEquals(mockEndpoint.getCurrentRefreshToken(), newRefreshToken,
                "Mismatched refresh tokens between client and server");
        assertEquals(mockEndpoint.getCurrentGeolocation(), newGeolocation,
                "Mismatched geolocations between client and server");
        assertNotEquals(oldRefreshToken, newRefreshToken, "A new refresh token should have been returned");
        
        if (expectGeolocationToChange) {
            assertNotEquals(oldGeolocation, newGeolocation, "A new geolocation should have been returned");
        } else {
            assertEquals(oldGeolocation, newGeolocation, "The geolocation in storage should not have changed");
        }
    }

    private void assertNewRefreshTokenRetrievalFails() {
        assertNewRefreshTokenRetrievalFails(true);
    }

    private void assertNewRefreshTokenRetrievalFails(boolean forceNewRefreshToken) {
        String oldGeolocation = getCurrentGeolocation();
        String oldRefreshToken = getCurrentRefreshToken();
        mockEndpoint.setForceNewRefreshToken(forceNewRefreshToken);
        assertThrows(RuntimeException.class, () -> concurAccessTokenV2Service.retrieveAndPersistNewRefreshToken(),
                "The service should have failed to retrieve a new refresh token");
        assertEquals(oldRefreshToken, getCurrentRefreshToken(),
                "The stored refresh token should not have changed after failing to retrieve a new refresh token");
        assertEquals(oldGeolocation, getCurrentGeolocation(),
                "The stored geolocation should not have changed after failing to retrieve a new refresh token");
    }

    private String getCurrentRefreshToken() {
        return mockWebServiceCredentialService.getWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, WebServiceCredentialKeys.REFRESH_TOKEN);
    }

    private void setCurrentRefreshToken(String currentRefreshToken) {
        mockWebServiceCredentialService.updateWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, WebServiceCredentialKeys.REFRESH_TOKEN, currentRefreshToken);
    }

    private String getCurrentGeolocation() {
        return mockConcurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_GEOLOCATION_URL);
    }

    private void setCurrentGeolocation(String currentGeolocation) {
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.CONCUR_GEOLOCATION_URL, currentGeolocation);
    }

}
