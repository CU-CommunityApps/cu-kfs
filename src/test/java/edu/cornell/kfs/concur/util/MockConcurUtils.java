package edu.cornell.kfs.concur.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;

public final class MockConcurUtils {

    @SafeVarargs
    public static ConcurBatchUtilityService createMockConcurBatchUtilityServiceBackedByParameters(
            Map.Entry<String, String>... parameters) {
        Map<String, String> parameterMap = Stream.of(parameters)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (val1, val2) -> val2, HashMap::new));
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

}
