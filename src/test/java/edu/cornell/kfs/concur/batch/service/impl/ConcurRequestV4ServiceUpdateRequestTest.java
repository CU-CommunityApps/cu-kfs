package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationApiService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.concur.util.MockConcurUtils;
import edu.cornell.kfs.concur.web.mock.MockConcurRequestV4WorkflowController;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurRequestV4ServiceUpdateRequestTest {

    private static final String TEST_BEARER_TOKEN = "ABCDEFG1234567abcdefg1234567ABCDEFG1234567";
    private static final String INVALID_BEARER_TOKEN = "0BCDEFG1234567abcdefg1234567ABCDEFG1234567";
    private static final String INVALID_WORKFLOW_ENDPOINT = "/travelrequest/v4/unknown";
    private static final String MESSAGE_INACTIVE_CHART = "Chart code is inactive";
    private static final String MESSAGE_MISSING_ACCOUNT = "Account number is missing";

    private enum WorkflowOutcome {
        SUCCESS(true),
        SKIP(false),
        ERROR_ON_ACTION(false),
        ERROR_ON_ACTION_FOR_MISSING_REQUEST(false),
        ERROR_ON_DUPLICATE_ACTION(false),
        ERROR_ANALYZING_RESPONSE(true);
        
        public final boolean expectsActionTaken;
        
        private WorkflowOutcome(boolean expectsActionTaken) {
            this.expectsActionTaken = expectsActionTaken;
        }
    }

    @RegisterExtension
    static MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private static MockConcurRequestV4WorkflowController mockEndpoint;

    private String accessToken;
    private ConcurBatchUtilityService mockConcurBatchUtilityService;
    private TestConcurRequestV4ServiceImpl concurRequestV4Service;

    @BeforeAll
    static void setUpMockEndpoint() throws Exception {
        mockEndpoint = new MockConcurRequestV4WorkflowController(TEST_BEARER_TOKEN, webServerExtension.getServerUrl());
        webServerExtension.initializeStandaloneMockMvcWithControllers(mockEndpoint);
    }

    @BeforeEach
    void setUp() throws Exception {
        this.accessToken = TEST_BEARER_TOKEN;
        this.mockConcurBatchUtilityService = createMockConcurBatchUtilityService(webServerExtension.getServerUrl());
        
        this.concurRequestV4Service = new TestConcurRequestV4ServiceImpl();
        concurRequestV4Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        concurRequestV4Service.setConcurEventNotificationApiService(
                createConcurEventNotificationApiService(mockConcurBatchUtilityService));
        concurRequestV4Service.setConfigurationService(createMockConfigurationService());
        concurRequestV4Service.setConcurAccountValidationService(Mockito.mock(ConcurAccountValidationService.class));
        concurRequestV4Service.setSimulateProduction(true);
        
        mockEndpoint.addTravelRequestsAwaitingAction(
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST);
    }

    @AfterEach
    void tearDown() throws Exception {
        concurRequestV4Service = null;
        mockConcurBatchUtilityService = null;
        accessToken = null;
    }

    @AfterAll
    static void tearDownMockEndpoint() throws Exception {
        mockEndpoint = null;
    }

    private ConcurBatchUtilityService createMockConcurBatchUtilityService(String serverUrl) {
        return MockConcurUtils.createMockConcurBatchUtilityServiceBackedByParameters(
                Map.entry(ConcurParameterConstants.CONCUR_GEOLOCATION_URL, serverUrl),
                Map.entry(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT,
                        ParameterTestValues.REQUEST_V4_RELATIVE_ENDPOINT),
                Map.entry(ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND,
                        KFSConstants.ACTIVE_INDICATOR),
                Map.entry(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, String.valueOf(1)));
    }

    private ConcurEventNotificationApiService createConcurEventNotificationApiService(
            ConcurBatchUtilityService concurBatchUtilityService) {
        ConcurEventNotificationApiServiceImpl concurEventNotificationApiService
                = new ConcurEventNotificationApiServiceImpl();
        concurEventNotificationApiService.setConcurBatchUtilityService(concurBatchUtilityService);
        return concurEventNotificationApiService;
    }

    private void adjustProductionMode(RequestV4DetailFixture requestFixture) {
        concurRequestV4Service.setSimulateProduction(!requestFixture.isOwnedByTestUser());
    }

    static Stream<RequestV4DetailFixture> travelRequests() {
        return Stream.of(
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST);
    }

    static Stream<RequestV4DetailFixture> nonExistentTravelRequests() {
        return Stream.of(
                RequestV4DetailFixture.NOT_SUBMITTED_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_COST_APPROVAL_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.APPROVED_TEST_REQUEST_JANE_DOE,
                RequestV4DetailFixture.CANCELED_TEST_REQUEST_JOHN_TEST);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testUpdateStatusesForRequests(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.SUCCESS);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testUpdateStatusesForRequestsWithProcessingErrors(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        ConcurEventNotificationResponse resultsDTO = createProcessingErrorResultsForRequest(
                requestFixture);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.SUCCESS, resultsDTO);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testActionHandlingWhenTestWorkflowParameterIsOff(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND, KFSConstants.ParameterValues.NO);
        WorkflowOutcome expectedOutcome = requestFixture.isOwnedByTestUser()
                ? WorkflowOutcome.SKIP : WorkflowOutcome.SUCCESS;
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, expectedOutcome);
    }

    @ParameterizedTest
    @MethodSource("nonExistentTravelRequests")
    void testCannotUpdateStatusesForNonExistentRequests(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture,
                WorkflowOutcome.ERROR_ON_ACTION_FOR_MISSING_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testCannotUpdateStatusesWhenCallingInvalidEndpoint(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        mockConcurBatchUtilityService.setConcurParameterValue(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT,
                INVALID_WORKFLOW_ENDPOINT);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.ERROR_ON_ACTION);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testCannotUpdateStatusesWhenUsingInvalidAccessToken(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        accessToken = INVALID_BEARER_TOKEN;
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.ERROR_ON_ACTION);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testCannotUpdateStatusesWhenServerMalfunctions(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        mockEndpoint.setForceInternalServerError(true);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.ERROR_ON_ACTION);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testHandleMalformedResponseAfterSuccessfulAction(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        mockEndpoint.setForceMalformedResponse(true);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.ERROR_ANALYZING_RESPONSE);
    }

    @ParameterizedTest
    @MethodSource("travelRequests")
    void testCannotTakeActionTwiceOnSameRequest(RequestV4DetailFixture requestFixture) throws Exception {
        adjustProductionMode(requestFixture);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.SUCCESS);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, WorkflowOutcome.ERROR_ON_DUPLICATE_ACTION);
    }

    @Test
    void testUpdateStatusesOfMultipleRequests() throws Exception {
        RequestV4DetailFixture[] requestFixtures = Stream.concat(travelRequests(), nonExistentTravelRequests())
                .filter(fixture -> !fixture.isOwnedByTestUser())
                .toArray(RequestV4DetailFixture[]::new);
        Set<RequestV4DetailFixture> nonExistentRequests = nonExistentTravelRequests()
                .filter(fixture -> !fixture.isOwnedByTestUser())
                .collect(Collectors.toUnmodifiableSet());
        
        for (RequestV4DetailFixture requestFixture : requestFixtures) {
            WorkflowOutcome expectedOutcome = nonExistentRequests.contains(requestFixture)
                    ? WorkflowOutcome.ERROR_ON_ACTION_FOR_MISSING_REQUEST : WorkflowOutcome.SUCCESS;
            assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, expectedOutcome);
        }
    }

    private void assertRequestWorkflowAttemptHasExpectedOutcome(RequestV4DetailFixture requestFixture,
            WorkflowOutcome expectedOutcome) {
        ConcurEventNotificationResponse resultsDTO = createProcessingResultsForRequest(requestFixture);
        assertRequestWorkflowAttemptHasExpectedOutcome(requestFixture, expectedOutcome, resultsDTO);
    }

    private void assertRequestWorkflowAttemptHasExpectedOutcome(RequestV4DetailFixture requestFixture,
            WorkflowOutcome expectedOutcome, ConcurEventNotificationResponse resultsDTO) {
        String requestUuid = requestFixture.id;
        ConcurEventNotificationResponse oldResultsDTO = new ConcurEventNotificationResponse(
                resultsDTO);
        boolean requestIsPresent = expectedOutcome != WorkflowOutcome.ERROR_ON_ACTION_FOR_MISSING_REQUEST;
        
        ConcurTestWorkflowInfo oldWorkflowInfo = mockEndpoint.getWorkflowInfoForTravelRequest(requestUuid);
        if (expectedOutcome == WorkflowOutcome.ERROR_ON_DUPLICATE_ACTION) {
            assertWorkflowActionWasRecorded(requestUuid, oldResultsDTO, oldWorkflowInfo.getVersionNumber());
        } else if (requestIsPresent) {
            assertTravelRequestIsAwaitingAction(requestUuid, oldWorkflowInfo);
        } else {
            assertNull(oldWorkflowInfo, "The mock server should not have had an entry for request " + requestUuid);
        }
        
        assertWorkflowAttemptSucceedsOrFailsAsExpected(requestUuid, expectedOutcome, resultsDTO);
        
        if (expectedOutcome.expectsActionTaken) {
            assertWorkflowActionWasRecorded(requestUuid, oldResultsDTO, oldWorkflowInfo.getVersionNumber() + 1);
        } else {
            assertWorkflowActionWasNotRecorded(requestUuid, expectedOutcome, oldWorkflowInfo);
        }
        assertResultsDTOHasExpectedData(expectedOutcome, oldResultsDTO, resultsDTO);
    }

    private void assertTravelRequestIsAwaitingAction(
            String requestUuid, ConcurTestWorkflowInfo workflowInfo) {
        assertNotNull(workflowInfo, "A placeholder workflow object should have been present for request "
                + requestUuid);
        assertTrue(StringUtils.isBlank(workflowInfo.getActionTaken()),
                "No workflow action should have been recorded yet for request " + requestUuid);
        assertTrue(StringUtils.isBlank(workflowInfo.getComment()),
                "No workflow comment should have been recorded yet for request " + requestUuid);
    }

    private void assertWorkflowAttemptSucceedsOrFailsAsExpected(String requestUuid,
            WorkflowOutcome expectedOutcome, ConcurEventNotificationResponse resultsDTO) {
        switch (expectedOutcome) {
            case ERROR_ON_ACTION:
            case ERROR_ON_ACTION_FOR_MISSING_REQUEST:
            case ERROR_ON_DUPLICATE_ACTION:
                assertThrows(RuntimeException.class,
                        () -> concurRequestV4Service.updateRequestStatusInConcurIfNecessary(
                                accessToken, requestUuid, resultsDTO),
                        "An exception should have been thrown when attempting action for request " + requestUuid);
                break;
            
            default:
                concurRequestV4Service.updateRequestStatusInConcurIfNecessary(accessToken, requestUuid, resultsDTO);
                break;
        }
    }

    private void assertWorkflowActionWasRecorded(String requestUuid,
                                                 ConcurEventNotificationResponse oldResultsDTO, int expectedVersionNumber) {
        ConcurTestWorkflowInfo newWorkflowInfo = mockEndpoint.getWorkflowInfoForTravelRequest(requestUuid);
        boolean requestValid = oldResultsDTO.getEventNotificationStatus() ==
                ConcurEventNotificationStatus.validAccounts;
        String expectedWorkflowAction = ConcurWorkflowActions.APPROVE;
        
        assertNotNull(newWorkflowInfo, "Workflow action data should have been present for request " + requestUuid);
        assertTrue(StringUtils.isNotBlank(newWorkflowInfo.getActionTaken()),
                "A workflow action should have been recorded for request " + requestUuid);
        assertEquals(expectedWorkflowAction, newWorkflowInfo.getActionTaken(),
                "Wrong workflow action was taken for request " + requestUuid);
        assertTrue(StringUtils.isNotBlank(newWorkflowInfo.getComment()),
                "A workflow comment should have been recorded for request " + requestUuid);
        if (requestValid) {
            assertEquals(ConcurConstants.APPROVE_COMMENT, newWorkflowInfo.getComment(),
                    "Wrong workflow comment was recorded for request " + requestUuid);
        }
        assertEquals(expectedVersionNumber, newWorkflowInfo.getVersionNumber(),
                "Wrong version number on workflow object for request " + requestUuid);
    }

    private void assertWorkflowActionWasNotRecorded(
            String requestUuid, WorkflowOutcome expectedOutcome, ConcurTestWorkflowInfo oldWorkflowInfo) {
        ConcurTestWorkflowInfo newWorkflowInfo = mockEndpoint.getWorkflowInfoForTravelRequest(requestUuid);
        if (expectedOutcome == WorkflowOutcome.ERROR_ON_ACTION_FOR_MISSING_REQUEST) {
            assertNull(newWorkflowInfo, "No workflow data should have been recorded for request " + requestUuid);
            return;
        }
        
        assertNotNull(newWorkflowInfo, "Workflow action data should have been present for request " + requestUuid);
        assertEquals(oldWorkflowInfo.getActionTaken(), newWorkflowInfo.getActionTaken(),
                "Previous workflow action (possibly blank) was not preserved for request " + requestUuid);
        assertEquals(oldWorkflowInfo.getComment(), newWorkflowInfo.getComment(),
                "Previous workflow comment (possibly blank) was not preserved for request " + requestUuid);
        assertEquals(oldWorkflowInfo.getVersionNumber(), newWorkflowInfo.getVersionNumber(),
                "Previous workflow version number was not preserved for request " + requestUuid);
    }

    private void assertResultsDTOHasExpectedData(WorkflowOutcome expectedOutcome,
            ConcurEventNotificationResponse oldResultsDTO,
            ConcurEventNotificationResponse newResultsDTO) {
        assertEquals(ConcurEventNotificationType.TravelRequest, newResultsDTO.getEventType(),
                "Wrong event type");
        assertEquals(oldResultsDTO.getReportNumber(), newResultsDTO.getReportNumber(), "Wrong Request ID");
        assertEquals(oldResultsDTO.getTravelerName(), newResultsDTO.getTravelerName(), "Wrong traveler name");
        assertTrue(StringUtils.isBlank(newResultsDTO.getTravelerEmail()), "Traveler email should have been blank");
        
        ConcurEventNotificationStatus expectedProcessingResults =
                (expectedOutcome == WorkflowOutcome.ERROR_ANALYZING_RESPONSE)
                        ? ConcurEventNotificationStatus.processingError
                        : oldResultsDTO.getEventNotificationStatus();
        
        assertEquals(expectedProcessingResults, newResultsDTO.getEventNotificationStatus(), "Wrong processing results");
        if (expectedProcessingResults == ConcurEventNotificationStatus.validAccounts) {
            assertTrue(CollectionUtils.isEmpty(newResultsDTO.getMessages()),
                    "No error messages should have been present for a valid-accounts result");
        } else {
            assertTrue(CollectionUtils.isNotEmpty(newResultsDTO.getMessages()),
                    "One or more error messages should have been present for an invalid-accounts or error result");
        }
    }

    private ConfigurationService createMockConfigurationService() {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(
                ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_WORKFLOW))
                .thenReturn(PropertyTestValues.REQUESTV4_WORKFLOW_MESSAGE);
        return configurationService;
    }

    private ConcurEventNotificationResponse createProcessingResultsForRequest(
            RequestV4DetailFixture requestFixture) {
        if (requestFixture.isExpectedToPassAccountValidation()) {
            return createProcessingResultsForRequest(requestFixture,
                    ConcurEventNotificationStatus.validAccounts);
        } else {
            return createProcessingResultsForRequest(requestFixture,
                    ConcurEventNotificationStatus.invalidAccounts,
                    MESSAGE_INACTIVE_CHART, MESSAGE_MISSING_ACCOUNT,
                    ConcurRequestV4ServiceImpl.APPROVE_DESPITE_ERROR_MESSAGE);
        }
    }

    private ConcurEventNotificationResponse createProcessingErrorResultsForRequest(
            RequestV4DetailFixture requestFixture) {
        return createProcessingResultsForRequest(requestFixture,
                ConcurEventNotificationStatus.processingError,
                ConcurRequestV4ServiceImpl.PROCESSING_ERROR_MESSAGE,
                ConcurRequestV4ServiceImpl.APPROVE_DESPITE_ERROR_MESSAGE);
    }

    private ConcurEventNotificationResponse createProcessingResultsForRequest(
            RequestV4DetailFixture requestFixture, ConcurEventNotificationStatus requestResults,
            String... messages) {
        return new ConcurEventNotificationResponse(
                ConcurEventNotificationType.TravelRequest, requestResults, requestFixture.requestId,
                requestFixture.name, requestFixture.approvalStatus.name,
                requestFixture.owner.getFullName(), KFSConstants.EMPTY_STRING, Arrays.asList(messages));
    }

    private static class TestConcurRequestV4ServiceImpl extends ConcurRequestV4ServiceImpl {
        private boolean simulateProduction;
        
        @Override
        protected boolean isProduction() {
            return simulateProduction;
        }
        
        public void setSimulateProduction(boolean simulateProduction) {
            this.simulateProduction = simulateProduction;
        }
    }

}
