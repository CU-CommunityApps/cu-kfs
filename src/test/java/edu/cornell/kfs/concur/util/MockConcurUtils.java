package edu.cornell.kfs.concur.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public final class MockConcurUtils {

    @SafeVarargs
    public static ConcurBatchUtilityService createMockConcurBatchUtilityServiceBackedByParameters(
            Map.Entry<String, String>... parameters) {
        Map<String, String> parameterMap = createMutableMap(parameters);
        return createMockConcurBatchUtilityServiceBackedByParameterMap(parameterMap);
    }

    public static ConcurBatchUtilityService createMockConcurBatchUtilityServiceBackedByParameterMap(
            Map<String, String> parameterMap) {
        ConcurBatchUtilityService mockService = Mockito.mock(ConcurBatchUtilityService.class);
        Mockito.when(mockService.getConcurParameterValue(Mockito.any()))
                .then(invocation -> parameterMap.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> parameterMap.put(invocation.getArgument(0), invocation.getArgument(1)))
                .when(mockService).setConcurParameterValue(Mockito.any(), Mockito.any());
        return mockService;
    }

    @SafeVarargs
    public static WebServiceCredentialService createMockWebServiceCredentialServiceBackedByCredentials(
            String groupCode, Map.Entry<String, String>... credentials) {
        Map<String, String> credentialMap = createMutableMap(credentials);
        return createMockWebServiceCredentialServiceBackedByCredentialMap(groupCode, credentialMap);
    }

    public static WebServiceCredentialService createMockWebServiceCredentialServiceBackedByCredentialMap(
            String groupCode, Map<String, String> credentialMap) {
        WebServiceCredentialService mockService = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(mockService.getWebServiceCredentialValue(Mockito.eq(groupCode), Mockito.any()))
                .then(invocation -> credentialMap.get(invocation.getArgument(1)));
        Mockito.doAnswer(invocation -> credentialMap.put(invocation.getArgument(1), invocation.getArgument(2)))
                .when(mockService).updateWebServiceCredentialValue(
                        Mockito.eq(groupCode), Mockito.any(), Mockito.any());
        return mockService;
    }

    @SafeVarargs
    private static Map<String, String> createMutableMap(Map.Entry<String, String>... entries) {
        return Stream.of(entries)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (val1, val2) -> val2, HashMap::new));
    }

}
