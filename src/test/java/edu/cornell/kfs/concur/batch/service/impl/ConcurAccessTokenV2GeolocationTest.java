package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.util.MockConcurUtils;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenV2GeolocationTest {

    private ConcurAccessTokenV2ServiceImpl concurAccessTokenV2Service;
    private ConcurBatchUtilityService mockConcurBatchUtilityService;

    @BeforeEach
    void setUp() throws Exception {
        this.mockConcurBatchUtilityService = MockConcurUtils.createMockConcurBatchUtilityServiceBackedByParameters(
                Map.entry(ConcurParameterConstants.CONCUR_GEOLOCATION_URL, ConcurTestConstants.CONCUR_GEOLOCATION_CONCURSOLUTIONS));
        this.concurAccessTokenV2Service = new ConcurAccessTokenV2ServiceImpl();
        
        concurAccessTokenV2Service.setWebServiceCredentialService(Mockito.mock(WebServiceCredentialService.class));
        concurAccessTokenV2Service.setConcurBatchUtilityService(mockConcurBatchUtilityService);
    }

    @AfterEach
    void tearDown() throws Exception {
        concurAccessTokenV2Service = null;
        mockConcurBatchUtilityService = null;
    }

}
