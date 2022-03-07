package edu.cornell.kfs.concur.batch.service.impl.fixture;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurTestConstants.RequestV4Dates;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;

public enum RequestV4ListingFixture {
    SEARCH_2022_01_02_TO_2022_01_03(2, RequestV4Dates.DATE_2022_01_02, RequestV4Dates.DATE_2022_01_03,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE),

    SEARCH_2022_01_02_TO_2022_01_03_PAGE_SIZE_5(SEARCH_2022_01_02_TO_2022_01_03, 5),

    SEARCH_2022_01_01_TO_2022_01_02(2, RequestV4Dates.DATE_2022_01_01, RequestV4Dates.DATE_2022_01_02),

    SEARCH_2022_01_01_TO_2022_01_02_PAGE_SIZE_5(SEARCH_2022_01_01_TO_2022_01_02, 5),

    SEARCH_2022_04_05_TO_2022_04_06(2, RequestV4Dates.DATE_2022_04_05, RequestV4Dates.DATE_2022_04_06,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH),

    SEARCH_2022_04_05_TO_2022_04_07(2, RequestV4Dates.DATE_2022_04_05, RequestV4Dates.DATE_2022_04_07,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH),

    SEARCH_2022_01_01_TO_2022_04_30(2, RequestV4Dates.DATE_2022_01_01, RequestV4Dates.DATE_2022_04_30,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JOHN_TEST,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_JOHN_DOE,
            RequestV4DetailFixture.APPROVED_TEST_REQUEST_JANE_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_TEST_REQUEST_JANE_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_TEST_REQUEST_JANE_DOE,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_INVALID_REGULAR_REQUEST_BOB_SMITH,
            RequestV4DetailFixture.PENDING_EXTERNAL_VALIDATION_REGULAR_REQUEST_BOB_SMITH),

    SEARCH_2022_01_01_TO_2022_04_30_PAGE_SIZE_5(SEARCH_2022_01_01_TO_2022_04_30, 5),

    SEARCH_2022_01_01_TO_2022_04_30_PAGE_SIZE_20(SEARCH_2022_01_01_TO_2022_04_30, 20);

    public final int pageSize;
    public final int totalResultRowCount;
    public final int totalPageCount;
    public final String modifiedFromDate;
    public final String modifiedToDate;
    public final Map<String, RequestV4DetailFixture> searchResults;

    private RequestV4ListingFixture(RequestV4ListingFixture existingFixture, int newPageSize) {
        this(newPageSize, existingFixture.modifiedFromDate, existingFixture.modifiedToDate,
                existingFixture.searchResults.values().toArray(RequestV4DetailFixture[]::new));
    }

    private RequestV4ListingFixture(int pageSize, String modifiedFromDate, String modifiedToDate,
            RequestV4DetailFixture... searchResults) {
        this.pageSize = pageSize;
        this.totalResultRowCount = searchResults.length;
        this.totalPageCount = (totalResultRowCount > 0)
                ? (totalResultRowCount / pageSize) + Math.min(totalResultRowCount % pageSize, 1)
                : 1;
        this.modifiedFromDate = modifiedFromDate;
        this.modifiedToDate = modifiedToDate;
        this.searchResults = Arrays.stream(searchResults)
                .collect(Collectors.toUnmodifiableMap(resultItem -> resultItem.id, resultItem -> resultItem));
    }

    public Map<String, RequestV4DetailFixture> getExpectedProcessedRequestsKeyedByRequestId(boolean productionMode) {
        return searchResults.values().stream()
                .filter(result -> result.approvalStatus == RequestV4Status.PENDING_EXTERNAL_VALIDATION)
                .filter(result -> productionMode != result.isOwnedByTestUser())
                .collect(Collectors.toUnmodifiableMap(result -> result.requestId, result -> result));
    }

    public int calculateExpectedResultCountForPage(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= totalPageCount) {
            throw new IndexOutOfBoundsException("pageIndex is out of range");
        }
        return (pageIndex < totalPageCount - 1) ? pageSize : totalResultRowCount - (pageIndex * pageSize);
    }

    public Map<String, Map<String, String>> buildExpectedParametersForOperationsOnPage(
            int pageIndex, String queryUrl) {
        if (pageIndex < 0 || pageIndex >= totalPageCount) {
            throw new IndexOutOfBoundsException("pageIndex is out of range");
        }
        Map<String, String> initialParameters = ConcurFixtureUtils.getQueryParametersFromUrl(queryUrl);
        Map<String, Map<String, String>> operationParams = new HashMap<>();
        int currentStartIndex = pageIndex * pageSize;
        int startIndexForPreviousPage = calculateStartIndexForPreviousPage(currentStartIndex);
        int startIndexForNextPage = calculateStartIndexForNextPage(currentStartIndex);
        
        operationParams.put(ConcurApiOperations.FIRST, buildExpectedParametersForOperation(initialParameters, 0));
        operationParams.put(ConcurApiOperations.LAST,
                buildExpectedParametersForOperation(initialParameters, calculateStartIndexForLastPage()));
        if (startIndexForPreviousPage >= 0) {
            operationParams.put(ConcurApiOperations.PREV,
                    buildExpectedParametersForOperation(initialParameters, startIndexForPreviousPage));
        }
        if (startIndexForNextPage >= 0) {
            operationParams.put(ConcurApiOperations.NEXT,
                    buildExpectedParametersForOperation(initialParameters, startIndexForNextPage));
        }
        
        return operationParams;
    }

    private Map<String, String> buildExpectedParametersForOperation(
            Map<String, String> initialParameters, int newStartIndex) {
        Map<String, String> parameters = new HashMap<>(initialParameters);
        parameters.put(ConcurApiParameters.START, String.valueOf(newStartIndex));
        return parameters;
    }

    private int calculateStartIndexForPreviousPage(int currentStartIndex) {
        return Math.max(currentStartIndex - pageSize, -1);
    }

    private int calculateStartIndexForNextPage(int currentStartIndex) {
        int nextStartIndex = currentStartIndex + pageSize;
        return (nextStartIndex < totalResultRowCount) ? nextStartIndex : -1;
    }

    private int calculateStartIndexForLastPage() {
        return ConcurFixtureUtils.calculateSearchStartIndexForLastPageOfResults(totalResultRowCount, pageSize);
    }

}
