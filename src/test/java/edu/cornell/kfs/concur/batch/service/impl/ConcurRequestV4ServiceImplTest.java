package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.RequestV4Dates;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4ListingFixture;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4PersonFixture;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4CustomItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4OperationDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4PersonDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.web.mock.MockRemoteServerExtension;

public class ConcurRequestV4ServiceImplTest {
/* This is not working after the http client library version upgrade with 11/17/21. This will be fixed in a separate user story */
//    private static final String US_EASTERN_TIME_ZONE = "US/Eastern";
//    private static final String DEFAULT_MOCK_CURRENT_DATE = RequestV4Dates.DATE_2022_01_03;
//    private static final String NONEXISTENT_REQUEST_ID = "AAAAAAAAAAAAAAAA0000000000000000";
//
//    private static final String VALIDATION_ERROR_MISSING_CHART = "Missing Chart Code";
//    private static final String VALIDATION_ERROR_MISSING_ACCOUNT = "Missing Account Number";
//    private static final String VALIDATION_ERROR_INVALID_ACCOUNT = "Invalid Account Number";
//
//    private String mockAccessToken;
//    private MockConcurRequestV4ServiceEndpoint mockConcurEndpoint;
//    private MockRemoteServerExtension mockHttpServer;
//    private String baseRequestV4Url;
//    private Map<String, String> concurParameters;
//    private Map<String, String> concurProperties;
//    private TestDateTimeServiceImpl testDateTimeService;
//    private TestConcurRequestV4ServiceImpl requestV4Service;
//    private DateTimeZone easternTimeZone;
//    private long mockCurrentTimeMillis;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        mockAccessToken = buildMockAccessTokenFromUUID();
//        mockConcurEndpoint = new MockConcurRequestV4ServiceEndpoint(mockAccessToken,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.PENDING_APPROVAL_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
//                RequestV4DetailFixture.CANCELED_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.APPROVED_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.SENTBACK_INVALID_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_COST_APPROVAL_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.NOT_SUBMITTED_REGULAR_REQUEST_BOB_SMITH);
//        
//        mockHttpServer = new MockRemoteServerExtension();
//        mockHttpServer.initialize(mockConcurEndpoint);
//        baseRequestV4Url = mockConcurEndpoint.getBaseRequestV4Url();
//        
//        concurParameters = buildTestParameters(baseRequestV4Url);
//        concurProperties = buildTestProperties();
//        
//        ConcurBatchUtilityService mockConcurBatchUtilityService = buildMockConcurBatchUtilityService();
//        testDateTimeService = buildDateTimeService();
//        easternTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone(US_EASTERN_TIME_ZONE));
//        mockCurrentTimeMillis = getTimeInMilliseconds(DEFAULT_MOCK_CURRENT_DATE);
//        
//        requestV4Service = new TestConcurRequestV4ServiceImpl();
//        requestV4Service.setSimulateProductionMode(false);
//        requestV4Service.setSkipRequestListItemProcessing(false);
//        requestV4Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
//        requestV4Service.setConcurEventNotificationV2WebserviceService(
//                buildConcurEventNotificationV2WebserviceService(mockConcurBatchUtilityService));
//        requestV4Service.setConfigurationService(buildMockConfigurationService());
//        requestV4Service.setDateTimeService(buildSpiedDateTimeService(testDateTimeService));
//        requestV4Service.setConcurAccountValidationService(buildMockConcurAccountValidationService());
//    }
//
//    @SuppressWarnings("deprecation")
//    @AfterEach
//    void tearDown() throws Exception {
//        IOUtils.closeQuietly(mockHttpServer);
//        mockAccessToken = null;
//        concurParameters = null;
//        concurProperties = null;
//        mockConcurEndpoint = null;
//        mockHttpServer = null;
//        baseRequestV4Url = null;
//        testDateTimeService = null;
//        requestV4Service = null;
//        easternTimeZone = null;
//    }
//
//    private String buildMockAccessTokenFromUUID() {
//        return StringUtils.strip(UUID.randomUUID().toString(), KFSConstants.DASH);
//    }
//
//    private Map<String, String> buildTestParameters(String requestV4Endpoint) {
//        Map<String, String> parameters = new HashMap<>();
//        parameters.put(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, ParameterTestValues.MAX_RETRIES_1);
//        parameters.put(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE,
//                ParameterTestValues.DEFAULT_OBJECT_CODE_5500);
//        parameters.put(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT, requestV4Endpoint);
//        parameters.put(ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE,
//                ParameterTestValues.REQUEST_V4_PAGE_SIZE_2);
//        parameters.put(ConcurParameterConstants.REQUEST_V4_TEST_USERS, buildDefaultTestUsersParameterValue());
//        parameters.put(ConcurParameterConstants.REQUEST_V4_NUMBER_OF_DAYS_OLD,
//                ParameterTestValues.REQUEST_V4_DAYS_OLD_1);
//        return parameters;
//    }
//
//    private String buildDefaultTestUsersParameterValue() {
//        return buildTestUsersParameterValue(RequestV4PersonFixture.JOHN_TEST, RequestV4PersonFixture.JANE_DOE,
//                RequestV4PersonFixture.TEST_MANAGER, RequestV4PersonFixture.TEST_APPROVER);
//    }
//
//    private String buildTestUsersParameterValue(RequestV4PersonFixture... users) {
//        return Stream.of(users)
//                .map(RequestV4PersonFixture::toKeyValuePairForParameterEntry)
//                .collect(Collectors.joining(CUKFSConstants.SEMICOLON));
//    }
//
//    private void overrideQueryNumberOfDaysOld(int newDaysOldValue) {
//        overrideParameter(ConcurParameterConstants.REQUEST_V4_NUMBER_OF_DAYS_OLD, String.valueOf(newDaysOldValue));
//    }
//
//    private void overrideQueryPageSize(int newPageSize) {
//        overrideParameter(ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE, String.valueOf(newPageSize));
//    }
//
//    private void overrideParameter(String parameterName, String parameterValue) {
//        concurParameters.put(parameterName, parameterValue);
//    }
//
//    private ConcurBatchUtilityService buildMockConcurBatchUtilityService() {
//        ConcurBatchUtilityService concurBatchUtilityService = Mockito.mock(ConcurBatchUtilityService.class);
//        Mockito.when(concurBatchUtilityService.getConcurParameterValue(Mockito.anyString()))
//                .then(invocation -> concurParameters.get(invocation.getArgument(0)));
//        return concurBatchUtilityService;
//    }
//
//    private Map<String, String> buildTestProperties() {
//        Map<String, String> properties = new HashMap<>();
//        properties.put(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING,
//                PropertyTestValues.REQUESTV4_REQUEST_LIST_SEARCH_MESSAGE);
//        properties.put(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_REQUEST,
//                PropertyTestValues.REQUESTV4_SINGLE_REQUEST_SEARCH_MESSAGE);
//        return properties;
//    }
//
//    private ConfigurationService buildMockConfigurationService() {
//        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
//        Mockito.when(configurationService.getPropertyValueAsString(Mockito.anyString()))
//                .then(invocation -> concurProperties.get(invocation.getArgument(0)));
//        return configurationService;
//    }
//
//    private TestDateTimeServiceImpl buildDateTimeService() throws Exception {
//        TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
//        dateTimeService.afterPropertiesSet();
//        return dateTimeService;
//    }
//
//    private DateTimeService buildSpiedDateTimeService(
//            TestDateTimeServiceImpl actualDateTimeService) throws Exception {
//        TestDateTimeServiceImpl dateTimeService = Mockito.spy(actualDateTimeService);
//        Mockito.doAnswer(invocation -> getMockCurrentDate())
//                .when(dateTimeService).getCurrentDate();
//        Mockito.doAnswer(invocation -> convertToSameTimeOfDayInEasternTime((Date) invocation.callRealMethod()))
//                .when(dateTimeService).convertToDateTime(Mockito.anyString());
//        Mockito.doAnswer(invocation -> toDateTimeStringInDefaultZone(actualDateTimeService, invocation.getArgument(0)))
//                .when(dateTimeService).toDateTimeString(Mockito.any());
//        return dateTimeService;
//    }
//
//    private Date getMockCurrentDate() {
//        if (mockCurrentTimeMillis <= 0L) {
//            throw new IllegalStateException("The mocked current-time setting may not have been initialized");
//        }
//        return new Date(mockCurrentTimeMillis);
//    }
//
//    private long getTimeInMilliseconds(String dateString) throws ParseException {
//        if (testDateTimeService == null) {
//            throw new IllegalStateException("testDateTimeService has not been initialized yet");
//        }
//        Date parsedDate = testDateTimeService.convertToDateTime(dateString);
//        parsedDate = convertToSameTimeOfDayInEasternTime(parsedDate);
//        return parsedDate.getTime();
//    }
//
//    private Date convertToSameTimeOfDayInEasternTime(Date dateValue) {
//        if (easternTimeZone == null) {
//            throw new IllegalStateException("easternTimeZone has not been initialized yet");
//        }
//        MutableDateTime dateTime = new MutableDateTime(dateValue);
//        dateTime.setZoneRetainFields(easternTimeZone);
//        return new Date(dateTime.getMillis());
//    }
//
//    private String toDateTimeStringInDefaultZone(DateTimeService actualDateTimeService, Date dateValue) {
//        Date newDateValue = convertFromEasternTimeToSameTimeOfDayInDefaultZone(dateValue);
//        return actualDateTimeService.toDateTimeString(newDateValue);
//    }
//
//    private Date convertFromEasternTimeToSameTimeOfDayInDefaultZone(Date dateValue) {
//        DateTimeZone defaultTimeZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
//        MutableDateTime dateTime = new MutableDateTime(dateValue, easternTimeZone);
//        dateTime.setZoneRetainFields(defaultTimeZone);
//        return new Date(dateTime.getMillis());
//    }
//
//    private void overrideCurrentDate(String newDate) throws ParseException {
//        mockCurrentTimeMillis = getTimeInMilliseconds(newDate);
//    }
//
//    private ConcurEventNotificationV2WebserviceService buildConcurEventNotificationV2WebserviceService(
//            ConcurBatchUtilityService mockConcurBatchUtilityService) {
//        ConcurEventNotificationV2WebserviceServiceImpl notificationV2Service
//                = new ConcurEventNotificationV2WebserviceServiceImpl();
//        notificationV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
//        return notificationV2Service;
//    }
//
//    private ConcurAccountValidationService buildMockConcurAccountValidationService() {
//        ConcurAccountValidationService accountValidationService = Mockito.mock(ConcurAccountValidationService.class);
//        Mockito.when(accountValidationService.validateConcurAccountInfo(Mockito.any()))
//                .then(invocation -> mockValidateConcurAccountInfo(invocation.getArgument(0)));
//        return accountValidationService;
//    }
//
//    private ValidationResult mockValidateConcurAccountInfo(ConcurAccountInfo concurAccountInfo) {
//        List<String> messages = new ArrayList<>();
//        if (ObjectUtils.isNull(concurAccountInfo)) {
//            throw new IllegalArgumentException("concurAccountInfo should NEVER be null");
//        }
//        
//        if (StringUtils.isBlank(concurAccountInfo.getChart())) {
//            messages.add(VALIDATION_ERROR_MISSING_CHART);
//        }
//        
//        if (StringUtils.isBlank(concurAccountInfo.getAccountNumber())) {
//            messages.add(VALIDATION_ERROR_MISSING_ACCOUNT);
//        } else if (StringUtils.equals(ConcurTestConstants.ACCT_XXXXXXX, concurAccountInfo.getAccountNumber())) {
//            messages.add(VALIDATION_ERROR_INVALID_ACCOUNT);
//        }
//        
//        return new ValidationResult(messages.isEmpty(), messages);
//    }
//
//    static Stream<Arguments> travelRequests() {
//        return Stream.of(
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.PENDING_APPROVAL_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
//                RequestV4DetailFixture.CANCELED_TEST_REQUEST_JOHN_TEST,
//                RequestV4DetailFixture.APPROVED_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.SENTBACK_INVALID_TEST_REQUEST_JANE_DOE,
//                RequestV4DetailFixture.PENDING_COST_APPROVAL_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
//                RequestV4DetailFixture.NOT_SUBMITTED_REGULAR_REQUEST_BOB_SMITH
//        ).map(Arguments::of);
//    }
//
//    @Test
//    void testParsingOfTestUsersParameter() throws Exception {
//        Map<String, String> expectedMappings = buildExpectedUserIdMappings(
//                RequestV4PersonFixture.JOHN_TEST, RequestV4PersonFixture.JANE_DOE,
//                RequestV4PersonFixture.TEST_MANAGER, RequestV4PersonFixture.TEST_APPROVER);
//        assertParameterDerivedTestUserMapContainsExpectedEntries(expectedMappings);
//        
//        String newTestUsersParameterValue = buildTestUsersParameterValue(
//                RequestV4PersonFixture.BOB_SMITH, RequestV4PersonFixture.TEST_MANAGER);
//        overrideParameter(ConcurParameterConstants.REQUEST_V4_TEST_USERS, newTestUsersParameterValue);
//        
//        expectedMappings = buildExpectedUserIdMappings(
//                RequestV4PersonFixture.BOB_SMITH, RequestV4PersonFixture.TEST_MANAGER);
//        assertParameterDerivedTestUserMapContainsExpectedEntries(expectedMappings);
//    }
//
//    private Map<String, String> buildExpectedUserIdMappings(RequestV4PersonFixture... users) {
//        return Stream.of(users).collect(Collectors.toUnmodifiableMap(
//                userFixture -> userFixture.id, RequestV4PersonFixture::getNameForParameterEntry));
//    }
//
//    private void assertParameterDerivedTestUserMapContainsExpectedEntries(Map<String, String> expectedMappings) {
//        Map<String, String> actualMappings = requestV4Service.getRequestV4TestUserMappingsFromParameter();
//        assertNotNull(actualMappings, "The test users map should not have been null");
//        assertEquals(expectedMappings, actualMappings, "Wrong test user entries in map");
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {
//            KFSConstants.EMPTY_STRING,
//            KFSConstants.BLANK_SPACE,
//            "Name1=",
//            "=Value1",
//            "Name1=Value1;Name2=;Name3=Value3",
//            "Name1=Value1;Name2=Value2;=Value3"
//    })
//    void testParsingFailureForInvalidTestUserParameterValue(String invalidValue) {
//        overrideParameter(ConcurParameterConstants.REQUEST_V4_TEST_USERS, invalidValue);
//        assertThrows(RuntimeException.class, () -> requestV4Service.getRequestV4TestUserMappingsFromParameter(),
//                "The parsing of the test users parameter should have failed due to blank or malformed value: '"
//                        + invalidValue + "'");
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequests")
//    void testFindSingleTravelRequest(RequestV4DetailFixture fixture) throws Exception {
//        ConcurRequestV4ReportDTO requestDetail = requestV4Service.getTravelRequest(mockAccessToken, fixture.id);
//        assertExpectedTravelRequestWasRetrieved(fixture, requestDetail);
//    }
//
//    @Test
//    void testSearchForSingleNonexistentTravelRequest() throws Exception {
//        assertThrows(RuntimeException.class,
//                () -> requestV4Service.getTravelRequest(mockAccessToken, NONEXISTENT_REQUEST_ID),
//                "The search should have encountered an error when the Travel Request does not exist");
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequests")
//    void testSearchForSingleTravelRequestOnBrokenServer(RequestV4DetailFixture fixture) throws Exception {
//        mockConcurEndpoint.setForceServerError(true);
//        assertThrows(RuntimeException.class, () -> requestV4Service.getTravelRequest(mockAccessToken, fixture.id),
//                "The search should have encountered and error when the service endpoint is in a bad state");
//    }
//
//    @Test
//    void testFindTravelRequestListingForDefaultSettings() throws Exception {
//        assertSearchForRequestListingReturnsExpectedResults(
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1);
//    }
//
//    @Test
//    void testSearchForTravelRequestListingForDefaultSettingsOnBrokenServer() throws Exception {
//        mockConcurEndpoint.setForceServerError(true);
//        assertSearchForRequestListingEncountersAnError(RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1);
//    }
//
//    static Stream<Arguments> travelRequestListingQueries() {
//        return Stream.of(
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_01_02_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_04_06_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_04_07_DAYS_OLD_2,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119
//        ).map(Arguments::of);
//    }
//
//    static Stream<Arguments> travelRequestListingQueriesWithPageSizeOverrides() {
//        return Stream.of(
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_01_02_DAYS_OLD_1_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119_PAGE_SIZE_20
//        ).map(Arguments::of);
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequestListingQueries")
//    void testFindTravelRequestListingAfterOverridingNumDaysOldAndCurrentDate(
//            RequestV4ListingFixture expectedResults) throws Exception {
//        overrideQueryNumberOfDaysOld(expectedResults.daysOld);
//        overrideCurrentDate(expectedResults.currentDate);
//        assertSearchForRequestListingReturnsExpectedResults(expectedResults);
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequestListingQueriesWithPageSizeOverrides")
//    void testFindTravelRequestListingAfterOverridingDateSettingsAndPageSize(
//            RequestV4ListingFixture expectedResults) throws Exception {
//        overrideQueryNumberOfDaysOld(expectedResults.daysOld);
//        overrideCurrentDate(expectedResults.currentDate);
//        overrideQueryPageSize(expectedResults.pageSize);
//        assertSearchForRequestListingReturnsExpectedResults(expectedResults);
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequestListingQueries")
//    void testSearchForTravelRequestListingOnBrokenServer(RequestV4ListingFixture expectedResults) throws Exception {
//        mockConcurEndpoint.setForceServerError(true);
//        overrideQueryNumberOfDaysOld(expectedResults.daysOld);
//        overrideCurrentDate(expectedResults.currentDate);
//        assertSearchForRequestListingEncountersAnError(expectedResults);
//    }
//
//    @ParameterizedTest
//    @ValueSource(booleans = { true, false })
//    void testValidateTravelRequestsForDefaultSettings(boolean productionMode) throws Exception {
//        requestV4Service.setSimulateProductionMode(productionMode);
//        assertProcessingOfRequestListingHasExpectedResults(
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1, productionMode);
//    }
//
//    @ParameterizedTest
//    @ValueSource(booleans = { true, false })
//    void testValidateTravelRequestsForDefaultSettingsOnBrokenServer(boolean productionMode) throws Exception {
//        requestV4Service.setSimulateProductionMode(productionMode);
//        mockConcurEndpoint.setForceServerError(true);
//        assertProcessingOfRequestListingEncountersAnError();
//    }
//
//    static Stream<Arguments> travelRequestListingsForValidation() {
//        return Stream.of(
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_01_03_DAYS_OLD_1_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_01_02_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_01_02_DAYS_OLD_1_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_04_06_DAYS_OLD_1,
//                RequestV4ListingFixture.SEARCH_2022_04_07_DAYS_OLD_2,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119_PAGE_SIZE_5,
//                RequestV4ListingFixture.SEARCH_2022_04_30_DAYS_OLD_119_PAGE_SIZE_20
//        ).flatMap(ConcurRequestV4ServiceImplTest::flatMapToFixturesPairedWithTrueFalseProductionModeFlags)
//                .map(Arguments::of);
//    }
//
//    static Stream<Object[]> flatMapToFixturesPairedWithTrueFalseProductionModeFlags(RequestV4ListingFixture fixture) {
//        return Stream.of(new Object[] { fixture, true }, new Object[] { fixture, false });
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequestListingsForValidation")
//    void testValidateTravelRequestsAfterOverridingDateSettingsAndPageSize(
//            RequestV4ListingFixture expectedResults, boolean productionMode) throws Exception {
//        requestV4Service.setSimulateProductionMode(productionMode);
//        overrideQueryNumberOfDaysOld(expectedResults.daysOld);
//        overrideCurrentDate(expectedResults.currentDate);
//        overrideQueryPageSize(expectedResults.pageSize);
//        assertProcessingOfRequestListingHasExpectedResults(expectedResults, productionMode);
//    }
//
//    @ParameterizedTest
//    @MethodSource("travelRequestListingsForValidation")
//    void testValidateTravelRequestsUsingQueryOverridesAndBrokenServer(
//            RequestV4ListingFixture expectedResults, boolean productionMode) throws Exception {
//        mockConcurEndpoint.setForceServerError(true);
//        requestV4Service.setSimulateProductionMode(productionMode);
//        overrideQueryNumberOfDaysOld(expectedResults.daysOld);
//        overrideCurrentDate(expectedResults.currentDate);
//        overrideQueryPageSize(expectedResults.pageSize);
//        assertProcessingOfRequestListingEncountersAnError();
//    }
//
//    private void assertProcessingOfRequestListingEncountersAnError() {
//        assertThrows(RuntimeException.class, () -> requestV4Service.processTravelRequests(mockAccessToken));
//    }
//
//    private void assertProcessingOfRequestListingHasExpectedResults(
//            RequestV4ListingFixture expectedListing, boolean productionMode) {
//        Map<String, RequestV4DetailFixture> expectedResults = expectedListing
//                .getExpectedProcessedRequestsKeyedByRequestId(productionMode);
//        List<ConcurEventNotificationProcessingResultsDTO> actualResults = requestV4Service.processTravelRequests(
//                mockAccessToken);
//        assertRequestsWereValidatedAsExpected(expectedResults, actualResults);
//    }
//
//    private void assertRequestsWereValidatedAsExpected(Map<String, RequestV4DetailFixture> expectedResults,
//            List<ConcurEventNotificationProcessingResultsDTO> actualResults) {
//        Set<String> encounteredResults = new HashSet<>();
//        assertEquals(expectedResults.size(), actualResults.size(), "Wrong number of validation results");
//        
//        for (ConcurEventNotificationProcessingResultsDTO actualResult : actualResults) {
//            String requestId = actualResult.getReportNumber();
//            assertTrue(StringUtils.isNotBlank(requestId), "Validation result should have had a Request ID");
//            assertTrue(encounteredResults.add(requestId), "Unexpected duplicate result for Request ID: " + requestId);
//            
//            RequestV4DetailFixture expectedResult = expectedResults.get(requestId);
//            assertNotNull(expectedResult, "Unexpected validation result for Request ID: " + requestId);
//            
//            assertRequestWasValidatedAsExpected(expectedResult, actualResult);
//        }
//    }
//
//    private void assertRequestWasValidatedAsExpected(
//            RequestV4DetailFixture expectedResult, ConcurEventNotificationProcessingResultsDTO actualResult) {
//        assertEquals(expectedResult.requestId, actualResult.getReportNumber(), "Wrong Request ID for result");
//        assertEquals(ConcurEventNoticationVersion2EventType.TravelRequest, actualResult.getEventType(),
//                "Wrong validation event type for result");
//        assertEquals(expectedResult.getExpectedProcessingResult(), actualResult.getProcessingResults(),
//                "Wrong validation outcome for result");
//        if (expectedResult.isExpectedToPassAccountValidation()) {
//            assertTrue(CollectionUtils.isEmpty(actualResult.getMessages()),
//                    "Successful validation result should not have contained any error messages");
//        } else {
//            assertTrue(CollectionUtils.isNotEmpty(actualResult.getMessages()),
//                    "Unsuccessful validation result should have contained at least one error message");
//        }
//    }
//
//    private void assertSearchForRequestListingEncountersAnError(RequestV4ListingFixture expectedResults) {
//        requestV4Service.setSkipRequestListItemProcessing(true);
//        assertThrows(RuntimeException.class,
//                () -> requestV4Service.processTravelRequests(mockAccessToken));
//    }
//
//    private void assertSearchForRequestListingReturnsExpectedResults(RequestV4ListingFixture expectedResults) {
//        requestV4Service.setSkipRequestListItemProcessing(true);
//        String initialQueryUrl = requestV4Service.buildInitialRequestQueryUrl();
//        List<ConcurEventNotificationProcessingResultsDTO> processingResults = requestV4Service.processTravelRequests(
//                mockAccessToken);
//        assertEquals(0, processingResults.size(),
//                "No processing results should have been returned when running in skip-list-item mode");
//        List<ConcurRequestV4ListingDTO> actualListings = requestV4Service.getEncounteredRequestListings();
//        assertSearchReturnedExpectedResultListings(initialQueryUrl, expectedResults, actualListings);
//    }
//
//    private void assertSearchReturnedExpectedResultListings(String queryUrl,
//            RequestV4ListingFixture expectedResults, List<ConcurRequestV4ListingDTO> actualResults) {
//        assertEquals(expectedResults.totalPageCount, actualResults.size(), "Wrong number of result pages");
//        assertTravelRequestListingsHaveExpectedListItems(expectedResults, actualResults);
//        assertTravelRequestListingsHaveExpectedOperations(queryUrl, expectedResults, actualResults);
//    }
//
//    private void assertTravelRequestListingsHaveExpectedOperations(String queryUrl,
//            RequestV4ListingFixture expectedResults, List<ConcurRequestV4ListingDTO> actualResults) {
//        int pageIndex = 0;
//        for (ConcurRequestV4ListingDTO actualPage : actualResults) {
//            Map<String, Map<String, String>> expectedParametersForOperations = expectedResults
//                    .buildExpectedParametersForOperationsOnPage(pageIndex, queryUrl);
//            assertTravelRequestListingHasExpectedOperations(expectedParametersForOperations, actualPage);
//            pageIndex++;
//        }
//    }
//
//    private void assertTravelRequestListingHasExpectedOperations(
//            Map<String, Map<String, String>> expectedParametersForOperations, ConcurRequestV4ListingDTO actualPage) {
//        Set<String> encounteredOperations = new HashSet<>();
//        
//        assertNotNull(actualPage.getOperations(), "List of operations should have been present");
//        assertEquals(expectedParametersForOperations.size(), actualPage.getOperations().size(),
//                "Wrong number of operations");
//        for (ConcurRequestV4OperationDTO operation : actualPage.getOperations()) {
//            String actualName = operation.getName();
//            String actualHref = operation.getHref();
//            
//            assertTrue(StringUtils.isNotBlank(actualName), "Operation was missing a name");
//            assertTrue(StringUtils.isNotBlank(actualHref), "Operation was missing a URL");
//            assertTrue(encounteredOperations.add(actualName), "Unexpected duplicate operation: " + actualName);
//            
//            Map<String, String> expectedParameters = expectedParametersForOperations.get(actualName);
//            assertNotNull(expectedParameters, "Found an unexpected operation: " + actualName);
//            
//            String urlPrefix = StringUtils.substringBefore(actualHref, KFSConstants.QUESTION_MARK);
//            assertEquals(baseRequestV4Url, urlPrefix, "Wrong URL path for operation");
//            
//            Map<String, String> actualParameters = ConcurFixtureUtils.getQueryParametersFromUrl(actualHref);
//            assertEquals(expectedParameters, actualParameters, "Wrong query parameters for operation");
//        }
//    }
//
//    private void assertTravelRequestListingsHaveExpectedListItems(
//            RequestV4ListingFixture expectedResults, List<ConcurRequestV4ListingDTO> actualResults) {
//        Set<String> encounteredRequestUuids = new HashSet<>();
//        int pageIndex = 0;
//        
//        for (ConcurRequestV4ListingDTO actualPage : actualResults) {
//            int expectedResultCountForPage = expectedResults.calculateExpectedResultCountForPage(pageIndex);
//            assertEquals(expectedResults.totalResultRowCount, actualPage.getTotalCount(), "Wrong total row count");
//            assertNotNull(actualPage.getListItems(), "Collection of list items should not have been null");
//            assertEquals(expectedResultCountForPage, actualPage.getListItems().size(),
//                    "Wrong number of items on results page at index " + pageIndex);
//            
//            for (ConcurRequestV4ListItemDTO listItem : actualPage.getListItems()) {
//                String requestUuid = listItem.getId();
//                assertTrue(StringUtils.isNotBlank(requestUuid), "List item was missing a Request UUID");
//                assertTrue(encounteredRequestUuids.add(requestUuid),
//                        "Unexpected duplicate item for UUID: " + requestUuid);
//                
//                RequestV4DetailFixture expectedItem = expectedResults.searchResults.get(requestUuid);
//                assertNotNull(expectedItem, "Encountered an unexpected list item with UUID: " + requestUuid);
//                assertExpectedTravelRequestListItemWasRetrieved(expectedItem, listItem);
//            }
//            pageIndex++;
//        }
//    }
//
//    private void assertExpectedTravelRequestListItemWasRetrieved(
//            RequestV4DetailFixture expectedResult, ConcurRequestV4ListItemDTO actualResult) {
//        assertEquals(expectedResult.id, actualResult.getId(), "Wrong Request UUID");
//        assertEquals(expectedResult.buildRequestHref(baseRequestV4Url), actualResult.getHref(), "Wrong Request HREF");
//        assertUserIsPresent(expectedResult.owner, actualResult.getOwner(), "Request Owner");
//        if (expectedResult.shouldAddApproverToRequestDTO()) {
//            assertUserIsPresent(expectedResult.pendingApprover.get(), actualResult.getApprover(), "Request Approver");
//        } else {
//            assertNull(actualResult.getApprover(), "Request Approver should not have been present");
//        }
//        assertRequestHasCorrectStatus(expectedResult.approvalStatus, actualResult.getApprovalStatus());
//        assertRequestHasCorrectDate(expectedResult.startDate, actualResult.getStartDate(), "Start Date");
//        assertRequestHasCorrectDate(expectedResult.endDate, actualResult.getEndDate(), "End Date");
//        assertRequestHasCorrectDate(expectedResult.creationDate, actualResult.getCreationDate(), "Creation Date");
//        assertRequestHasCorrectDate(expectedResult.submitDate, actualResult.getSubmitDate(), "Submit Date");
//    }
//
//    private void assertExpectedTravelRequestWasRetrieved(
//            RequestV4DetailFixture expectedResult, ConcurRequestV4ReportDTO actualResult) {
//        assertEquals(expectedResult.id, actualResult.getId(), "Wrong Request UUID");
//        assertEquals(expectedResult.buildRequestHref(baseRequestV4Url), actualResult.getHref(), "Wrong Request HREF");
//        assertUserIsPresent(expectedResult.owner, actualResult.getOwner(), "Request Owner");
//        if (expectedResult.shouldAddApproverToRequestDTO()) {
//            assertUserIsPresent(expectedResult.pendingApprover.get(), actualResult.getApprover(), "Request Approver");
//        } else {
//            assertNull(actualResult.getApprover(), "Request Approver should not have been present");
//        }
//        assertRequestHasCorrectStatus(expectedResult.approvalStatus, actualResult.getApprovalStatus());
//        assertRequestHasCorrectCustomItem(expectedResult.chartCode, actualResult.getChart(), "Chart Code");
//        assertRequestHasCorrectCustomItem(expectedResult.accountNumber, actualResult.getAccount(), "Account Number");
//        assertRequestHasCorrectCustomItem(expectedResult.subAccountNumber, actualResult.getSubAccount(),
//                "Sub-Account Number");
//        assertRequestHasCorrectCustomItem(expectedResult.subObjectCode, actualResult.getSubObjectCode(),
//                "Sub-Object Code");
//        assertRequestHasCorrectCustomItem(expectedResult.projectCode, actualResult.getProjectCode(), "Project Code");
//        assertRequestHasCorrectDate(expectedResult.startDate, actualResult.getStartDate(), "Start Date");
//        assertRequestHasCorrectDate(expectedResult.endDate, actualResult.getEndDate(), "End Date");
//        assertRequestHasCorrectDate(expectedResult.creationDate, actualResult.getCreationDate(), "Creation Date");
//        assertRequestHasCorrectDate(expectedResult.submitDate, actualResult.getSubmitDate(), "Submit Date");
//        assertRequestHasCorrectDate(expectedResult.lastModifiedDate, actualResult.getLastModifiedDate(),
//                "LastModified Date");
//    }
//
//    private void assertUserIsPresent(RequestV4PersonFixture expectedPerson, ConcurRequestV4PersonDTO actualPerson,
//            String personLabel) {
//        assertNotNull(actualPerson, personLabel + " should have been present");
//        assertEquals(expectedPerson.id, actualPerson.getId(), "Wrong UUID for " + personLabel);
//        assertEquals(expectedPerson.firstName, actualPerson.getFirstName(), "Wrong first name for " + personLabel);
//        assertEquals(expectedPerson.lastName, actualPerson.getLastName(), "Wrong last name for " + personLabel);
//        if (StringUtils.isNotBlank(expectedPerson.middleInitial)) {
//            assertEquals(expectedPerson.middleInitial, actualPerson.getMiddleInitial(),
//                    "Wrong middle initial for " + personLabel);
//        } else {
//            assertTrue(StringUtils.isBlank(actualPerson.getMiddleInitial()),
//                    personLabel + " should not have had a middle initial");
//        }
//    }
//
//    private void assertRequestHasCorrectStatus(RequestV4Status expectedStatus, ConcurRequestV4StatusDTO actualStatus) {
//        assertNotNull(actualStatus, "Request Status DTO should have been present");
//        assertEquals(expectedStatus.code, actualStatus.getCode(), "Wrong status code for request");
//        assertEquals(expectedStatus.name, actualStatus.getName(), "Wrong status name for request");
//    }
//
//    private void assertRequestHasCorrectCustomItem(String expectedItemCode, ConcurRequestV4CustomItemDTO actualItem,
//            String itemLabel) {
//        if (StringUtils.isNotBlank(expectedItemCode)) {
//            assertNotNull(actualItem, itemLabel + " should have been present");
//            assertEquals(expectedItemCode, actualItem.getCode(), "Wrong custom field value for " + itemLabel);
//        } else {
//            assertNull(actualItem, itemLabel + " should not have been present");
//        }
//    }
//
//    private void assertRequestHasCorrectDate(DateTime expectedDate, Date actualDate, String dateLabel) {
//        if (expectedDate != null) {
//            assertNotNull(actualDate, dateLabel + " should have been present");
//            assertEquals(expectedDate.toDate(), actualDate, "Wrong date-time value for " + dateLabel);
//        } else {
//            assertNull(actualDate, dateLabel + " should not have been present");
//        }
//    }
//
//    private static class TestConcurRequestV4ServiceImpl extends ConcurRequestV4ServiceImpl {
//        private boolean simulateProductionMode;
//        private List<ConcurRequestV4ListingDTO> encounteredRequestListings = new ArrayList<>();
//        private boolean skipRequestListItemProcessing;
//        
//        @Override
//        protected Stream<ConcurEventNotificationProcessingResultsDTO> processTravelRequestsSubset(
//                String accessToken, Map<String, String> testUserIdMappings, ConcurRequestV4ListingDTO requestListing) {
//            encounteredRequestListings.add(requestListing);
//            if (skipRequestListItemProcessing) {
//                return Stream.empty();
//            } else {
//                return super.processTravelRequestsSubset(accessToken, testUserIdMappings, requestListing);
//            }
//        }
//        
//        @Override
//        protected boolean isProduction() {
//            return simulateProductionMode;
//        }
//        
//        public void setSimulateProductionMode(boolean simulateProductionMode) {
//            this.simulateProductionMode = simulateProductionMode;
//        }
//        
//        public List<ConcurRequestV4ListingDTO> getEncounteredRequestListings() {
//            return encounteredRequestListings;
//        }
//        
//        public void setSkipRequestListItemProcessing(boolean skipRequestListItemProcessing) {
//            this.skipRequestListItemProcessing = skipRequestListItemProcessing;
//        }
//    }

}
