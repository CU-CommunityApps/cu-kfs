package edu.cornell.kfs.concur.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.MapUtils;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public final class MockConcurUtils {

    @SafeVarargs
    public static ConcurBatchUtilityService createMockConcurBatchUtilityServiceBackedByParameters(
            Map.Entry<String, Object>... parameters) {
        Map<String, String> stringParameterMap = createMutableStringMapFromObjectValues(parameters);
        Map<String, Boolean> booleanParameterMap = createMutableBooleanMapFromObjectValues(parameters);
        return createMockConcurBatchUtilityServiceBackedByParameterMap(stringParameterMap, booleanParameterMap);
    }

    public static ConcurBatchUtilityService createMockConcurBatchUtilityServiceBackedByParameterMap(
            Map<String, String> stringParameterMap, Map<String, Boolean> booleanParameterMap) {
        ConcurBatchUtilityService mockService = Mockito.mock(ConcurBatchUtilityService.class);

        if (MapUtils.isNotEmpty(stringParameterMap)) {
            Mockito.when(mockService.getConcurParameterValue(Mockito.any()))
                    .then(invocation -> stringParameterMap.get(invocation.getArgument(0)));
            Mockito.doAnswer(invocation -> stringParameterMap.put(invocation.getArgument(0), invocation.getArgument(1)))
                    .when(mockService).setConcurParameterValue(Mockito.any(), Mockito.any());
        }

        if (MapUtils.isNotEmpty(booleanParameterMap)) {
            Mockito.when(mockService.getConcurParameterBooleanValue(Mockito.any()))
                    .then(invocation -> booleanParameterMap.get(invocation.getArgument(0)));
        }
        return mockService;
    }

    @SafeVarargs
    public static WebServiceCredentialService createMockWebServiceCredentialServiceBackedByCredentials(
            String groupCode, Map.Entry<String, String>... credentials) {
        Map<String, String> credentialMap = createMutableStringMapFromStringValues(credentials);
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
    private static Map<String, String> createMutableStringMapFromStringValues(Map.Entry<String, String>... entries) {
        return Stream.of(entries)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (val1, val2) -> val2, HashMap::new));
    }

    @SafeVarargs
    public static Map<String, String> createMutableStringMapFromObjectValues(Map.Entry<String, Object>... entries) {
        return Arrays.stream(entries)
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> (String) entry.getValue(), 
                        (val1, val2) -> val2, 
                        HashMap::new)
                        );
    }
    
    @SafeVarargs
    public static Map<String, Boolean> createMutableBooleanMapFromObjectValues(Map.Entry<String, Object>... entries) {
        return Arrays.stream(entries)
                .filter(entry -> entry.getValue() instanceof Boolean)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> (Boolean) entry.getValue(), 
                        (val1, val2) -> val2, 
                        HashMap::new)
                        );
    }


}
