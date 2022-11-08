package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.CredentialTestValues;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.web.mock.MockConcurAuthenticationEndpointController;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurAccessTokenV2ServiceImplTest {

    private static final String INVALID_GEOLOCATION_SUFFIX = "/concur.cornell.edu";

    @RegisterExtension
    MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private String currentRefreshToken;
    private String currentGeolocation;
    private WebServiceCredentialService mockWebServiceCredentialService;
    private ConcurAccessTokenV2ServiceImpl concurAccessTokenV2Service;
    private MockConcurAuthenticationEndpointController mockEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        this.currentRefreshToken = CredentialTestValues.REFRESH_TOKEN;
        this.currentGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX;
        this.mockWebServiceCredentialService = createMockWebServiceCredentialService();
        
        this.concurAccessTokenV2Service = new ConcurAccessTokenV2ServiceImpl();
        concurAccessTokenV2Service.setWebServiceCredentialService(mockWebServiceCredentialService);
        concurAccessTokenV2Service.setConcurBatchUtilityService(createMockConcurBatchUtilityService());
        concurAccessTokenV2Service.setConcurGeolocationUrlPattern(
                ConcurTestConstants.CONCUR_GEOLOCATION_PATTERN_LOCALHOST);
        
        this.mockEndpoint = new MockConcurAuthenticationEndpointController();
        mockEndpoint.setCurrentRefreshToken(currentRefreshToken);
        mockEndpoint.setCurrentGeolocation(currentGeolocation);
        
        webServerExtension.initializeStandaloneMockMvcWithControllers(mockEndpoint);
    }

    private WebServiceCredentialService createMockWebServiceCredentialService() {
        String[][] staticCredentials = {
                {WebServiceCredentialKeys.CLIENT_ID, CredentialTestValues.CLIENT_ID},
                {WebServiceCredentialKeys.SECRET_ID, CredentialTestValues.CLIENT_SECRET},
                {WebServiceCredentialKeys.USER_NAME, CredentialTestValues.USERNAME},
                {WebServiceCredentialKeys.REQUEST_TOKEN, CredentialTestValues.PASSWORD},
        };
        WebServiceCredentialService credentialService = Mockito.mock(WebServiceCredentialService.class);
        for (String[] staticCredential : staticCredentials) {
            Mockito.when(credentialService.getWebServiceCredentialValue(
                    WebServiceCredentialKeys.GROUP_CODE, staticCredential[0]))
                    .thenReturn(staticCredential[1]);
        }
        Mockito.when(credentialService.getWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, WebServiceCredentialKeys.REFRESH_TOKEN))
                .then(invocation -> currentRefreshToken);
        Mockito.doAnswer(invocation -> currentRefreshToken = invocation.getArgument(2))
                .when(credentialService).updateWebServiceCredentialValue(
                        Mockito.eq(WebServiceCredentialKeys.GROUP_CODE),
                        Mockito.eq(WebServiceCredentialKeys.REFRESH_TOKEN), Mockito.anyString());
        return credentialService;
    }

    private ConcurBatchUtilityService createMockConcurBatchUtilityService() {
        ConcurBatchUtilityService utilityService = Mockito.mock(ConcurBatchUtilityService.class);
        Mockito.when(utilityService.getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES))
                .thenReturn(String.valueOf(1));
        Mockito.when(utilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_ACCESS_TOKEN_ENDPOINT))
                .thenReturn(ConcurTestConstants.CONCUR_TOKEN_API_PATH);
        Mockito.when(utilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_GEOLOCATION_URL))
                .then(invocation -> currentGeolocation);
        Mockito.doAnswer(invocation -> currentGeolocation = invocation.getArgument(1))
                .when(utilityService).setConcurParameterValue(
                        Mockito.eq(ConcurParameterConstants.CONCUR_GEOLOCATION_URL), Mockito.anyString());
        return utilityService;
    }

    @AfterEach
    void tearDown() throws Exception {
        mockEndpoint = null;
        concurAccessTokenV2Service = null;
        mockWebServiceCredentialService = null;
        currentGeolocation = null;
        currentRefreshToken = null;
    }

    @Test
    void testRetrieveAccessToken() throws Exception {
        assertAccessTokenRetrievalSucceeds(false, false);
    }

    @Test
    void testRetrieveAccessTokenAndNewGeolocation() throws Exception {
        String oldGeolocation = currentGeolocation;
        String newGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2;
        
        mockEndpoint.setCurrentGeolocation(newGeolocation);
        assertAccessTokenRetrievalSucceeds(false, true);
        
        currentGeolocation = oldGeolocation;
        assertAccessTokenRetrievalFails();
        
        currentGeolocation = newGeolocation;
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
        String oldRefreshToken = currentRefreshToken;
        String newRefreshToken;
        
        mockEndpoint.setForceNewRefreshToken(true);
        assertAccessTokenRetrievalSucceeds(true, alsoExpectNewGeolocation);
        
        newRefreshToken = currentRefreshToken;
        mockEndpoint.setForceNewRefreshToken(false);
        currentRefreshToken = oldRefreshToken;
        assertAccessTokenRetrievalFails();
        
        currentRefreshToken = newRefreshToken;
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
        Mockito.when(mockWebServiceCredentialService.getWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, credentialKey))
                .thenReturn(credentialValue);
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testCannotRetrieveAccessTokenFromInvalidEndpoint() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        currentGeolocation = invalidGeolocation;
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testInvalidGeolocationReturnedFromServerWhenRetrievingAccessToken() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertAccessTokenRetrievalFails();
    }

    @Test
    void testRetrieveNewRefreshToken() throws Exception {
        assertNewRefreshTokenRetrievalSucceeds(false);
    }

    @Test
    void testRetrieveNewRefreshTokenAndNewGeolocation() throws Exception {
        String oldGeolocation = currentGeolocation;
        String newGeolocation = webServerExtension.getServerUrl()
                + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2;
        
        mockEndpoint.setCurrentGeolocation(newGeolocation);
        assertNewRefreshTokenRetrievalSucceeds(true);
        
        currentGeolocation = oldGeolocation;
        assertNewRefreshTokenRetrievalFails();
        
        currentGeolocation = newGeolocation;
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
        Mockito.when(mockWebServiceCredentialService.getWebServiceCredentialValue(
                WebServiceCredentialKeys.GROUP_CODE, credentialKey))
                .thenReturn(credentialValue);
        assertNewRefreshTokenRetrievalFails();
    }

    @Test
    void testCannotRetrieveNewRefreshTokenFromInvalidEndpoint() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        currentGeolocation = invalidGeolocation;
        assertNewRefreshTokenRetrievalFails();
    }

    @Test
    void testInvalidGeolocationReturnedFromServerWhenRetrievingNewRefreshToken() throws Exception {
        String invalidGeolocation = webServerExtension.getServerUrl() + INVALID_GEOLOCATION_SUFFIX;
        mockEndpoint.setCurrentGeolocation(invalidGeolocation);
        assertNewRefreshTokenRetrievalFails();
    }

    private void assertAccessTokenRetrievalSucceeds(boolean expectRefreshTokenToChange,
            boolean expectGeolocationToChange) {
        String oldGeolocation = currentGeolocation;
        String oldRefreshToken = currentRefreshToken;
        String newAccessToken = concurAccessTokenV2Service.retrieveNewAccessBearerToken();
        
        assertTrue(mockEndpoint.isAccessTokenActive(newAccessToken),
                "The retrieved access token does not match the one recorded by the mock server");
        assertEquals(mockEndpoint.getCurrentRefreshToken(), currentRefreshToken,
                "Mismatched refresh tokens between client and server");
        assertEquals(mockEndpoint.getCurrentGeolocation(), currentGeolocation,
                "Mismatched geolocations between client and server");
        
        if (expectRefreshTokenToChange) {
            assertNotEquals(oldRefreshToken, currentRefreshToken, "A new refresh token should have been returned");
        } else {
            assertEquals(oldRefreshToken, currentRefreshToken, "The refresh token in storage should not have changed");
        }
        
        if (expectGeolocationToChange) {
            assertNotEquals(oldGeolocation, currentGeolocation, "A new geolocation should have been returned");
        } else {
            assertEquals(oldGeolocation, currentGeolocation, "The geolocation in storage should not have changed");
        }
    }

    private void assertAccessTokenRetrievalFails() {
        String oldRefreshToken = currentRefreshToken;
        String oldGeolocation = currentGeolocation;
        assertThrows(RuntimeException.class, () -> concurAccessTokenV2Service.retrieveNewAccessBearerToken(),
                "The service should have failed to retrieve a new access token");
        assertEquals(oldRefreshToken, currentRefreshToken,
                "The stored refresh token should not have changed after failing to retrieve a new access token");
        assertEquals(oldGeolocation, currentGeolocation,
                "The stored geolocation should not have changed after failing to retrieve a new access token");
    }

    private void assertNewRefreshTokenRetrievalSucceeds(boolean expectGeolocationToChange) {
        String oldGeolocation = currentGeolocation;
        String oldRefreshToken = currentRefreshToken;
        mockEndpoint.setForceNewRefreshToken(true);
        
        concurAccessTokenV2Service.retrieveAndPersistNewRefreshToken();
        
        assertEquals(mockEndpoint.getCurrentRefreshToken(), currentRefreshToken,
                "Mismatched refresh tokens between client and server");
        assertEquals(mockEndpoint.getCurrentGeolocation(), currentGeolocation,
                "Mismatched geolocations between client and server");
        assertNotEquals(oldRefreshToken, currentRefreshToken, "A new refresh token should have been returned");
        
        if (expectGeolocationToChange) {
            assertNotEquals(oldGeolocation, currentGeolocation, "A new geolocation should have been returned");
        } else {
            assertEquals(oldGeolocation, currentGeolocation, "The geolocation in storage should not have changed");
        }
    }

    private void assertNewRefreshTokenRetrievalFails() {
        assertNewRefreshTokenRetrievalFails(true);
    }

    private void assertNewRefreshTokenRetrievalFails(boolean forceNewRefreshToken) {
        String oldGeolocation = currentGeolocation;
        String oldRefreshToken = currentRefreshToken;
        mockEndpoint.setForceNewRefreshToken(forceNewRefreshToken);
        assertThrows(RuntimeException.class, () -> concurAccessTokenV2Service.retrieveAndPersistNewRefreshToken(),
                "The service should have failed to retrieve a new refresh token");
        assertEquals(oldRefreshToken, currentRefreshToken,
                "The stored refresh token should not have changed after failing to retrieve a new refresh token");
        assertEquals(oldGeolocation, currentGeolocation,
                "The stored geolocation should not have changed after failing to retrieve a new refresh token");
    }

}
