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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.UrlFactory;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4StatusCodes;
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

    private static final Logger LOG = LogManager.getLogger();

    private static final int MAX_RESULT_LIMIT = 100;

    private static final Set<String> ALLOWED_REQUEST_LIST_QUERY_PARAMETERS = Set.of(
            ConcurApiParameters.VIEW, ConcurApiParameters.START, ConcurApiParameters.LIMIT,
            ConcurApiParameters.MODIFIED_AFTER, ConcurApiParameters.MODIFIED_BEFORE,
            ConcurApiParameters.SORT_FIELD, ConcurApiParameters.SORT_ORDER);

    private ConcurrentMap<String, RequestV4DetailFixture> travelRequests;
    private ConcurrentMap<RequestV4DetailFixture, ConcurRequestV4ListItemDTO> requestsAsListItems;
    private ConcurrentMap<RequestV4DetailFixture, ConcurRequestV4ReportDTO> requestsAsDetails;
    private String baseRequestV4Url;

    public MockConcurRequestV4Backend(String baseRequestV4Url, RequestV4DetailFixture... requestsToAdd) {
        this.travelRequests = new ConcurrentHashMap<>();
        this.requestsAsListItems = new ConcurrentHashMap<>();
        this.requestsAsDetails = new ConcurrentHashMap<>();
        this.baseRequestV4Url = baseRequestV4Url;
        addTravelRequests(requestsToAdd);
    }

    public void addTravelRequests(RequestV4DetailFixture... requestsToAdd) {
        for (RequestV4DetailFixture travelRequest : requestsToAdd) {
            travelRequests.put(travelRequest.id, travelRequest);
        }
    }

    public Optional<ConcurRequestV4ReportDTO> findRequest(String id) {
        RequestV4DetailFixture matchingFixture = travelRequests.get(id);
        return Optional.ofNullable(matchingFixture)
                .map(this::getRequestAsDetail);
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
        
        if (!StringUtils.equalsIgnoreCase(RequestV4Views.SUBMITTED, view)) {
            LOG.warn("findRequests, Unexpected query view was specified: " + view);
        }
        if (!StringUtils.equalsIgnoreCase(ConcurConstants.REQUEST_QUERY_START_DATE_FIELD, sortField)) {
            LOG.warn("findRequests, Unexpected sort field was specified: " + sortField);
        }
        if (!StringUtils.equalsIgnoreCase(ConcurConstants.REQUEST_QUERY_SORT_ORDER_DESC, sortOrder)) {
            LOG.warn("findRequests, Unexpected sort order was specified: " + sortOrder);
        }
        
        if (startIndex < 0) {
            throw new IllegalArgumentException("start index cannot be negative");
        } else if (limit < 1 || limit > MAX_RESULT_LIMIT) {
            throw new IllegalArgumentException("limit cannot be non-positive and cannot be greater than 100");
        } else if (modifiedAfter.compareTo(modifiedBefore) > 0) {
            throw new IllegalArgumentException("modifiedAfter date cannot be later than modifiedBefore date");
        }
        
        List<ConcurRequestV4ListItemDTO> unboundedResults = travelRequests.values().stream()
                .filter(fixture -> requestWasLastModifiedWithinDateRange(fixture, modifiedAfter, modifiedBefore))
                .filter(fixture -> requestHasAppropriateStatus(fixture))
                .map(this::getRequestAsListItem)
                .sorted(requestSorter)
                .collect(Collectors.toUnmodifiableList());
        
        return buildRequestListing(unboundedResults, queryParameters);
    }

    private Set<String> searchForUnsupportedRequestListQueryParameters(Map<String, String> queryParameters) {
        return queryParameters.keySet().stream()
                .filter(paramName -> !ALLOWED_REQUEST_LIST_QUERY_PARAMETERS.contains(paramName))
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean requestWasLastModifiedWithinDateRange(
            RequestV4DetailFixture requestFixture, Date rangeStart, Date rangeEnd) {
        ConcurRequestV4ReportDTO requestDetail = getRequestAsDetail(requestFixture);
        Date lastModifiedDate = requestDetail.getLastModifiedDate();
        return lastModifiedDate != null && lastModifiedDate.compareTo(rangeStart) >= 0
                && lastModifiedDate.compareTo(rangeEnd) < 0;
    }

    private boolean requestHasAppropriateStatus(RequestV4DetailFixture requestFixture) {
        ConcurRequestV4ReportDTO requestDetail = getRequestAsDetail(requestFixture);
        ConcurRequestV4StatusDTO latestStatus = requestDetail.getApprovalStatus();
        return StringUtils.equalsAnyIgnoreCase(latestStatus.getCode(),
                RequestV4StatusCodes.SUBMITTED, RequestV4StatusCodes.APPROVED);
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

    private ConcurRequestV4ListItemDTO getRequestAsListItem(RequestV4DetailFixture requestFixture) {
        return requestsAsListItems.computeIfAbsent(requestFixture,
                fixture -> fixture.toConcurRequestV4ListItemDTO(baseRequestV4Url));
    }

    private ConcurRequestV4ReportDTO getRequestAsDetail(RequestV4DetailFixture requestFixture) {
        return requestsAsDetails.computeIfAbsent(requestFixture,
                fixture -> fixture.toConcurRequestV4ReportDTO(baseRequestV4Url));
    }

}
