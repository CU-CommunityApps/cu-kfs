package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.UrlFactory;

import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Views;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestV4Service;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurRequestV4ServiceImpl implements ConcurRequestV4Service {

    private static final Logger LOG = LogManager.getLogger();

    protected static final String PROCESSING_ERROR_MESSAGE = "Encountered and error while processing travel request";

    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;

    @Override
    public List<ConcurEventNotificationProcessingResultsDTO> processTravelRequests(String accessToken) {
        List<Optional<String>> approversForQuery = findRequestApproversForSearchQuery();
        Set<String> excludedApprovers = findRequestApproversToExclude();
        return null;
    }

    protected Stream<ConcurEventNotificationProcessingResultsDTO> processTravelRequests(
            String accessToken, Optional<String> approverId, Set<String> excludedApprovers) {
        int startIndex = 0;
        int pageSize = findRequestV4QueryPageSize();
        Stream.Builder<ConcurEventNotificationProcessingResultsDTO> results = Stream.builder();
        ConcurRequestV4ListingDTO requestListing;
        
        do {
            requestListing = findPendingTravelRequests(accessToken, startIndex, approverId);
            for (ConcurRequestV4ListItemDTO requestAsListItem : requestListing.getListItems()) {
                ConcurEventNotificationProcessingResultsDTO result = processTravelRequest(
                        accessToken, requestAsListItem);
                results.add(result);
            }
            startIndex += pageSize;
        } while (requestListingHasMorePages(requestListing));
        
        return results.build();
    }

    protected boolean requestListingHasMorePages(ConcurRequestV4ListingDTO requestListing) {
        return CollectionUtils.isNotEmpty(requestListing.getOperations())
                && requestListing.getOperations().stream()
                        .anyMatch(operation -> StringUtils.equals(ConcurApiOperations.NEXT, operation.getName()));
    }

    protected ConcurEventNotificationProcessingResultsDTO processTravelRequest(String accessToken,
            ConcurRequestV4ListItemDTO requestAsListItem) {
        String requestUuid = requestAsListItem.getId();
        ConcurEventNotificationVersion2ProcessingResults processingResult;
        List<String> validationMessages = new ArrayList<>();
        boolean requestValid = true;
        try {
            ConcurRequestV4ReportDTO request = findPendingTravelRequest(accessToken, requestUuid);
            ConcurAccountInfo accountInfo = buildAccountInfo(request);
        } catch (Exception e) {
            LOG.error("processTravelRequest, Unexpected error encountered while processing request " + requestUuid, e);
            requestValid = false;
            processingResult = ConcurEventNotificationVersion2ProcessingResults.processingError;
            validationMessages.add(PROCESSING_ERROR_MESSAGE);
        }
        return null;
    }

    protected ConcurAccountInfo buildAccountInfo(ConcurRequestV4ReportDTO request) {
        ConcurAccountInfo accountInfo = new ConcurAccountInfo();
        return accountInfo;
    }

    protected ConcurRequestV4ListingDTO findPendingTravelRequests(String accessToken, int startIndex,
            Optional<String> approverId) {
        String messageKey = (startIndex == 0) ? ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING
                : ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING_NEXT_PAGE;
        String messageDetail = configurationService.getPropertyValueAsString(messageKey);
        String queryUrl = buildRequestQueryUrl(startIndex, approverId);
        
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ListingDTO.class, messageDetail);
    }

    protected ConcurRequestV4ReportDTO findPendingTravelRequest(String accessToken, String requestUuid) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_REQUEST),
                requestUuid);
        String queryUrl = findRequestV4Endpoint() + CUKFSConstants.SLASH + UrlFactory.encode(requestUuid);
        
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ReportDTO.class, messageDetail);
    }

    protected String buildRequestQueryUrl(int startIndex, Optional<String> approverId) {
        String baseUrl = findRequestV4Endpoint();
        int pageSize = findRequestV4QueryPageSize();
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(ConcurApiParameters.VIEW, RequestV4Views.TO_APPROVE);
        urlParameters.put(ConcurApiParameters.START, Integer.toString(startIndex));
        urlParameters.put(ConcurApiParameters.LIMIT, Integer.toString(pageSize));
        if (approverId.isPresent()) {
            urlParameters.put(ConcurApiParameters.USER_ID, approverId.get());
        }
        return UrlFactory.parameterizeUrl(baseUrl, urlParameters);
    }

    protected String findRequestV4Endpoint() {
        String requestEndpoint = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_LISTING_ENDPOINT);
        if (StringUtils.isBlank(requestEndpoint)) {
            throw new IllegalStateException("Endpoint for Request V4 API was not specified in the parameter");
        }
        return requestEndpoint;
    }

    protected int findRequestV4QueryPageSize() {
        String pageSize = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE);
        if (!StringUtils.isNumeric(pageSize)) {
            throw new IllegalStateException("Page size for Request V4 API was either not specified in the parameter "
                    + "or was not a positive integer");
        }
        try {
            return Integer.parseInt(pageSize);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unexpected error encountered when parsing Request V4 page size", e);
        }
    }

    protected List<Optional<String>> findRequestApproversForSearchQuery() {
        if (isProduction()) {
            return List.of(Optional.empty());
        } else {
            return findRequestV4TestApprovers().stream()
                    .map(Optional::of)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    protected Set<String> findRequestApproversToExclude() {
        if (isProduction()) {
            return Set.copyOf(findRequestV4TestApprovers());
        } else {
            return Set.of();
        }
    }

    protected List<String> findRequestV4TestApprovers() {
        String testApprovers = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_TEST_APPROVERS);
        if (StringUtils.isBlank(testApprovers)) {
            throw new IllegalStateException("Test approvers for Request V4 API were not specified in the parameter");
        }
        String[] testApproversArray = StringUtils.split(testApprovers, CUKFSConstants.SEMICOLON);
        return Arrays.stream(testApproversArray)
                .collect(Collectors.toUnmodifiableList());
    }

    protected boolean isProduction() {
        return ConfigContext.getCurrentContextConfig().isProductionEnvironment();
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setConcurEventNotificationV2WebserviceService(
            ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService) {
        this.concurEventNotificationV2WebserviceService = concurEventNotificationV2WebserviceService;
    }

    public void setConcurAccountValidationService(ConcurAccountValidationService concurAccountValidationService) {
        this.concurAccountValidationService = concurAccountValidationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
