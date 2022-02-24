package edu.cornell.kfs.concur.batch.service.impl.fixture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;

public enum RequestV4ListingFixture {
    DEFAULT_ALL_SEARCH_2022_01_03_PAGE_1(0, 2, 2,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE);

    public final int startIndex;
    public final int limit;
    public final int totalCount;
    public final Map<String, RequestV4DetailFixture> pageResults;

    private RequestV4ListingFixture(int startIndex, int limit, int totalCount, RequestV4DetailFixture... pageResults) {
        this.startIndex = startIndex;
        this.limit = limit;
        this.totalCount = totalCount;
        this.pageResults = Arrays.stream(pageResults)
                .collect(Collectors.toUnmodifiableMap(resultItem -> resultItem.id, resultItem -> resultItem));
    }

    public Map<String, Map<String, String>> buildExpectedParametersForOperations(String queryUrl) {
        Map<String, String> initialParameters = ConcurFixtureUtils.getQueryParametersFromUrl(queryUrl);
        Map<String, Map<String, String>> operationParams = new HashMap<>();
        
        operationParams.put(ConcurApiOperations.FIRST, buildExpectedParametersForOperation(initialParameters, 0));
        operationParams.put(ConcurApiOperations.LAST,
                buildExpectedParametersForOperation(initialParameters, getStartIndexForLastPage()));
        if (hasPreviousPage()) {
            operationParams.put(ConcurApiOperations.PREV,
                    buildExpectedParametersForOperation(initialParameters, getStartIndexForPreviousPage()));
        }
        if (hasNextPage()) {
            operationParams.put(ConcurApiOperations.NEXT,
                    buildExpectedParametersForOperation(initialParameters, getStartIndexForNextPage()));
        }
        
        return operationParams;
    }

    private Map<String, String> buildExpectedParametersForOperation(
            Map<String, String> initialParameters, int newStartIndex) {
        Map<String, String> parameters = new HashMap<>(initialParameters);
        parameters.put(ConcurApiParameters.START, String.valueOf(newStartIndex));
        return parameters;
    }

    public boolean hasPreviousPage() {
        return getStartIndexForPreviousPage() >= 0;
    }

    public int getStartIndexForPreviousPage() {
        return Math.max(startIndex - limit, -1);
    }

    public boolean hasNextPage() {
        return getStartIndexForNextPage() >= 0;
    }

    public int getStartIndexForNextPage() {
        int nextStartIndex = startIndex + limit;
        return (nextStartIndex < totalCount) ? nextStartIndex : -1;
    }

    public int getStartIndexForLastPage() {
        return ConcurFixtureUtils.calculateSearchStartIndexForLastPageOfResults(totalCount, limit);
    }

}
