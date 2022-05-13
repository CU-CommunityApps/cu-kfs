package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.UrlFactory;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Views;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4OperationDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;

public class MockConcurRequestV4Backend {

    private static final int MAX_RESULT_LIMIT = 100;

    private static final Set<String> ALLOWED_REQUEST_LIST_QUERY_PARAMETERS = Set.of(
            ConcurApiParameters.VIEW, ConcurApiParameters.START, ConcurApiParameters.LIMIT,
            ConcurApiParameters.MODIFIED_AFTER, ConcurApiParameters.MODIFIED_BEFORE,
            ConcurApiParameters.SORT_FIELD, ConcurApiParameters.SORT_ORDER);

    private ConcurrentMap<String, RequestEntry> travelRequests;
    private String baseRequestV4Url;

    public MockConcurRequestV4Backend(String baseRequestV4Url, RequestV4DetailFixture... requestsToAdd) {
        this.travelRequests = new ConcurrentHashMap<>();
        this.baseRequestV4Url = baseRequestV4Url;
        addTravelRequests(requestsToAdd);
    }

    public void addTravelRequests(RequestV4DetailFixture... requestsToAdd) {
        for (RequestV4DetailFixture travelRequest : requestsToAdd) {
            travelRequests.put(travelRequest.id, new RequestEntry(travelRequest, baseRequestV4Url));
        }
    }

    public Optional<ConcurRequestV4ReportDTO> findRequest(String id) {
        RequestEntry matchingEntry = travelRequests.get(id);
        return Optional.ofNullable(matchingEntry)
                .map(entry -> entry.requestDetail);
    }

    public ConcurRequestV4ListingDTO findRequests(Map<String, String> queryParameters) {
        Set<String> unsupportedParameters = searchForUnsupportedRequestListQueryParameters(queryParameters);
        if (!unsupportedParameters.isEmpty()) {
            throw new IllegalArgumentException("Query contains unsupported parameters "
                    + "(and this mock backend might not support all the parameters from the real Concur web service): "
                    + unsupportedParameters.toString());
        }
        
        String view = getExistingParameter(queryParameters, ConcurApiParameters.VIEW);
        int startIndex = getExistingIntParameter(queryParameters, ConcurApiParameters.START);
        int limit = getExistingIntParameter(queryParameters, ConcurApiParameters.LIMIT);
        Date modifiedAfter = getExistingDateParameter(queryParameters, ConcurApiParameters.MODIFIED_AFTER);
        Date modifiedBefore = getExistingDateParameter(queryParameters, ConcurApiParameters.MODIFIED_BEFORE);
        String sortField = getExistingParameter(queryParameters, ConcurApiParameters.SORT_FIELD);
        String sortOrder = getExistingParameter(queryParameters, ConcurApiParameters.SORT_ORDER);
        
        Comparator<ConcurRequestV4ListItemDTO> requestSorter = Comparator.comparing(
                ConcurRequestV4ListItemDTO::getStartDate, Comparator.reverseOrder());
        
        if (!StringUtils.equalsIgnoreCase(RequestV4Views.APPROVED, view)) {
            throw new IllegalArgumentException(
                    "This mock backend only supports the APPROVED view; actual requested view: " + view);
        } else if (startIndex < 0) {
            throw new IllegalArgumentException("start index cannot be negative");
        } else if (limit < 1 || limit > MAX_RESULT_LIMIT) {
            throw new IllegalArgumentException("limit cannot be non-positive and cannot be greater than 100");
        } else if (modifiedAfter.compareTo(modifiedBefore) > 0) {
            throw new IllegalArgumentException("modifiedAfter date cannot be later than modifiedBefore date");
        } else if (!StringUtils.equalsIgnoreCase(ConcurConstants.REQUEST_QUERY_START_DATE_FIELD, sortField)) {
            throw new IllegalArgumentException(
                    "This mock backend only supports sorting by start date; actual sort field: " + sortField);
        } else if (!StringUtils.equalsIgnoreCase(ConcurConstants.REQUEST_QUERY_SORT_ORDER_DESC, sortOrder)) {
            throw new IllegalArgumentException(
                    "This mock backend only supports descending sort order; actual sort order: " + sortOrder);
        }
        
        List<ConcurRequestV4ListItemDTO> unboundedResults = travelRequests.values().stream()
                .filter(requestEntry -> requestWasLastModifiedWithinDateRange(
                        requestEntry, modifiedAfter, modifiedBefore))
                .filter(requestEntry -> requestHasAppropriateStatus(requestEntry))
                .map(requestEntry -> requestEntry.requestAsListItem)
                .sorted(requestSorter)
                .collect(Collectors.toUnmodifiableList());
        
        return buildRequestListing(unboundedResults, queryParameters);
    }

    private Set<String> searchForUnsupportedRequestListQueryParameters(Map<String, String> queryParameters) {
        return queryParameters.keySet().stream()
                .filter(paramName -> !ALLOWED_REQUEST_LIST_QUERY_PARAMETERS.contains(paramName))
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean requestWasLastModifiedWithinDateRange(RequestEntry requestEntry, Date rangeStart, Date rangeEnd) {
        Date lastModifiedDate = requestEntry.requestDetail.getLastModifiedDate();
        return lastModifiedDate != null && lastModifiedDate.compareTo(rangeStart) >= 0
                && lastModifiedDate.compareTo(rangeEnd) < 0;
    }

    private boolean requestHasAppropriateStatus(RequestEntry requestEntry) {
        ConcurRequestV4StatusDTO latestStatus = requestEntry.requestDetail.getApprovalStatus();
        return StringUtils.equalsAnyIgnoreCase(latestStatus.getName(),
                RequestV4Status.PENDING_EXTERNAL_VALIDATION.name, RequestV4Status.APPROVED.name);
    }

    private ConcurRequestV4ListingDTO buildRequestListing(List<ConcurRequestV4ListItemDTO> unboundedResults,
            Map<String, String> queryParameters) {
        int startIndex = getExistingIntParameter(queryParameters, ConcurApiParameters.START);
        int limit = getExistingIntParameter(queryParameters, ConcurApiParameters.LIMIT);
        int totalCount = unboundedResults.size();
        int endIndex = Math.min(startIndex + limit, totalCount);
        int startIndexForLastPage = ConcurFixtureUtils
                .calculateSearchStartIndexForLastPageOfResults(totalCount, limit);
        
        Stream.Builder<ConcurRequestV4OperationDTO> operationsBuilder = Stream.builder();
        if (startIndex - limit >= 0) {
            operationsBuilder.add(buildOperation(queryParameters, ConcurApiOperations.PREV, startIndex - limit));
        }
        if (startIndex + limit < totalCount) {
            operationsBuilder.add(buildOperation(queryParameters, ConcurApiOperations.NEXT, startIndex + limit));
        }
        operationsBuilder.add(buildOperation(queryParameters, ConcurApiOperations.FIRST, 0));
        operationsBuilder.add(buildOperation(queryParameters, ConcurApiOperations.LAST, startIndexForLastPage));
        List<ConcurRequestV4OperationDTO> operations = operationsBuilder.build()
                .collect(Collectors.toUnmodifiableList());
        
        ConcurRequestV4ListingDTO requestListing = new ConcurRequestV4ListingDTO();
        requestListing.setTotalCount(Integer.valueOf(totalCount));
        requestListing.setListItems(unboundedResults.subList(startIndex, endIndex));
        requestListing.setOperations(operations);
        return requestListing;
    }

    private ConcurRequestV4OperationDTO buildOperation(Map<String, String> queryParameters,
            String name, int startIndex) {
        Map<String, String> operationParameters = new HashMap<>(queryParameters);
        operationParameters.put(ConcurApiParameters.START, String.valueOf(startIndex));
        
        ConcurRequestV4OperationDTO operation = new ConcurRequestV4OperationDTO();
        operation.setName(name);
        operation.setHref(UrlFactory.parameterizeUrl(baseRequestV4Url, operationParameters));
        return operation;
    }

    private Date getExistingDateParameter(Map<String, String> queryParameters, String key) {
        String value = getExistingParameter(queryParameters, key);
        try {
            return ConcurUtils.parseUTCDateToDateTime(value).toDate();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Query has badly formatted date parameter " + key, e);
        }
    }

    private int getExistingIntParameter(Map<String, String> queryParameters, String key) {
        String value = getExistingParameter(queryParameters, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Query has badly formatted int parameter " + key, e);
        }
    }

    private String getExistingParameter(Map<String, String> queryParameters, String key) {
        String value = queryParameters.get(key);
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Query has blank or missing parameter: " + key);
        }
        return value;
    }

    private static final class RequestEntry {
        private final RequestV4DetailFixture requestFixture;
        private final ConcurRequestV4ListItemDTO requestAsListItem;
        private final ConcurRequestV4ReportDTO requestDetail;
        
        public RequestEntry(RequestV4DetailFixture requestFixture, String baseRequestUrl) {
            this.requestFixture = requestFixture;
            this.requestAsListItem = requestFixture.toConcurRequestV4ListItemDTO(baseRequestUrl);
            this.requestDetail = requestFixture.toConcurRequestV4ReportDTO(baseRequestUrl);
        }
    }

}
