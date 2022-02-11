package edu.cornell.kfs.concur.batch.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.ConcurTestConstants.PropertyTestValues;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.impl.fixture.ConcurV4PersonFixture;
import edu.cornell.kfs.sys.CUKFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class ConcurRequestV4ServiceImplTest {

    private TestConcurRequestV4ServiceImpl requestV4Service;
    private Map<String, String> concurParameters;
    private Map<String, String> concurProperties;

    @BeforeEach
    public void setUp() throws Exception {
        concurParameters = buildTestParameters();
        concurProperties = buildTestProperties();
        requestV4Service = new TestConcurRequestV4ServiceImpl();
        requestV4Service.setSimulateProductionMode(false);
        requestV4Service.setConcurBatchUtilityService(buildMockConcurBatchUtilityService());
        requestV4Service.setConfigurationService(buildMockConfigurationService());
    }

    @AfterEach
    public void tearDown() throws Exception {
        concurParameters = null;
        concurProperties = null;
        requestV4Service = null;
    }

    private Map<String, String> buildTestParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE,
                ParameterTestValues.DEFAULT_OBJECT_CODE_5500);
        parameters.put(ConcurParameterConstants.REQUEST_V4_LISTING_ENDPOINT,
                ParameterTestValues.REQUEST_V4_LOCALHOST_ENDPOINT);
        parameters.put(ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE,
                ParameterTestValues.REQUEST_V4_PAGE_SIZE_2);
        parameters.put(ConcurParameterConstants.REQUEST_V4_TEST_APPROVERS, buildTestApproversParameterValue());
        return parameters;
    }

    private String buildTestApproversParameterValue() {
        return ConcurV4PersonFixture.TEST_MANAGER.id + CUKFSConstants.SEMICOLON
                + ConcurV4PersonFixture.TEST_APPROVER.id;
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
        properties.put(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING_NEXT_PAGE,
                PropertyTestValues.REQUESTV4_REQUEST_LIST_SEARCH_NEXT_PAGE_MESSAGE);
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
