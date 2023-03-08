package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationWebserviceService;
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
import org.kuali.kfs.sys.KFSConstants.ParameterValues;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.ConcurTestWorkflowInfo;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.concur.util.MockConcurUtils;
import edu.cornell.kfs.concur.web.mock.MockConcurExpenseV4WorkflowController;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurExpenseV3ServiceUpdateReportStatusTest {

    private static final String TEST_BEARER_TOKEN = "ABCDEFG1234567abcdefg1234567ABCDEFG1234567";
    private static final String INVALID_BEARER_TOKEN = "0BCDEFG1234567abcdefg1234567ABCDEFG1234567";
    private static final String NON_EXISTING_REPORT_ID_1 = "aEiOuYYYYYYYYYY";
    private static final String NON_EXISTING_REPORT_ID_2 = "bcdFGHjklmnPQRS";
    private static final String NON_EXISTING_REPORT_ID_3 = "777777777777777";
    private static final String REPORT_NAME_E3_CONFERENCE = "John Doe E3 Conference";
    private static final String REPORT_STATUS_APPROVED = "Approved";
    private static final String TRAVELER_NAME_JOHN_DOE = "John Doe";
    private static final String TRAVELER_EMAIL_JOHN_DOE = "johndoe123@somedomain.com";
    private static final String MESSAGE_ACTION_TAKEN = "Action has been taken";
    private static final String MESSAGE_INACTIVE_CHART = "Chart code is inactive";
    private static final String MESSAGE_MISSING_ACCOUNT = "Account number is missing";
    private static final String MESSAGE_ERROR_ENCOUNTERED = "Encountered an error while validating";
    private static final String INVALID_WORKFLOW_ENDPOINT = "/expensereports/v4/unknown";

    @RegisterExtension
    static MockMvcWebServerExtension webServerExtension = new MockMvcWebServerExtension();

    private static MockConcurExpenseV4WorkflowController mockEndpoint;

    private String accessToken;
    private ConcurBatchUtilityService mockConcurBatchUtilityService;
    private TestConcurExpenseV3ServiceImpl concurExpenseV3Service;

    @BeforeAll
    static void setUpMockEndpoint() throws Exception {
        mockEndpoint = new MockConcurExpenseV4WorkflowController(TEST_BEARER_TOKEN);
        webServerExtension.initializeStandaloneMockMvcWithControllers(mockEndpoint);
    }

    @BeforeEach
    void setUp() throws Exception {
        this.accessToken = TEST_BEARER_TOKEN;
        this.mockConcurBatchUtilityService = createMockConcurBatchUtilityService(webServerExtension.getServerUrl());
        
        this.concurExpenseV3Service = new TestConcurExpenseV3ServiceImpl();
        concurExpenseV3Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        concurExpenseV3Service.setConcurEventNotificationApiService(
                createConcurEventNotificationV2WebserviceService(mockConcurBatchUtilityService));
        concurExpenseV3Service.setConfigurationService(createMockConfigurationService());
        concurExpenseV3Service.setConcurAccountValidationService(Mockito.mock(ConcurAccountValidationService.class));
        concurExpenseV3Service.setSimulateProduction(true);
        
        mockEndpoint.addReportsAwaitingAction(ConcurTestConstants.REPORT_ID_1,
                ConcurTestConstants.REPORT_ID_2, ConcurTestConstants.REPORT_ID_3);
    }

    @AfterEach
    void tearDown() throws Exception {
        concurExpenseV3Service = null;
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
                Map.entry(ConcurParameterConstants.EXPENSE_V4_WORKFLOW_ENDPOINT,
                        ParameterTestValues.EXPENSE_V4_WORKFLOW_ENDPOINT),
                Map.entry(ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND,
                        KFSConstants.ACTIVE_INDICATOR),
                Map.entry(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, String.valueOf(1)));
    }

    private ConcurEventNotificationWebserviceService createConcurEventNotificationV2WebserviceService(
            ConcurBatchUtilityService concurBatchUtilityService) {
        ConcurEventNotificationWebserviceServiceImpl concurEventNotificationV2WebserviceService
                = new ConcurEventNotificationWebserviceServiceImpl();
        concurEventNotificationV2WebserviceService.setConcurBatchUtilityService(concurBatchUtilityService);
        return concurEventNotificationV2WebserviceService;
    }

    private ConfigurationService createMockConfigurationService() {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(
                ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV4_EXPENSE_REPORT_WORKFLOW))
                .thenReturn(PropertyTestValues.EXPENSEV4_REPORT_WORKFLOW_MESSAGE);
        return configurationService;
    }

    static Stream<ConcurEventNotificationResponse> resultsForExistingReports() {
        return Stream.of(
                createResultsDTO(ConcurTestConstants.REPORT_ID_1,
                        ConcurEventNotificationStatus.validAccounts),
                createResultsDTO(ConcurTestConstants.REPORT_ID_2,
                        ConcurEventNotificationStatus.invalidAccounts,
                        MESSAGE_INACTIVE_CHART, MESSAGE_MISSING_ACCOUNT),
                createResultsDTO(ConcurTestConstants.REPORT_ID_3,
                        ConcurEventNotificationStatus.processingError,
                        MESSAGE_ERROR_ENCOUNTERED));
    }

    static Stream<ConcurEventNotificationResponse> resultsForNonExistingReports() {
        return Stream.of(
                createResultsDTO(NON_EXISTING_REPORT_ID_1,
                        ConcurEventNotificationStatus.validAccounts),
                createResultsDTO(NON_EXISTING_REPORT_ID_2,
                        ConcurEventNotificationStatus.invalidAccounts,
                        MESSAGE_INACTIVE_CHART, MESSAGE_MISSING_ACCOUNT),
                createResultsDTO(NON_EXISTING_REPORT_ID_3,
                        ConcurEventNotificationStatus.processingError,
                        MESSAGE_ERROR_ENCOUNTERED));
    }

    static Stream<ConcurEventNotificationResponse> resultsForExistingAndNonExistingReports() {
        return Stream.concat(resultsForExistingReports(), resultsForNonExistingReports());
    }

    private static ConcurEventNotificationResponse createResultsDTO(
            String reportId, ConcurEventNotificationStatus reportResults, String... messages) {
        return new ConcurEventNotificationResponse(
                ConcurEventNotificationType.ExpenseReport, reportResults, reportId, REPORT_NAME_E3_CONFERENCE, REPORT_STATUS_APPROVED,
                TRAVELER_NAME_JOHN_DOE, TRAVELER_EMAIL_JOHN_DOE, Arrays.asList(messages));
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingReports")
    void testUpdateReportStatusForExistingProductionReports(ConcurEventNotificationResponse resultsDTO)
            throws Exception {
        assertReportStatusUpdatesSuccessfully(resultsDTO);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingReports")
    void testUpdateReportStatusForExistingProductionReportsEvenWithParameterSetToN(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND, ParameterValues.NO);
        assertReportStatusUpdatesSuccessfully(resultsDTO);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingReports")
    void testUpdateReportStatusForExistingNonProductionReports(ConcurEventNotificationResponse resultsDTO)
            throws Exception {
        concurExpenseV3Service.setSimulateProduction(false);
        assertReportStatusUpdatesSuccessfully(resultsDTO);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingAndNonExistingReports")
    void testSkipUpdatesForNonProductionReportsIfParameterSetToN(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        boolean reportExists = mockEndpoint.doesReportExistOnMockServer(resultsDTO.getReportNumber());
        concurExpenseV3Service.setSimulateProduction(false);
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND, ParameterValues.NO);
        assertReportStatusDoesNotUpdate(resultsDTO, reportExists, false);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingReports")
    void testCannotUpdateReportStatusAgainAfterSuccessfulUpdate(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        assertReportStatusUpdatesSuccessfully(resultsDTO);
        assertReportStatusDoesNotUpdate(resultsDTO, true, true);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingReports")
    void testCannotUpdateReportStatusIfReportWasAlreadyApprovedOrSentBack(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        boolean reportValid =
                (resultsDTO.getEventNotificationStatus() == ConcurEventNotificationStatus.validAccounts);
        String workflowAction = reportValid ? ConcurWorkflowActions.APPROVE : ConcurWorkflowActions.SEND_BACK;
        mockEndpoint.overrideWorkflowInfoForReport(resultsDTO.getReportNumber(),
                new ConcurTestWorkflowInfo(workflowAction, MESSAGE_ACTION_TAKEN, 1));
        assertReportStatusDoesNotUpdate(resultsDTO, true, true);
    }

    @ParameterizedTest
    @MethodSource("resultsForNonExistingReports")
    void testCannotUpdateReportStatusForNonExistingReports(ConcurEventNotificationResponse resultsDTO)
            throws Exception {
        assertReportStatusDoesNotUpdate(resultsDTO, false, true);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingAndNonExistingReports")
    void testCannotUpdateReportStatusWhenCallingIncorrectEndpoint(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        boolean reportExists = mockEndpoint.doesReportExistOnMockServer(resultsDTO.getReportNumber());
        mockConcurBatchUtilityService.setConcurParameterValue(
                ConcurParameterConstants.EXPENSE_V4_WORKFLOW_ENDPOINT, INVALID_WORKFLOW_ENDPOINT);
        assertReportStatusDoesNotUpdate(resultsDTO, reportExists, true);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingAndNonExistingReports")
    void testCannotUpdateReportStatusIfEndpointEncountersInternalServerError(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        boolean reportExists = mockEndpoint.doesReportExistOnMockServer(resultsDTO.getReportNumber());
        mockEndpoint.setForceInternalServerError(true);
        assertReportStatusDoesNotUpdate(resultsDTO, reportExists, true);
    }

    @ParameterizedTest
    @MethodSource("resultsForExistingAndNonExistingReports")
    void testCannotUpdateReportStatusWhenUsingInvalidAccessToken(
            ConcurEventNotificationResponse resultsDTO) throws Exception {
        boolean reportExists = mockEndpoint.doesReportExistOnMockServer(resultsDTO.getReportNumber());
        accessToken = INVALID_BEARER_TOKEN;
        assertReportStatusDoesNotUpdate(resultsDTO, reportExists, true);
    }

    @Test
    void testUpdateStatusesOfMultipleReports() throws Exception {
        ConcurEventNotificationResponse[] resultsDTOs = resultsForExistingAndNonExistingReports()
                .toArray(ConcurEventNotificationResponse[]::new);
        for (ConcurEventNotificationResponse resultsDTO : resultsDTOs) {
            if (mockEndpoint.doesReportExistOnMockServer(resultsDTO.getReportNumber())) {
                assertReportStatusUpdatesSuccessfully(resultsDTO);
            } else {
                assertReportStatusDoesNotUpdate(resultsDTO, false, true);
            }
        }
    }

    private void assertReportStatusUpdatesSuccessfully(ConcurEventNotificationResponse resultsDTO) {
        String reportId = resultsDTO.getReportNumber();
        boolean reportValid =
                (resultsDTO.getEventNotificationStatus() == ConcurEventNotificationStatus.validAccounts);
        String expectedWorkflowAction = reportValid ? ConcurWorkflowActions.APPROVE : ConcurWorkflowActions.SEND_BACK;
        
        ConcurTestWorkflowInfo workflowInfo = mockEndpoint.getWorkflowInfoForReport(reportId);
        assertNotNull(workflowInfo, "A placeholder workflow object should have been present for report " + reportId);
        assertTrue(StringUtils.isBlank(workflowInfo.getActionTaken()),
                "No workflow action should have been recorded yet for report " + reportId);
        assertTrue(StringUtils.isBlank(workflowInfo.getComment()),
                "No workflow comment should have been recorded yet for report " + reportId);
        int oldVersionNumber = workflowInfo.getVersionNumber();
        
        concurExpenseV3Service.updateStatusInConcur(accessToken, reportId, reportValid, resultsDTO);
        
        workflowInfo = mockEndpoint.getWorkflowInfoForReport(reportId);
        assertNotNull(workflowInfo, "Workflow action data should have been present for report " + reportId);
        assertTrue(StringUtils.isNotBlank(workflowInfo.getActionTaken()),
                "A workflow action should have been recorded for report " + reportId);
        assertEquals(expectedWorkflowAction, workflowInfo.getActionTaken(),
                "Wrong workflow action was taken for report " + reportId);
        assertTrue(StringUtils.isNotBlank(workflowInfo.getComment()),
                "A workflow comment should have been recorded for report " + reportId);
        if (reportValid) {
            assertEquals(ConcurConstants.APPROVE_COMMENT, workflowInfo.getComment(),
                    "Wrong workflow comment was recorded for report " + reportId);
        }
        assertEquals(oldVersionNumber + 1, workflowInfo.getVersionNumber(),
                "Wrong version number on workflow object for report " + reportId);
    }

    private void assertReportStatusDoesNotUpdate(ConcurEventNotificationResponse resultsDTO,
                                                 boolean reportExists, boolean expectErrorOnWorkflowAttempt) {
        String reportId = resultsDTO.getReportNumber();
        boolean reportValid =
                (resultsDTO.getEventNotificationStatus() == ConcurEventNotificationStatus.validAccounts);
        ConcurTestWorkflowInfo workflowInfo = mockEndpoint.getWorkflowInfoForReport(reportId);
        String oldActionTaken = null;
        String oldActionComment = null;
        int oldVersionNumber = -1;
        if (reportExists) {
            assertNotNull(workflowInfo,
                    "A placeholder workflow object should have been present for report " + reportId);
            oldActionTaken = workflowInfo.getActionTaken();
            oldActionComment = workflowInfo.getComment();
            oldVersionNumber = workflowInfo.getVersionNumber();
        } else {
            assertNull(workflowInfo,
                    "A placeholder workflow object should not have been present for report " + reportId);
        }
        
        if (expectErrorOnWorkflowAttempt) {
            assertThrows(RuntimeException.class,
                    () -> concurExpenseV3Service.updateStatusInConcur(
                            accessToken, reportId, reportValid, resultsDTO),
                    "The attempted workflow action should have failed for report " + reportId);
        } else {
            concurExpenseV3Service.updateStatusInConcur(accessToken, reportId, reportValid, resultsDTO);
        }
        
        
        workflowInfo = mockEndpoint.getWorkflowInfoForReport(reportId);
        if (reportExists) {
            assertNotNull(workflowInfo,
                    "A placeholder workflow object should have been present for report " + reportId);
            assertEquals(oldActionTaken, workflowInfo.getActionTaken(),
                    "Previous workflow action (possibly blank) was not preserved for report " + reportId);
            assertEquals(oldActionComment, workflowInfo.getComment(),
                    "Previous workflow comment (possibly blank) was not preserved for report " + reportId);
            assertEquals(oldVersionNumber, workflowInfo.getVersionNumber(),
                    "Previous workflow version number was not preserved for report " + reportId);
        } else {
            assertNull(workflowInfo,
                    "A placeholder workflow object should not have been present for report " + reportId);
        }
    }

    private static class TestConcurExpenseV3ServiceImpl extends ConcurExpenseV3ServiceImpl {
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
