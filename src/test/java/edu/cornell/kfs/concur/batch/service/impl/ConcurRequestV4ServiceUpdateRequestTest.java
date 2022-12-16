package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
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
        concurRequestV4Service.setConcurEventNotificationV2WebserviceService(
                createConcurEventNotificationV2WebserviceService(mockConcurBatchUtilityService));
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

    private ConcurEventNotificationV2WebserviceService createConcurEventNotificationV2WebserviceService(
            ConcurBatchUtilityService concurBatchUtilityService) {
        ConcurEventNotificationV2WebserviceServiceImpl concurEventNotificationV2WebserviceService
                = new ConcurEventNotificationV2WebserviceServiceImpl();
        concurEventNotificationV2WebserviceService.setConcurBatchUtilityService(concurBatchUtilityService);
        return concurEventNotificationV2WebserviceService;
    }

    static Stream<RequestV4DetailFixture> regularTravelRequests() {
        return Stream.of(
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH);
    }

    @ParameterizedTest
    @MethodSource("regularTravelRequests")
    void testUpdateStatusesForRegularRequests(RequestV4DetailFixture requestFixture) throws Exception {
        assertRequestWorkflowUpdateSucceeds(requestFixture);
    }

    private void assertRequestWorkflowUpdateSucceeds(RequestV4DetailFixture requestFixture) {
        ConcurEventNotificationProcessingResultsDTO resultsDTO = createProcessingResultsForRequest(requestFixture);
        assertRequestWorkflowUpdateSucceeds(requestFixture, requestFixture.getExpectedProcessingResult(), resultsDTO);
    }

    private void assertRequestWorkflowUpdateSucceeds(RequestV4DetailFixture requestFixture,
            ConcurEventNotificationVersion2ProcessingResults expectedOutcome,
            ConcurEventNotificationProcessingResultsDTO resultsDTO) {
        boolean requestValid =
                (resultsDTO.getProcessingResults() == ConcurEventNotificationVersion2ProcessingResults.validAccounts);
        String expectedWorkflowAction = requestValid ? ConcurWorkflowActions.APPROVE : ConcurWorkflowActions.SEND_BACK;
        
        ConcurTestWorkflowInfo workflowInfo = mockEndpoint.getWorkflowInfoForTravelRequest(requestFixture.id);
        assertNotNull(workflowInfo, "A placeholder workflow object should have been present for request "
                + requestFixture.id);
        assertTrue(StringUtils.isBlank(workflowInfo.getActionTaken()),
                "No workflow action should have been recorded yet for request " + requestFixture.id);
        assertTrue(StringUtils.isBlank(workflowInfo.getComment()),
                "No workflow comment should have been recorded yet for request " + requestFixture.id);
        int oldVersionNumber = workflowInfo.getVersionNumber();
        
        concurRequestV4Service.updateRequestStatusInConcur(accessToken, requestFixture.id, resultsDTO);
        
        workflowInfo = mockEndpoint.getWorkflowInfoForTravelRequest(requestFixture.id);
        assertNotNull(workflowInfo, "Workflow action data should have been present for request " + requestFixture.id);
        assertTrue(StringUtils.isNotBlank(workflowInfo.getActionTaken()),
                "A workflow action should have been recorded for request " + requestFixture.id);
        assertEquals(expectedWorkflowAction, workflowInfo.getActionTaken(),
                "Wrong workflow action was taken for request " + requestFixture.id);
        assertTrue(StringUtils.isNotBlank(workflowInfo.getComment()),
                "A workflow comment should have been recorded for request " + requestFixture.id);
        if (requestValid) {
            assertEquals(ConcurConstants.APPROVE_COMMENT, workflowInfo.getComment(),
                    "Wrong workflow comment was recorded for request " + requestFixture.id);
        }
        assertEquals(oldVersionNumber + 1, workflowInfo.getVersionNumber(),
                "Wrong version number on workflow object for request " + requestFixture.id);
        
        assertResultsDTOHasExpectedData(requestFixture, expectedOutcome, resultsDTO);
    }

    private void assertResultsDTOHasExpectedData(RequestV4DetailFixture requestFixture,
            ConcurEventNotificationVersion2ProcessingResults expectedOutcome,
            ConcurEventNotificationProcessingResultsDTO resultsDTO) {
        assertEquals(ConcurEventNoticationVersion2EventType.TravelRequest, resultsDTO.getEventType(),
                "Wrong event type");
        assertEquals(expectedOutcome, resultsDTO.getProcessingResults(), "Wrong processing result outcome");
        assertEquals(requestFixture.requestId, resultsDTO.getReportNumber(), "Wrong Request ID");
        assertEquals(requestFixture.owner.getFullName(), resultsDTO.getTravelerName(), "Wrong traveler name");
        assertTrue(StringUtils.isBlank(resultsDTO.getTravelerEmail()), "Traveler email should have been blank");
        if (expectedOutcome == ConcurEventNotificationVersion2ProcessingResults.validAccounts) {
            assertTrue(CollectionUtils.isEmpty(resultsDTO.getMessages()),
                    "No error messages should have been present for a valid-accounts result");
        } else {
            assertTrue(CollectionUtils.isNotEmpty(resultsDTO.getMessages()),
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

    private ConcurEventNotificationProcessingResultsDTO createProcessingResultsForRequest(
            RequestV4DetailFixture requestFixture) {
        if (requestFixture.isExpectedToPassAccountValidation()) {
            return createProcessingResultsForRequest(requestFixture,
                    ConcurEventNotificationVersion2ProcessingResults.validAccounts);
        } else {
            return createProcessingResultsForRequest(requestFixture,
                    ConcurEventNotificationVersion2ProcessingResults.invalidAccounts,
                    MESSAGE_INACTIVE_CHART, MESSAGE_MISSING_ACCOUNT);
        }
    }

    private ConcurEventNotificationProcessingResultsDTO createProcessingErrorResultsForRequest(
            RequestV4DetailFixture requestFixture) {
        return createProcessingResultsForRequest(requestFixture,
                ConcurEventNotificationVersion2ProcessingResults.processingError,
                ConcurRequestV4ServiceImpl.PROCESSING_ERROR_MESSAGE);
    }

    private ConcurEventNotificationProcessingResultsDTO createProcessingResultsForRequest(
            RequestV4DetailFixture requestFixture, ConcurEventNotificationVersion2ProcessingResults requestResults,
            String... messages) {
        return new ConcurEventNotificationProcessingResultsDTO(
                ConcurEventNoticationVersion2EventType.TravelRequest, requestResults, requestFixture.requestId,
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
