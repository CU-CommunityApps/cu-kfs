package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4PersonFixture;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.web.mock.MockRemoteServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurRequestV4ServiceImplTest {

    private static final String MOCK_CURRENT_DATE = "02/15/2022 11:22:33";

    private String mockAccessToken;
    private MockConcurRequestV4Server mockConcurBackendServer;
    private MockConcurRequestV4ServiceEndpoint mockConcurEndpoint;
    private MockRemoteServerExtension mockHttpServer;
    private Map<String, String> concurParameters;
    private Map<String, String> concurProperties;
    private TestConcurRequestV4ServiceImpl requestV4Service;

    @BeforeEach
    void setUp() throws Exception {
        mockAccessToken = buildMockAccessTokenFromUUID();
        mockConcurEndpoint = new MockConcurRequestV4ServiceEndpoint(mockAccessToken,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
                RequestV4DetailFixture.PENDING_APPROVAL_TEST_REQUEST_JOHN_DOE);
        mockHttpServer = new MockRemoteServerExtension();
        mockHttpServer.initialize(mockConcurEndpoint);
        mockConcurBackendServer = mockConcurEndpoint.getMockBackendServer();
        
        concurParameters = buildTestParameters(mockHttpServer.getServerUrl().get());
        concurProperties = buildTestProperties();
        
        ConcurBatchUtilityService mockConcurBatchUtilityService = buildMockConcurBatchUtilityService();
        
        requestV4Service = new TestConcurRequestV4ServiceImpl();
        requestV4Service.setSimulateProductionMode(false);
        requestV4Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        requestV4Service.setConcurEventNotificationV2WebserviceService(
                buildConcurEventNotificationV2WebserviceService(mockConcurBatchUtilityService));
        requestV4Service.setConfigurationService(buildMockConfigurationService());
        requestV4Service.setDateTimeService(buildDateTimeService());
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

    private Map<String, String> buildTestParameters(String baseUri) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES, ParameterTestValues.MAX_RETRIES_1);
        parameters.put(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE,
                ParameterTestValues.DEFAULT_OBJECT_CODE_5500);
        parameters.put(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT,
                baseUri + ParameterTestValues.REQUEST_V4_RELATIVE_ENDPOINT);
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

    private DateTimeService buildDateTimeService() throws Exception {
        TestDateTimeServiceImpl actualDateTimeService = new TestDateTimeServiceImpl();
        actualDateTimeService.afterPropertiesSet();
        Date mockCurrentDate = actualDateTimeService.convertToDate(MOCK_CURRENT_DATE);
        long mockCurrentTimeMillis = mockCurrentDate.getTime();
        
        TestDateTimeServiceImpl dateTimeService = Mockito.spy(actualDateTimeService);
        Mockito.doAnswer(invocation -> new Date(mockCurrentTimeMillis))
                .when(dateTimeService).getCurrentDate();
        return dateTimeService;
    }

    private ConcurEventNotificationV2WebserviceService buildConcurEventNotificationV2WebserviceService(
            ConcurBatchUtilityService mockConcurBatchUtilityService) {
        ConcurEventNotificationV2WebserviceServiceImpl notificationV2Service
                = new ConcurEventNotificationV2WebserviceServiceImpl();
        notificationV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
        return notificationV2Service;
    }

    @Test
    void testFindSingleTravelRequest() throws Exception {
        String requestUuid = RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE.id;
        
        ConcurRequestV4ReportDTO requestDetail = requestV4Service.findTravelRequest(mockAccessToken, requestUuid);
        assertEquals(requestUuid, requestDetail.getId(), "Wrong Request UUID");
    }

    @Test
    void testFindTravelRequestListing() throws Exception {
        String queryUrl = requestV4Service.buildInitialRequestQueryUrl(Optional.empty());
        requestV4Service.findPendingTravelRequests(mockAccessToken, 1, queryUrl);
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
