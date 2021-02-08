package edu.cornell.kfs.concur.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurAwsKeyNames;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.MockLegacyAuthConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.aws.ConcurStaticConfig;
import edu.cornell.kfs.concur.aws.ConcurTokenConfig;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.rest.application.MockConcurLegacyAuthenticationServerApplication;
import edu.cornell.kfs.concur.rest.resource.MockConcurLegacyAuthenticationServerResource;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CuSysTestConstants.MockAwsSecretServiceConstants;
import edu.cornell.kfs.sys.extension.AwsSecretServiceCacheExtension;
import edu.cornell.kfs.sys.rest.util.ApacheHttpJerseyTestExtension;
import edu.cornell.kfs.sys.service.impl.MockAwsSecretServiceImpl;
import edu.cornell.kfs.sys.util.CuJsonTestUtils;
import edu.cornell.kfs.sys.util.CuJsonUtils;

@Execution(ExecutionMode.SAME_THREAD)
@AwsSecretServiceCacheExtension(awsSecretServiceField = "mockAwsSecretService")
public class ConcurAccessTokenServiceImplTest {

    private static final String DUMMY_CONFIG_VALUE = "ABCD3333EFG456__";

    private static ApacheHttpJerseyTestExtension jerseyExtension;
    private static DateTimeFormatter dateTimeFormatter;

    private MockConcurLegacyAuthenticationServerResource mockAuthenticationResource;
    private AccessTokenDTO initialTokenDTO;
    private ConcurBatchUtilityService concurBatchUtilityService;
    private MockAwsSecretServiceImpl mockAwsSecretService;
    private ConcurAccessTokenServiceImpl concurAccessTokenService;

    @BeforeAll
    static void startUpMockConcurApplication() throws Exception {
        dateTimeFormatter = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am)
                .withLocale(Locale.US);
        jerseyExtension = new ApacheHttpJerseyTestExtension(
                new MockConcurLegacyAuthenticationServerApplication());
        jerseyExtension.startUp();
    }

    @AfterAll
    static void shutDownMockConcurApplication() throws Exception {
        dateTimeFormatter = null;
        try {
            if (jerseyExtension != null) {
                jerseyExtension.close();
            }
        } finally {
            jerseyExtension = null;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        this.mockAuthenticationResource = getAndConfigureMockAuthenticationResource();
        this.initialTokenDTO = mockAuthenticationResource.buildAndStoreNewTokenDTO();
        this.concurBatchUtilityService = buildMockConcurBatchUtilityService();
        this.mockAwsSecretService = buildMockAwsSecretService(initialTokenDTO);
        this.concurAccessTokenService = buildConcurAccessTokenService(concurBatchUtilityService, mockAwsSecretService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockAuthenticationResource != null) {
            mockAuthenticationResource.resetForNextTestMethod();
        }
        this.mockAuthenticationResource = null;
        this.initialTokenDTO = null;
        this.concurBatchUtilityService = null;
        this.mockAwsSecretService = null;
        this.concurAccessTokenService = null;
    }

    private MockConcurLegacyAuthenticationServerResource getAndConfigureMockAuthenticationResource() {
        String decodedCredentials = MockLegacyAuthConstants.MOCK_USERNAME
                + ConcurConstants.USERNAME_PASSWORD_SEPARATOR + MockLegacyAuthConstants.MOCK_PASSWORD;
        String encodedCredentials = ConcurUtils.base64Encode(decodedCredentials);
        
        MockConcurLegacyAuthenticationServerResource mockResource = jerseyExtension.getSingletonFromApplication(
                MockConcurLegacyAuthenticationServerResource.class);
        mockResource.setBaseUri(jerseyExtension.getBaseUri().toString());
        mockResource.setValidClientId(MockLegacyAuthConstants.MOCK_CLIENT_ID);
        mockResource.setValidClientSecret(MockLegacyAuthConstants.MOCK_SECRET_KEY);
        mockResource.setValidEncodedCredentials(encodedCredentials);
        return mockResource;
    }

    private ConcurBatchUtilityService buildMockConcurBatchUtilityService() {
        ConcurBatchUtilityService utilityService = Mockito.mock(ConcurBatchUtilityService.class);
        Mockito.when(utilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_REFRESH_ACCESS_TOKEN))
                .thenReturn(KFSConstants.ParameterValues.YES);
        return utilityService;
    }

    private MockAwsSecretServiceImpl buildMockAwsSecretService(AccessTokenDTO tokenDTO) {
        String staticConfigJson = buildStaticConfigJson();
        String tokenConfigJson = buildTokenConfigJson(tokenDTO);
        
        MockAwsSecretServiceImpl secretService = new MockAwsSecretServiceImpl();
        secretService.overrideLocalSecrets(
                buildSharedConcurSecret(ConcurAwsKeyNames.STATIC_CONFIG, staticConfigJson),
                buildInstanceConcurSecret(ConcurAwsKeyNames.TOKEN_CONFIG, tokenConfigJson));
        return secretService;
    }

    private String buildStaticConfigJson() {
        return CuJsonTestUtils.buildJsonStringFromEntries(
                Map.entry(ConcurPropertyConstants.ConcurStaticConfig.LOGIN_USERNAME,
                        MockLegacyAuthConstants.MOCK_USERNAME),
                Map.entry(ConcurPropertyConstants.ConcurStaticConfig.LOGIN_PASSWORD,
                        MockLegacyAuthConstants.MOCK_PASSWORD),
                Map.entry(ConcurPropertyConstants.ConcurStaticConfig.CONSUMER_KEY,
                        MockLegacyAuthConstants.MOCK_CLIENT_ID),
                Map.entry(ConcurPropertyConstants.ConcurStaticConfig.SECRET_KEY,
                        MockLegacyAuthConstants.MOCK_SECRET_KEY));
    }

    private String buildTokenConfigJson(AccessTokenDTO tokenDTO) {
        return CuJsonTestUtils.buildJsonStringFromEntries(
                Map.entry(ConcurPropertyConstants.ConcurTokenConfig.ACCESS_TOKEN, tokenDTO.getToken()),
                Map.entry(ConcurPropertyConstants.ConcurTokenConfig.REFRESH_TOKEN, tokenDTO.getRefreshToken()),
                Map.entry(ConcurPropertyConstants.ConcurTokenConfig.ACCESS_TOKEN_EXPIRATION_DATE,
                        dateTimeFormatter.print(tokenDTO.getExpirationDate().getTime())));
    }

    private Map.Entry<String, String> buildInstanceConcurSecret(String awsKeyName, String value) {
        return buildConcurSecret(MockAwsSecretServiceConstants.KFS_LOCALDEV_INSTANCE_NAMESPACE, awsKeyName, value);
    }

    private Map.Entry<String, String> buildSharedConcurSecret(String awsKeyName, String value) {
        return buildConcurSecret(MockAwsSecretServiceConstants.KFS_SHARED_NAMESPACE, awsKeyName, value);
    }

    private Map.Entry<String, String> buildConcurSecret(String awsKeyPrefix, String awsKeyName, String value) {
        return Map.entry(awsKeyPrefix + awsKeyName, value);
    }

    private ConcurAccessTokenServiceImpl buildConcurAccessTokenService(
            ConcurBatchUtilityService batchUtilityService, MockAwsSecretServiceImpl awsSecretService) {
        String requestTokenUrl = buildMockConcurServiceUrl(MockLegacyAuthConstants.REQUEST_TOKEN_PATH);
        String refreshTokenUrl = buildMockConcurServiceUrl(
                MockLegacyAuthConstants.REFRESH_TOKEN_PATH + KFSConstants.QUESTION_MARK);
        String revokeTokenUrl = buildMockConcurServiceUrl(
                MockLegacyAuthConstants.REVOKE_TOKEN_PATH + KFSConstants.QUESTION_MARK);
        
        ConcurAccessTokenServiceImpl tokenService = new ConcurAccessTokenServiceImpl();
        tokenService.setConcurBatchUtilityService(batchUtilityService);
        tokenService.setAwsSecretService(awsSecretService);
        tokenService.setConcurRequestAccessTokenURL(requestTokenUrl);
        tokenService.setConcurRefreshAccessTokenURL(refreshTokenUrl);
        tokenService.setConcurRevokeAccessTokenURL(revokeTokenUrl);
        return tokenService;
    }

    private String buildMockConcurServiceUrl(String... subPathSegments) {
        return jerseyExtension.buildAbsoluteUriPath(
                MockLegacyAuthConstants.BASE_RESOURCE_PATH, subPathSegments);
    }

    @Test
    void testUnmarshalAndMarshalOfStaticConfigJson() throws Exception {
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        String staticConfigJson = buildStaticConfigJson();
        assertTrue(StringUtils.isNotBlank(staticConfigJson), "Initial JSON string should have been non-blank");
        
        ConcurStaticConfig staticConfig = objectMapper.readValue(staticConfigJson, ConcurStaticConfig.class);
        assertNotNull(staticConfig, "Static Config DTO should have been non-null");
        assertEquals(MockLegacyAuthConstants.MOCK_USERNAME, staticConfig.getLogin_username(), "Wrong username");
        assertEquals(MockLegacyAuthConstants.MOCK_PASSWORD, staticConfig.getLogin_password(), "Wrong password");
        assertEquals(MockLegacyAuthConstants.MOCK_CLIENT_ID, staticConfig.getConsumer_key(),
                "Wrong client ID (aka consumer key)");
        assertEquals(MockLegacyAuthConstants.MOCK_SECRET_KEY, staticConfig.getSecret_key(), "Wrong secret key");
        
        String jsonFromDTO = objectMapper.writeValueAsString(staticConfig);
        assertEquals(staticConfigJson, jsonFromDTO, "Marshalled JSON should have matched the original JSON");
    }

    @Test
    void testUnmarshalAndMarshalOfTokenConfigJson() throws Exception {
        assertTrue(StringUtils.isNotBlank(initialTokenDTO.getToken()),
                "Initial access token should have been non-blank");
        assertTrue(StringUtils.isNotBlank(initialTokenDTO.getRefreshToken()),
                "Initial refresh token should have been non-blank");
        assertNotNull(initialTokenDTO.getExpirationDate(),
                "Initial access token expiration date should have been non-null");
        
        ObjectMapper objectMapper = CuJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        String tokenConfigJson = buildTokenConfigJson(initialTokenDTO);
        assertTrue(StringUtils.isNotBlank(tokenConfigJson), "Initial JSON string should have been non-blank");
        
        ConcurTokenConfig tokenConfig = objectMapper.readValue(tokenConfigJson, ConcurTokenConfig.class);
        assertNotNull(tokenConfig, "Token Config DTO should have been non-null");
        assertEquals(initialTokenDTO.getToken(), tokenConfig.getAccess_token(), "Wrong access token");
        assertEquals(initialTokenDTO.getRefreshToken(), tokenConfig.getRefresh_token(), "Wrong refresh token");
        assertEquals(initialTokenDTO.getExpirationDate(), tokenConfig.getAccess_token_expiration_date(),
                "Wrong access token expiration date");
        
        String jsonFromDTO = objectMapper.writeValueAsString(tokenConfig);
        assertEquals(tokenConfigJson, jsonFromDTO, "Marshalled JSON should have matched the original JSON");
    }

    @Test
    void testRequestNewToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.requestNewAccessToken();
        assertMockEndpointHasMatchingToken(initialTokenDTO);
        assertSuccessfulTokenChangeForNewTokenRequest(initialTokenDTO);
    }

    @Test
    void testRefreshExistingToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.refreshAccessToken();
        assertSuccessfulTokenChangeForTokenRefresh(initialTokenDTO);
    }

    @Test
    void testRevokeExistingToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.revokeAccessToken();
        assertTokenWasRevokedProperly(initialTokenDTO);
    }

    @Test
    void testRevokeAndReplaceToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.revokeAndReplaceAccessToken();
        assertMockEndpointNoLongerHasToken(initialTokenDTO.getToken());
        assertSuccessfulTokenChangeForNewTokenRequest(initialTokenDTO);
    }

    @Test
    void testResetToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.resetTokenToEmptyStringInDataStorage();
        assertTokenWasResetProperly(initialTokenDTO);
    }

    @Test
    void testResetAndRequestToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.resetTokenToEmptyStringInDataStorage();
        assertTokenWasResetProperly(initialTokenDTO);
        concurAccessTokenService.requestNewAccessToken();
        assertMockEndpointHasMatchingToken(initialTokenDTO);
        assertSuccessfulTokenChangeForNewTokenRequest(initialTokenDTO);
    }

    @Test
    void testRevokeAndRequestTokenInTwoSteps() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.revokeAccessToken();
        assertTokenWasRevokedProperly(initialTokenDTO);
        concurAccessTokenService.requestNewAccessToken();
        assertMockEndpointNoLongerHasToken(initialTokenDTO.getToken());
        assertSuccessfulTokenChangeForNewTokenRequest(initialTokenDTO);
    }

    @Test
    void testRequestAndRefreshNewToken() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.requestNewAccessToken();
        assertMockEndpointHasMatchingToken(initialTokenDTO);
        assertSuccessfulTokenChangeForNewTokenRequest(initialTokenDTO);
        
        Optional<AccessTokenDTO> optionalDTO = mockAuthenticationResource.getTokenDTOByAccessToken(
                concurAccessTokenService.getAccessToken());
        assertTrue(optionalDTO.isPresent(), "The mock endpoint should have contained the newly requested token");
        
        AccessTokenDTO requestedTokenDTO = optionalDTO.get();
        concurAccessTokenService.refreshAccessToken();
        assertSuccessfulTokenChangeForTokenRefresh(requestedTokenDTO);
    }

    @Test
    void testDisableTokenRefresh() throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        Mockito.doReturn(KFSConstants.ParameterValues.NO)
                .when(concurBatchUtilityService).getConcurParameterValue(
                        ConcurParameterConstants.CONCUR_REFRESH_ACCESS_TOKEN);
        
        concurAccessTokenService.refreshAccessToken();
        assertServiceHasCorrectTokenForInitialSetup();
        assertCurrentTokenConfigHasCorrectSetupAndThen(tokenConfig -> {
            assertEquals(initialTokenDTO.getRefreshToken(), tokenConfig.getRefresh_token(),
                    "The refresh token should not have changed when access token refreshes are disabled in KFS");
        });
    }

    static Stream<Consumer<ConcurAccessTokenServiceImpl>> serviceActionsDisallowedAfterResetOrRevoke() {
        return Stream.of(
                ConcurAccessTokenServiceImpl::refreshAccessToken,
                ConcurAccessTokenServiceImpl::revokeAccessToken,
                ConcurAccessTokenServiceImpl::revokeAndReplaceAccessToken);
    }

    @ParameterizedTest
    @MethodSource("serviceActionsDisallowedAfterResetOrRevoke")
    void testTokenRevokePreventsSubsequentRefreshOrRevoke(
            Consumer<ConcurAccessTokenServiceImpl> serviceInvoker) throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.revokeAccessToken();
        assertTokenWasRevokedProperly(initialTokenDTO);
        assertThrows(RuntimeException.class, () -> serviceInvoker.accept(concurAccessTokenService),
                "The post-revoke operation should have failed");
    }

    @ParameterizedTest
    @MethodSource("serviceActionsDisallowedAfterResetOrRevoke")
    void testTokenResetPreventsSubsequentRefreshOrRevoke(
            Consumer<ConcurAccessTokenServiceImpl> serviceInvoker) throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        concurAccessTokenService.resetTokenToEmptyStringInDataStorage();
        assertTokenWasResetProperly(initialTokenDTO);
        assertThrows(RuntimeException.class, () -> serviceInvoker.accept(concurAccessTokenService),
                "The post-reset operation should have failed");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        ConcurPropertyConstants.ConcurStaticConfig.LOGIN_USERNAME,
        ConcurPropertyConstants.ConcurStaticConfig.LOGIN_PASSWORD,
        ConcurPropertyConstants.ConcurStaticConfig.CONSUMER_KEY
    })
    void testCannotRequestNewTokenUsingInvalidConfigProperty(String propertyToChange) throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        overridePropertyInJsonConfig(propertyToChange, DUMMY_CONFIG_VALUE);
        assertThrows(RuntimeException.class, concurAccessTokenService::requestNewAccessToken,
                "The request for a new access token should have failed, due to an invalid config setting");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        ConcurPropertyConstants.ConcurStaticConfig.CONSUMER_KEY,
        ConcurPropertyConstants.ConcurStaticConfig.SECRET_KEY,
        ConcurPropertyConstants.ConcurTokenConfig.ACCESS_TOKEN,
        ConcurPropertyConstants.ConcurTokenConfig.REFRESH_TOKEN
    })
    void testCannotRefreshTokenUsingInvalidConfigProperty(String propertyToChange) throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        overridePropertyInJsonConfig(propertyToChange, DUMMY_CONFIG_VALUE);
        assertThrows(RuntimeException.class, concurAccessTokenService::refreshAccessToken,
                "The refresh of the access token should have failed, due to an invalid config setting");
    }

    static Stream<Consumer<ConcurAccessTokenServiceImpl>> revocationActionInvokers() {
        return Stream.of(
                ConcurAccessTokenServiceImpl::revokeAccessToken,
                ConcurAccessTokenServiceImpl::revokeAndReplaceAccessToken);
    }

    @ParameterizedTest
    @MethodSource("revocationActionInvokers")
    void testCannotRevokeTokenUsingInvalidAccessToken(
            Consumer<ConcurAccessTokenServiceImpl> revocationActionInvoker) throws Exception {
        assertServiceHasCorrectTokenForInitialSetup();
        overridePropertyInJsonConfig(ConcurPropertyConstants.ConcurTokenConfig.ACCESS_TOKEN, DUMMY_CONFIG_VALUE);
        assertThrows(RuntimeException.class, () -> revocationActionInvoker.accept(concurAccessTokenService),
                "The revoking of an invalid access token should have failed");
    }

    private void assertServiceHasCorrectTokenForInitialSetup() throws Exception {
        assertTrue(concurAccessTokenService.currentAccessTokenExists(),
                "Service should have had an initial token set up");
        assertEquals(initialTokenDTO.getToken(), concurAccessTokenService.getAccessToken(),
                "Service returned the wrong initial token");
        assertEquals(initialTokenDTO.getExpirationDate(), concurAccessTokenService.getAccessTokenExpirationDate(),
                "Service returned the wrong initial token expiration date");
        assertMockEndpointHasMatchingToken(initialTokenDTO);
    }

    private void assertSuccessfulTokenChangeForNewTokenRequest(AccessTokenDTO oldTokenDTO) {
        assertTokenConfigurationWasUpdatedProperlyAndThen(oldTokenDTO, tokenConfig -> {
            assertNotEquals(tokenConfig.getRefresh_token(), oldTokenDTO.getRefreshToken(),
                    "The request for a new token should have also updated the refresh token");
        });
    }

    private void assertSuccessfulTokenChangeForTokenRefresh(AccessTokenDTO oldTokenDTO) {
        assertTokenConfigurationWasUpdatedProperlyAndThen(oldTokenDTO, tokenConfig -> {
            assertMockEndpointNoLongerHasToken(oldTokenDTO.getToken());
            assertEquals(tokenConfig.getRefresh_token(), oldTokenDTO.getRefreshToken(),
                    "The refresh of the access token should have kept the refresh token the same");
        });
    }

    private void assertTokenConfigurationWasUpdatedProperlyAndThen(
            AccessTokenDTO oldTokenDTO, Consumer<ConcurTokenConfig> extraValidation) {
        assertTrue(concurAccessTokenService.currentAccessTokenExists(),
                "Service should have had an active token");
        assertNotEquals(oldTokenDTO.getToken(), concurAccessTokenService.getAccessToken(),
                "Service should have updated its access token");
        
        assertCurrentTokenConfigHasCorrectSetupAndThen(extraValidation);
    }

    private void assertCurrentTokenConfigHasCorrectSetupAndThen(Consumer<ConcurTokenConfig> extraValidation) {
        ConcurTokenConfig tokenConfig = concurAccessTokenService.getTokenConfig();
        assertNotNull(tokenConfig, "Service should have had a token config object available");
        assertEquals(tokenConfig.getAccess_token(), concurAccessTokenService.getAccessToken(),
                "Service should have had the same access token as the current token config object");
        assertEquals(tokenConfig.getAccess_token_expiration_date(),
                concurAccessTokenService.getAccessTokenExpirationDate(),
                "Service should have had the same access token expiration date as the current token config object");
        
        assertMockEndpointHasMatchingToken(tokenConfig);
        extraValidation.accept(tokenConfig);
    }

    private void assertTokenWasRevokedProperly(AccessTokenDTO oldTokenDTO) {
        assertServiceHasBlankTokenConfiguration();
        assertMockEndpointNoLongerHasToken(oldTokenDTO.getToken());
    }

    private void assertTokenWasResetProperly(AccessTokenDTO oldTokenDTO) {
        assertServiceHasBlankTokenConfiguration();
        assertMockEndpointHasMatchingToken(oldTokenDTO);
    }

    private void assertServiceHasBlankTokenConfiguration() {
        assertFalse(concurAccessTokenService.currentAccessTokenExists(),
                "Service should not have had an active token");
        assertTrue(StringUtils.isBlank(concurAccessTokenService.getAccessToken()),
                "Service should have returned a blank access token");
        assertNull(concurAccessTokenService.getAccessTokenExpirationDate(),
                "Service should have returned a null access token expiration date");
        
        ConcurTokenConfig tokenConfig = concurAccessTokenService.getTokenConfig();
        assertNotNull(tokenConfig, "Service should have had a token config object, even if its fields are blank");
        assertTrue(StringUtils.isBlank(tokenConfig.getAccess_token()), "Access token should have been blank");
        assertTrue(StringUtils.isBlank(tokenConfig.getRefresh_token()), "Refresh token should have been blank");
        assertNull(tokenConfig.getAccess_token_expiration_date(), "Token expiration date should have been null");
    }

    private void assertMockEndpointHasMatchingToken(AccessTokenDTO tokenDTO) {
        assertMockEndpointHasMatchingToken(tokenDTO.getToken(), tokenDTO.getRefreshToken(),
                tokenDTO.getExpirationDate());
    }

    private void assertMockEndpointHasMatchingToken(ConcurTokenConfig tokenConfig) {
        assertMockEndpointHasMatchingToken(tokenConfig.getAccess_token(), tokenConfig.getRefresh_token(),
                tokenConfig.getAccess_token_expiration_date());
    }

    private void assertMockEndpointHasMatchingToken(String accessToken, String refreshToken, Date expirationDate) {
        Optional<AccessTokenDTO> optionalDTO = mockAuthenticationResource.getTokenDTOByAccessToken(accessToken);
        assertTrue(optionalDTO.isPresent(), "The mock endpoint should have contained the given token");
        
        AccessTokenDTO newTokenDTO = optionalDTO.get();
        assertEquals(accessToken, newTokenDTO.getToken(), "Wrong access token from endpoint");
        assertEquals(refreshToken, newTokenDTO.getRefreshToken(), "Wrong refresh token from endpoint");
        assertEquals(expirationDate, newTokenDTO.getExpirationDate(), "Wrong token expiration date from endpoint");
    }

    private void assertMockEndpointNoLongerHasToken(String accessToken) {
        Optional<AccessTokenDTO> optionalDTO = mockAuthenticationResource.getTokenDTOByAccessToken(accessToken);
        assertFalse(optionalDTO.isPresent(), "The mock endpoint should have removed the given token");
    }

    private void overridePropertyInJsonConfig(String propertyName, String propertyValue) {
        String jsonAwsKey = getAwsKeyOfConfigContainingProperty(propertyName);
        boolean useKfsInstanceNamespace = StringUtils.equals(ConcurAwsKeyNames.TOKEN_CONFIG, jsonAwsKey);
        String oldJsonConfig = mockAwsSecretService.getSingleStringValueFromAwsSecret(
                jsonAwsKey, useKfsInstanceNamespace);
        String newJsonConfig = CuJsonTestUtils.rebuildJsonStringWithNewOrUpdatedProperty(
                oldJsonConfig, propertyName, propertyValue);
        mockAwsSecretService.updateSecretValue(jsonAwsKey, useKfsInstanceNamespace, newJsonConfig);
    }

    private String getAwsKeyOfConfigContainingProperty(String propertyName) {
        switch (propertyName) {
            case ConcurPropertyConstants.ConcurStaticConfig.LOGIN_USERNAME :
            case ConcurPropertyConstants.ConcurStaticConfig.LOGIN_PASSWORD :
            case ConcurPropertyConstants.ConcurStaticConfig.CONSUMER_KEY :
            case ConcurPropertyConstants.ConcurStaticConfig.SECRET_KEY :
                return ConcurAwsKeyNames.STATIC_CONFIG;
            
            case ConcurPropertyConstants.ConcurTokenConfig.ACCESS_TOKEN :
            case ConcurPropertyConstants.ConcurTokenConfig.REFRESH_TOKEN :
                return ConcurAwsKeyNames.TOKEN_CONFIG;
            
            default :
                throw new IllegalArgumentException("Invalid property name for override: " + propertyName);
        }
    }

}
