package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsRootDTO;
import edu.cornell.kfs.pmw.web.mock.MockPaymentWorksGetNewVendorsController;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.MockMvcWebServerExtension;

@Execution(ExecutionMode.SAME_THREAD)
public class PaymentWorksGetNewVendorRequestsTest {

    private static final String MOCK_USERID = "123ab";
    private static final String MOCK_AUTHORIZATION_TOKEN = "AABBCCDDEEFFGGHHIIJJ00112233445566778899";
    private static final String MOCK_PMW_REQUEST_ID_1 = "5676567";
    private static final String MOCK_PMW_REQUEST_ID_2 = "8888888";

    @RegisterExtension
    MockMvcWebServerExtension mockServerExtension = new MockMvcWebServerExtension();

    private String serverUrl;
    private MockPaymentWorksGetNewVendorsController mockPaymentWorksEndpoint;
    private PaymentWorksWebServiceCallsServiceImpl paymentWorksWebServiceCallsService;

    @BeforeEach
    void setUp() throws Exception {
        this.serverUrl = mockServerExtension.getServerUrl();
        this.mockPaymentWorksEndpoint = new MockPaymentWorksGetNewVendorsController()
                .withExpectedAuthorizationToken(MOCK_AUTHORIZATION_TOKEN);
        
        String mockPaymentWorksUrl = serverUrl + CUKFSConstants.SLASH;
        this.paymentWorksWebServiceCallsService = buildPaymentWorksWebServiceCallsService(mockPaymentWorksUrl);
        
        mockServerExtension.initializeStandaloneMockMvcWithControllers(mockPaymentWorksEndpoint);
    }

    private PaymentWorksWebServiceCallsServiceImpl buildPaymentWorksWebServiceCallsService(
            String mockPaymentWorksUrl) {
        PaymentWorksWebServiceCallsServiceImpl pmwService = new PaymentWorksWebServiceCallsServiceImpl();
        pmwService.setPaymentWorksDtoToPaymentWorksVendorConversionService(
                Mockito.mock(PaymentWorksDtoToPaymentWorksVendorConversionService.class));
        pmwService.setWebServiceCredentialService(
                buildMockWebServiceCredentialService(mockPaymentWorksUrl));
        return pmwService;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService(String mockPaymentWorksUrl) {
        WebServiceCredentialService credentialService = Mockito.mock(WebServiceCredentialService.class);
        String[][] mockCredentials = {
            { PaymentWorksCredentialKeys.PAYMENTWORKS_USER_ID, MOCK_USERID },
            { PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN, MOCK_AUTHORIZATION_TOKEN },
            { PaymentWorksCredentialKeys.PAYMENTWORKS_API_URL, mockPaymentWorksUrl }
        };
        for (String[] keyValuePair : mockCredentials) {
            Mockito.when(credentialService.getWebServiceCredentialValue(
                    PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, keyValuePair[0]))
                    .thenReturn(keyValuePair[1]);
        }
        return credentialService;
    }

    @AfterEach
    void tearDown() throws Exception {
        paymentWorksWebServiceCallsService = null;
        mockPaymentWorksEndpoint = null;
        serverUrl = null;
    }

    static Stream<List<String>> idsOfNewVendorRequests() {
        return Stream.of(
                List.of(),
                List.of(MOCK_PMW_REQUEST_ID_1),
                List.of(MOCK_PMW_REQUEST_ID_1, MOCK_PMW_REQUEST_ID_2)
        );
    }

    @ParameterizedTest
    @MethodSource("idsOfNewVendorRequests")
    void testGetVendorRequestIdsForSinglePageOfResults(List<String> expectedIds) throws Exception {
        PaymentWorksNewVendorRequestsRootDTO vendorRequestsDTO = buildRequestsDTOForSinglePageOfResults(expectedIds);
        mockPaymentWorksEndpoint.setVendorRequestsDTO(vendorRequestsDTO);
        
        List<String> actualIds = paymentWorksWebServiceCallsService.obtainPmwIdentifiersForApprovedNewVendorRequests();
        assertNotNull(actualIds, "The retrieved ID list should have been non-null");
        assertEquals(expectedIds.size(), actualIds.size(), "Wrong number of IDs found");
        
        Set<String> expectedIdsSet = Set.copyOf(expectedIds);
        Set<String> encounteredIds = new HashSet<>();
        for (String actualId : actualIds) {
            assertTrue(expectedIdsSet.contains(actualId), "An unexpected request ID was found: " + actualId);
            assertTrue(encounteredIds.add(actualId), "An unexpected duplicate request ID was found: " + actualId);
        }
    }

    private PaymentWorksNewVendorRequestsRootDTO buildRequestsDTOForSinglePageOfResults(List<String> ids) {
        List<PaymentWorksNewVendorRequestDTO> newRequests = ids.stream()
                .map(this::buildNewVendorRequestDTO)
                .collect(Collectors.toUnmodifiableList());
        
        PaymentWorksNewVendorRequestsDTO requestsListDTO = new PaymentWorksNewVendorRequestsDTO();
        requestsListDTO.setPmwNewVendorRequests(newRequests);
        
        PaymentWorksNewVendorRequestsRootDTO requestsDTO = new PaymentWorksNewVendorRequestsRootDTO();
        requestsDTO.setCount(newRequests.size());
        requestsDTO.setPmwNewVendorRequestsDTO(requestsListDTO);
        return requestsDTO;
    }

    private PaymentWorksNewVendorRequestDTO buildNewVendorRequestDTO(String id) {
        PaymentWorksNewVendorRequestDTO requestDTO = new PaymentWorksNewVendorRequestDTO();
        requestDTO.setId(id);
        requestDTO.setRequest_status(
                PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.getCodeAsString());
        return requestDTO;
    }

}
