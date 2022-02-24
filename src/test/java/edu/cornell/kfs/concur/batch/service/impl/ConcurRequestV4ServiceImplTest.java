package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4ListingFixture;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4PersonFixture;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
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

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurRequestV4ServiceImplTest {

    private static final String DEFAULT_MOCK_CURRENT_DATE = "01/03/2022 11:22:33";
    private static final String VALIDATION_ERROR_MISSING_CHART = "Missing Chart Code";
    private static final String VALIDATION_ERROR_MISSING_ACCOUNT = "Missing Account Number";
    private static final String VALIDATION_ERROR_INVALID_ACCOUNT = "Invalid Account Number";
    

    private String mockAccessToken;
    private MockConcurRequestV4Server mockConcurBackendServer;
    private MockConcurRequestV4ServiceEndpoint mockConcurEndpoint;
    private MockRemoteServerExtension mockHttpServer;
    private String baseRequestV4Url;
    private Map<String, String> concurParameters;
    private Map<String, String> concurProperties;
    private TestDateTimeServiceImpl testDateTimeService;
    private TestConcurRequestV4ServiceImpl requestV4Service;
    private long mockCurrentTimeMillis;

    @BeforeEach
    void setUp() throws Exception {
        mockAccessToken = buildMockAccessTokenFromUUID();
        mockConcurEndpoint = new MockConcurRequestV4ServiceEndpoint(mockAccessToken,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_APPROVAL_TEST_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.CANCELED_TEST_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.APPROVED_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.SENTBACK_INVALID_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_COST_APPROVAL_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.NOT_SUBMITTED_REGULAR_REQUEST_BOB_SMITH);
        
        mockHttpServer = new MockRemoteServerExtension();
        mockHttpServer.initialize(mockConcurEndpoint);
        mockConcurBackendServer = mockConcurEndpoint.getMockBackendServer();
        baseRequestV4Url = mockConcurEndpoint.getBaseRequestV4Url();
        
        concurParameters = buildTestParameters(baseRequestV4Url);
        concurProperties = buildTestProperties();
        
        ConcurBatchUtilityService mockConcurBatchUtilityService = buildMockConcurBatchUtilityService();
        testDateTimeService = buildDateTimeService();
        mockCurrentTimeMillis = getTimeInMilliseconds(DEFAULT_MOCK_CURRENT_DATE);
        
        requestV4Service = new TestConcurRequestV4ServiceImpl();
        requestV4Service.setSimulateProductionMode(false);
        requestV4Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        requestV4Service.setConcurEventNotificationV2WebserviceService(
                buildConcurEventNotificationV2WebserviceService(mockConcurBatchUtilityService));
        requestV4Service.setConfigurationService(buildMockConfigurationService());
        requestV4Service.setDateTimeService(buildDateTimeServiceWithOverridableCurrentDate(testDateTimeService));
        requestV4Service.setConcurAccountValidationService(buildMockConcurAccountValidationService());
    }

    @SuppressWarnings("deprecation")
    @AfterEach
    void tearDown() throws Exception {
        IOUtils.closeQuietly(mockConcurEndpoint);
        IOUtils.closeQuietly(mockHttpServer);
        IOUtils.closeQuietly(mockConcurBackendServer);
        mockAccessToken = null;
        concurParameters = null;
        concurProperties = null;
        mockConcurBackendServer = null;
        mockConcurEndpoint = null;
        mockHttpServer = null;
        requestV4Service = null;
    }

    private String buildMockAccessTokenFromUUID() {
        return StringUtils.strip(UUID.randomUUID().toString(), KFSConstants.DASH);
    }

    private Map<String, String> buildTestParameters(String requestV4Endpoint) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, ParameterTestValues.MAX_RETRIES_1);
        parameters.put(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE,
                ParameterTestValues.DEFAULT_OBJECT_CODE_5500);
        parameters.put(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT, requestV4Endpoint);
        parameters.put(ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE,
                ParameterTestValues.REQUEST_V4_PAGE_SIZE_2);
        parameters.put(ConcurParameterConstants.REQUEST_V4_TEST_APPROVERS, buildTestApproversParameterValue());
        return parameters;
    }

    private String buildTestApproversParameterValue() {
        return Stream.of(RequestV4PersonFixture.TEST_MANAGER, RequestV4PersonFixture.TEST_APPROVER)
                .map(fixture -> fixture.firstName + CUKFSConstants.EQUALS_SIGN + fixture.id)
                .collect(Collectors.joining(CUKFSConstants.SEMICOLON));
    }

    private ConcurBatchUtilityService buildMockConcurBatchUtilityService() {
        ConcurBatchUtilityService concurBatchUtilityService = Mockito.mock(ConcurBatchUtilityService.class);
        Mockito.when(concurBatchUtilityService.getConcurParameterValue(Mockito.anyString()))
                .then(invocation -> concurParameters.get(invocation.getArgument(0)));
        return concurBatchUtilityService;
    }

    private Map<String, String> buildTestProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING,
                PropertyTestValues.REQUESTV4_REQUEST_LIST_SEARCH_MESSAGE);
        properties.put(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_REQUEST,
                PropertyTestValues.REQUESTV4_SINGLE_REQUEST_SEARCH_MESSAGE);
        return properties;
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(Mockito.anyString()))
                .then(invocation -> concurProperties.get(invocation.getArgument(0)));
        return configurationService;
    }

    private TestDateTimeServiceImpl buildDateTimeService() throws Exception {
        TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        return dateTimeService;
    }

    private DateTimeService buildDateTimeServiceWithOverridableCurrentDate(
            TestDateTimeServiceImpl actualDateTimeService) {
        TestDateTimeServiceImpl dateTimeService = Mockito.spy(actualDateTimeService);
        Mockito.doAnswer(invocation -> getMockCurrentDate())
                .when(dateTimeService).getCurrentDate();
        return dateTimeService;
    }

    private Date getMockCurrentDate() {
        if (mockCurrentTimeMillis <= 0L) {
            throw new IllegalStateException("The mocked current-time setting may not have been initialized");
        }
        return new Date(mockCurrentTimeMillis);
    }

    private long getTimeInMilliseconds(String dateString) throws ParseException {
        if (testDateTimeService == null) {
            throw new IllegalStateException("testDateTimeService has not been initialized yet");
        }
        Date parsedDate = testDateTimeService.convertToDateTime(dateString);
        return parsedDate.getTime();
    }

    private void overrideCurrentTime(String newDate) throws ParseException {
        mockCurrentTimeMillis = getTimeInMilliseconds(newDate);
    }

    private ConcurEventNotificationV2WebserviceService buildConcurEventNotificationV2WebserviceService(
            ConcurBatchUtilityService mockConcurBatchUtilityService) {
        ConcurEventNotificationV2WebserviceServiceImpl notificationV2Service
                = new ConcurEventNotificationV2WebserviceServiceImpl();
        notificationV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        return notificationV2Service;
    }

    private ConcurAccountValidationService buildMockConcurAccountValidationService() {
        ConcurAccountValidationService accountValidationService = Mockito.mock(ConcurAccountValidationService.class);
        Mockito.when(accountValidationService.validateConcurAccountInfo(Mockito.any()))
                .then(invocation -> mockValidateConcurAccountInfo(invocation.getArgument(0)));
        return accountValidationService;
    }

    private ValidationResult mockValidateConcurAccountInfo(ConcurAccountInfo concurAccountInfo) {
        List<String> messages = new ArrayList<>();
        if (ObjectUtils.isNull(concurAccountInfo)) {
            throw new IllegalArgumentException("concurAccountInfo should NEVER be null");
        }
        
        if (StringUtils.isBlank(concurAccountInfo.getChart())) {
            messages.add(VALIDATION_ERROR_MISSING_CHART);
        }
        
        if (StringUtils.isBlank(concurAccountInfo.getAccountNumber())) {
            messages.add(VALIDATION_ERROR_MISSING_ACCOUNT);
        } else if (StringUtils.equals(ConcurTestConstants.ACCT_XXXXXXX, concurAccountInfo.getAccountNumber())) {
            messages.add(VALIDATION_ERROR_INVALID_ACCOUNT);
        }
        
        return new ValidationResult(messages.isEmpty(), messages);
    }

    @Test
    void testFindSingleTravelRequest() throws Exception {
        RequestV4DetailFixture fixture = RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE;
        ConcurRequestV4ReportDTO requestDetail = requestV4Service.findTravelRequest(mockAccessToken, fixture.id);
        assertExpectedTravelRequestWasRetrieved(fixture, requestDetail);
    }

    @Test
    void testFindTravelRequestListingForDefaultSettings() throws Exception {
        String queryUrl = requestV4Service.buildInitialRequestQueryUrl(Optional.empty());
        ConcurRequestV4ListingDTO requestListing = requestV4Service.findPendingTravelRequests(
                mockAccessToken, 1, queryUrl);
        assertExpectedTravelRequestListingWasRetrieved(
                queryUrl, RequestV4ListingFixture.DEFAULT_ALL_SEARCH_2022_01_03_PAGE_1, requestListing);
    }

    private void assertExpectedTravelRequestListingWasRetrieved(String queryUrl,
            RequestV4ListingFixture expectedResult, ConcurRequestV4ListingDTO actualResult) {
        assertEquals(expectedResult.totalCount, actualResult.getTotalCount(), "Wrong total count for search");
        assertTravelRequestListingHasExpectedListItems(expectedResult, actualResult);
        assertTravelRequestListingHasExpectedOperations(queryUrl, expectedResult, actualResult);
    }

    private void assertTravelRequestListingHasExpectedOperations(String queryUrl,
            RequestV4ListingFixture expectedResult, ConcurRequestV4ListingDTO actualResult) {
        Map<String, Map<String, String>> expectedParametersForOperations = expectedResult
                .buildExpectedParametersForOperations(queryUrl);
        Set<String> encounteredOperations = new HashSet<>();
        
        assertNotNull(actualResult.getOperations(), "List of operations should have been present");
        assertEquals(expectedParametersForOperations.size(), actualResult.getOperations().size(),
                "Wrong number of operations");
        for (ConcurRequestV4OperationDTO operation : actualResult.getOperations()) {
            String actualName = operation.getName();
            String actualHref = operation.getHref();
            
            assertTrue(StringUtils.isNotBlank(actualName), "Operation was missing a name");
            assertTrue(StringUtils.isNotBlank(actualHref), "Operation was missing a URL");
            assertTrue(encounteredOperations.add(actualName), "Unexpected duplicate operation: " + actualName);
            
            Map<String, String> expectedParameters = expectedParametersForOperations.get(actualName);
            assertNotNull(expectedParameters, "Found an unexpected operation: " + actualName);
            
            String urlPrefix = StringUtils.substringBefore(actualHref, KFSConstants.QUESTION_MARK);
            assertEquals(baseRequestV4Url, urlPrefix, "Wrong URL path for operation");
            
            Map<String, String> actualParameters = ConcurFixtureUtils.getQueryParametersFromUrl(actualHref);
            assertEquals(expectedParameters, actualParameters, "Wrong query parameters for operation");
        }
    }

    private void assertTravelRequestListingHasExpectedListItems(
            RequestV4ListingFixture expectedResult, ConcurRequestV4ListingDTO actualResult) {
        Set<String> encounteredRequestUuids = new HashSet<>();
        assertNotNull(actualResult.getListItems(), "Collection of list items should not have been null");
        assertEquals(expectedResult.pageResults.size(), actualResult.getListItems().size(),
                "Wrong number of items on current page of results");
        
        for (ConcurRequestV4ListItemDTO listItem : actualResult.getListItems()) {
            String requestUuid = listItem.getId();
            assertTrue(StringUtils.isNotBlank(requestUuid), "List item was missing a Request UUID");
            assertTrue(encounteredRequestUuids.add(requestUuid), "Unexpected duplicate item for UUID: " + requestUuid);
            
            RequestV4DetailFixture expectedItem = expectedResult.pageResults.get(requestUuid);
            assertNotNull(expectedItem, "Encountered an unexpected list item with UUID: " + requestUuid);
            assertExpectedTravelRequestListItemWasRetrieved(expectedItem, listItem);
        }
    }

    private void assertExpectedTravelRequestListItemWasRetrieved(
            RequestV4DetailFixture expectedResult, ConcurRequestV4ListItemDTO actualResult) {
        assertEquals(expectedResult.id, actualResult.getId(), "Wrong Request UUID");
        assertEquals(expectedResult.buildRequestHref(baseRequestV4Url), actualResult.getHref(), "Wrong Request HREF");
        assertUserIsPresent(expectedResult.owner, actualResult.getOwner(), "Request Owner");
        if (expectedResult.shouldAddApproverToRequestDTO()) {
            assertUserIsPresent(expectedResult.approvers.get(0), actualResult.getApprover(), "Request Approver");
        } else {
            assertNull(actualResult.getApprover(), "Request Approver should not have been present");
        }
        assertRequestHasCorrectStatus(expectedResult.approvalStatus, actualResult.getApprovalStatus());
        assertRequestHasCorrectDate(expectedResult.startDate, actualResult.getStartDate(), "Start Date");
        assertRequestHasCorrectDate(expectedResult.endDate, actualResult.getEndDate(), "End Date");
        assertRequestHasCorrectDate(expectedResult.creationDate, actualResult.getCreationDate(), "Creation Date");
        assertRequestHasCorrectDate(expectedResult.submitDate, actualResult.getSubmitDate(), "Submit Date");
    }

    private void assertExpectedTravelRequestWasRetrieved(
            RequestV4DetailFixture expectedResult, ConcurRequestV4ReportDTO actualResult) {
        assertEquals(expectedResult.id, actualResult.getId(), "Wrong Request UUID");
        assertEquals(expectedResult.buildRequestHref(baseRequestV4Url), actualResult.getHref(), "Wrong Request HREF");
        assertUserIsPresent(expectedResult.owner, actualResult.getOwner(), "Request Owner");
        if (expectedResult.shouldAddApproverToRequestDTO()) {
            assertUserIsPresent(expectedResult.approvers.get(0), actualResult.getApprover(), "Request Approver");
        } else {
            assertNull(actualResult.getApprover(), "Request Approver should not have been present");
        }
        assertRequestHasCorrectStatus(expectedResult.approvalStatus, actualResult.getApprovalStatus());
        assertRequestHasCorrectCustomItem(expectedResult.chartCode, actualResult.getChart(), "Chart Code");
        assertRequestHasCorrectCustomItem(expectedResult.accountNumber, actualResult.getAccount(), "Account Number");
        assertRequestHasCorrectCustomItem(expectedResult.subAccountNumber, actualResult.getSubAccount(),
                "Sub-Account Number");
        assertRequestHasCorrectCustomItem(expectedResult.subObjectCode, actualResult.getSubObjectCode(),
                "Sub-Object Code");
        assertRequestHasCorrectCustomItem(expectedResult.projectCode, actualResult.getProjectCode(), "Project Code");
        assertRequestHasCorrectDate(expectedResult.startDate, actualResult.getStartDate(), "Start Date");
        assertRequestHasCorrectDate(expectedResult.endDate, actualResult.getEndDate(), "End Date");
        assertRequestHasCorrectDate(expectedResult.creationDate, actualResult.getCreationDate(), "Creation Date");
        assertRequestHasCorrectDate(expectedResult.submitDate, actualResult.getSubmitDate(), "Submit Date");
        assertRequestHasCorrectDate(expectedResult.lastModifiedDate, actualResult.getLastModifiedDate(),
                "LastModified Date");
    }

    private void assertUserIsPresent(RequestV4PersonFixture expectedPerson, ConcurRequestV4PersonDTO actualPerson,
            String personLabel) {
        assertNotNull(actualPerson, personLabel + " should have been present");
        assertEquals(expectedPerson.id, actualPerson.getId(), "Wrong UUID for " + personLabel);
        assertEquals(expectedPerson.firstName, actualPerson.getFirstName(), "Wrong first name for " + personLabel);
        assertEquals(expectedPerson.lastName, actualPerson.getLastName(), "Wrong last name for " + personLabel);
        if (StringUtils.isNotBlank(expectedPerson.middleInitial)) {
            assertEquals(expectedPerson.middleInitial, actualPerson.getMiddleInitial(),
                    "Wrong middle initial for " + personLabel);
        } else {
            assertTrue(StringUtils.isBlank(actualPerson.getMiddleInitial()),
                    personLabel + " should not have had a middle initial");
        }
    }

    private void assertRequestHasCorrectStatus(RequestV4Status expectedStatus, ConcurRequestV4StatusDTO actualStatus) {
        assertNotNull(actualStatus, "Request Status DTO should have been present");
        assertEquals(expectedStatus.code, actualStatus.getCode(), "Wrong status code for request");
        assertEquals(expectedStatus.name, actualStatus.getName(), "Wrong status name for request");
    }

    private void assertRequestHasCorrectCustomItem(String expectedItemCode, ConcurRequestV4CustomItemDTO actualItem,
            String itemLabel) {
        if (StringUtils.isNotBlank(expectedItemCode)) {
            assertNotNull(actualItem, itemLabel + " should have been present");
            assertEquals(expectedItemCode, actualItem.getCode(), "Wrong custom field value for " + itemLabel);
        } else {
            assertNull(actualItem, itemLabel + " should not have been present");
        }
    }

    private void assertRequestHasCorrectDate(DateTime expectedDate, Date actualDate, String dateLabel) {
        if (expectedDate != null) {
            assertNotNull(actualDate, dateLabel + " should have been present");
            assertEquals(expectedDate.toDate(), actualDate, "Wrong date-time value for " + dateLabel);
        } else {
            assertNull(actualDate, dateLabel + " should not have been present");
        }
    }

    private static class TestConcurRequestV4ServiceImpl extends ConcurRequestV4ServiceImpl {
        private boolean simulateProductionMode;
        
        @Override
        protected boolean isProduction() {
            return simulateProductionMode;
        }
        
        public void setSimulateProductionMode(boolean simulateProductionMode) {
            this.simulateProductionMode = simulateProductionMode;
        }
    }

}