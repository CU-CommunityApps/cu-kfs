package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOAuth2TokenResponseDTO;
import edu.cornell.kfs.concur.util.MockConcurUtils;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenV2GeolocationTest {

    private static final String LOCALHOST_8080 = "http://localhost:8080";
    private static final String LOCALHOST_25 = "http://localhost:25";

    private ConcurAccessTokenV2ServiceImpl concurAccessTokenV2Service;
    private ConcurBatchUtilityService mockConcurBatchUtilityService;

    @BeforeEach
    void setUp() throws Exception {
        this.mockConcurBatchUtilityService = MockConcurUtils.createMockConcurBatchUtilityServiceBackedByParameters(
                Map.entry(ConcurParameterConstants.CONCUR_GEOLOCATION_URL,
                        ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS));
        this.concurAccessTokenV2Service = new ConcurAccessTokenV2ServiceImpl();
        
        concurAccessTokenV2Service.setWebServiceCredentialService(Mockito.mock(WebServiceCredentialService.class));
        concurAccessTokenV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        concurAccessTokenV2Service.setConcurGeolocationUrlPattern(
                ConcurTestConstants.CONCUR_GEOLOCATION_PATTERN_CONCURSOLUTIONS);
    }

    @AfterEach
    void tearDown() throws Exception {
        concurAccessTokenV2Service = null;
        mockConcurBatchUtilityService = null;
    }

    static Stream<String> validRemoteGeolocationUrls() {
        return Stream.of(
                ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS,
                ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS2,
                "https://us3.api.concursolutions.com",
                "https://us-east.api.concursolutions.com",
                "https://america.tech.api.concursolutions.com",
                "https://www.concursolutions.com"
        ).flatMap(url -> Stream.of(url, url + CUKFSConstants.SLASH));
    }

    static Stream<String> validLocalGeolocationUrls() {
        return Stream.of(
                LOCALHOST_8080 + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX,
                LOCALHOST_8080 + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2,
                LOCALHOST_25 + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX,
                LOCALHOST_25 + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX2,
                LOCALHOST_8080 + "/us3.api.concursolutions.com",
                LOCALHOST_8080 + "/us-east.api.concursolutions.com",
                LOCALHOST_8080 + "/america.tech.api.concursolutions.com",
                LOCALHOST_8080 + "/www.concursolutions.com"
        ).flatMap(url -> Stream.of(url, url + CUKFSConstants.SLASH));
    }

    static Stream<String> invalidGeolocationUrls() {
        return Stream.of(
                null,
                KFSConstants.EMPTY_STRING,
                KFSConstants.BLANK_SPACE,
                KFSConstants.NEWLINE,
                "us.api.concursolutions.edu",
                "www.concur.com",
                "us.api.concursolutions.com/api",
                "us.api.concursolutions.com.net",
                "us.api%20system.concursolutions.com",
                "concursolutions.com"
        ).flatMap(url -> StringUtils.isNotBlank(url)
                ? Stream.of("https://" + url, LOCALHOST_8080 + CUKFSConstants.SLASH + url)
                : Stream.of(url));
    }

    @ParameterizedTest
    @MethodSource("validRemoteGeolocationUrls")
    void testValidRemoteGeolocationUrls(String newUrl) throws Exception {
        assertUrlPassesValidation(ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS, newUrl);
    }

    @ParameterizedTest
    @MethodSource("validLocalGeolocationUrls")
    void testValidLocalGeolocationUrls(String newUrl) throws Exception {
        assertUrlPassesValidationForLocalhostSetup(newUrl);
    }

    @ParameterizedTest
    @MethodSource("validLocalGeolocationUrls")
    void testLocalGeolocationUrlsFailWhenExpectingRemoteUrls(String newUrl) throws Exception {
        assertUrlFailsValidation(ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS, newUrl);
    }

    @ParameterizedTest
    @MethodSource("validRemoteGeolocationUrls")
    void testRemoteGeolocationUrlsFailWhenExpectingLocalUrls(String newUrl) throws Exception {
        assertUrlFailsValidationForLocalhostSetup(newUrl);
    }

    @ParameterizedTest
    @MethodSource("invalidGeolocationUrls")
    void testMalformedUrlsFailForRemoteUrlSetup(String newUrl) throws Exception {
        assertUrlFailsValidation(ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS, newUrl);
    }

    @ParameterizedTest
    @MethodSource("invalidGeolocationUrls")
    void testMalformedUrlsFailForLocalUrlSetup(String newUrl) throws Exception {
        assertUrlFailsValidationForLocalhostSetup(newUrl);
    }

    private void assertUrlPassesValidationForLocalhostSetup(String newUrl) {
        String localUrl = configureAndReturnLocalUrl();
        assertUrlPassesValidation(localUrl, newUrl);
    }

    private void assertUrlPassesValidation(String oldUrl, String newUrl) {
        assertCorrectInitialUrlIsConfigured(oldUrl);
        
        ConcurOAuth2TokenResponseDTO tokenResponse = new ConcurOAuth2TokenResponseDTO();
        tokenResponse.setGeolocation(newUrl);
        concurAccessTokenV2Service.updateAndValidateGeolocationIfRequired(tokenResponse);
        
        String newStoredUrl = mockConcurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_GEOLOCATION_URL);
        String urlForCompare = chopEndingSlashIfPresent(newUrl);
        assertEquals(newStoredUrl, urlForCompare, "URL should have been updated after successful validation");
    }

    private void assertUrlFailsValidationForLocalhostSetup(String newUrl) {
        String localUrl = configureAndReturnLocalUrl();
        assertUrlFailsValidation(localUrl, newUrl);
    }

    private void assertUrlFailsValidation(String oldUrl, String newUrl) {
        assertCorrectInitialUrlIsConfigured(oldUrl);
        
        ConcurOAuth2TokenResponseDTO tokenResponse = new ConcurOAuth2TokenResponseDTO();
        tokenResponse.setGeolocation(newUrl);
        assertThrows(RuntimeException.class,
                () -> concurAccessTokenV2Service.updateAndValidateGeolocationIfRequired(tokenResponse),
                "The URL update attempt should have failed due to a malformed replacement URL");
        
        String storedUrl = mockConcurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_GEOLOCATION_URL);
        assertEquals(storedUrl, oldUrl, "Configured URL should have remained the same due to failed validation");
    }

    private void assertCorrectInitialUrlIsConfigured(String oldUrl) {
        String oldStoredUrl = mockConcurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_GEOLOCATION_URL);
        assertEquals(oldStoredUrl, oldUrl, "Wrong initial URL");
    }

    private String configureAndReturnLocalUrl() {
        String localUrl = LOCALHOST_8080 + ConcurTestConstants.CONCUR_GEOLOCATION_LOCALHOST_SUFFIX;
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.CONCUR_GEOLOCATION_URL, localUrl);
        concurAccessTokenV2Service.setConcurGeolocationUrlPattern(
                ConcurTestConstants.CONCUR_GEOLOCATION_PATTERN_LOCALHOST);
        return localUrl;
    }

    private String chopEndingSlashIfPresent(String url) {
        return StringUtils.endsWith(url, CUKFSConstants.SLASH) ? StringUtils.chop(url) : url;
    }

}
