package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4StatusNames;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Views;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestV4Service;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4CustomItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4StatusDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurRequestV4ServiceImpl implements ConcurRequestV4Service {

    private static final Logger LOG = LogManager.getLogger();

    protected static final String PROCESSING_ERROR_MESSAGE = "Encountered an error while processing travel request";

    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;

    @Override
    public List<ConcurEventNotificationProcessingResultsDTO> processTravelRequests(String accessToken) {
        LOG.info("processTravelRequests, Starting processing of travel requests");
        Set<String> testRequestIds = findIdsOfPendingRequestsApprovedByTestUsers(accessToken);
        Predicate<String> requestIdFilter;
        if (isProduction()) {
            LOG.info("processTravelRequests, Only Production travel requests will be processed for this run");
            requestIdFilter = requestId -> !testRequestIds.contains(requestId);
        } else {
            LOG.info("processTravelRequests, Only non-Production travel requests will be processed for this run");
            requestIdFilter = requestId -> testRequestIds.contains(requestId);
        }
        
        Stream<ConcurEventNotificationProcessingResultsDTO> streamResults = findAndProcessPendingTravelRequests(
                accessToken, Optional.empty(),
                (token, requestListing) -> processTravelRequestsSubset(token, requestListing, requestIdFilter));
        
        List<ConcurEventNotificationProcessingResultsDTO> finalResults = streamResults.collect(
                Collectors.toUnmodifiableList());
        LOG.info("processTravelRequests, Finished processing " + finalResults.size() + " travel requests");
        return finalResults;
    }

    protected Stream<ConcurEventNotificationProcessingResultsDTO> processTravelRequestsSubset(
            String accessToken, ConcurRequestV4ListingDTO requestListing, Predicate<String> requestIdFilter) {
        if (CollectionUtils.isEmpty(requestListing.getListItems())) {
            return Stream.empty();
        }
        Stream.Builder<ConcurEventNotificationProcessingResultsDTO> subResults = Stream.builder();
        
        for (ConcurRequestV4ListItemDTO requestAsListItem : requestListing.getListItems()) {
            if (StringUtils.isBlank(requestAsListItem.getId())) {
                throw new IllegalStateException("Found a request item with a blank ID; this should NEVER happen");
            }
            if (isRequestPendingExternalValidation(requestAsListItem)
                    && requestIdFilter.test(requestAsListItem.getId())) {
                ConcurEventNotificationProcessingResultsDTO processingResultForRequest = processTravelRequest(
                        accessToken, requestAsListItem);
                subResults.add(processingResultForRequest);
            }
        }
        
        return subResults.build();
    }

    

    protected ConcurEventNotificationProcessingResultsDTO processTravelRequest(String accessToken,
            ConcurRequestV4ListItemDTO requestAsListItem) {
        String requestUuid = requestAsListItem.getId();
        ConcurEventNotificationVersion2ProcessingResults processingResult;
        List<String> validationMessages = new ArrayList<>();
        boolean requestValid;
        
        try {
            ConcurRequestV4ReportDTO request = findTravelRequest(accessToken, requestUuid);
            ConcurAccountInfo accountInfo = buildAccountInfo(request);
            LOG.info("processTravelRequest, Validating request " + requestUuid + " with account info: "
                    + accountInfo.toString());
            ValidationResult validationResult = concurAccountValidationService.validateConcurAccountInfo(accountInfo);
            requestValid = validationResult.isValid();
            validationMessages.addAll(validationResult.getMessages());
            processingResult = requestValid ? ConcurEventNotificationVersion2ProcessingResults.validAccounts
                    : ConcurEventNotificationVersion2ProcessingResults.invalidAccounts;
        } catch (Exception e) {
            LOG.error("processTravelRequest, Unexpected error encountered while validating request " + requestUuid, e);
            requestValid = false;
            processingResult = ConcurEventNotificationVersion2ProcessingResults.processingError;
            validationMessages.add(PROCESSING_ERROR_MESSAGE);
        }
        
        ConcurEventNotificationProcessingResultsDTO resultsDTO = new ConcurEventNotificationProcessingResultsDTO(
                ConcurEventNoticationVersion2EventType.TravelRequest, processingResult,
                requestAsListItem.getRequestId(), validationMessages);
        updateRequestStatusInConcur(accessToken, requestUuid, resultsDTO);
        
        return resultsDTO;
    }

    protected ConcurAccountInfo buildAccountInfo(ConcurRequestV4ReportDTO request) {
        ConcurAccountInfo accountInfo = new ConcurAccountInfo();
        accountInfo.setChart(defaultCodeToEmptyIfAbsent(request.getChart()));
        accountInfo.setAccountNumber(defaultCodeToEmptyIfAbsent(request.getAccount()));
        accountInfo.setSubAccountNumber(defaultCodeToEmptyIfAbsent(request.getSubAccount()));
        accountInfo.setObjectCode(concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE));
        accountInfo.setSubObjectCode(defaultCodeToEmptyIfAbsent(request.getSubObjectCode()));
        accountInfo.setProjectCode(defaultCodeToEmptyIfAbsent(request.getProjectCode()));
        return accountInfo;
    }

    protected String defaultCodeToEmptyIfAbsent(ConcurRequestV4CustomItemDTO item) {
        if (ObjectUtils.isNull(item)) {
            return KFSConstants.EMPTY_STRING;
        } else {
            return StringUtils.defaultIfBlank(item.getCode(), KFSConstants.EMPTY_STRING);
        }
    }

    protected void updateRequestStatusInConcur(
            String accessToken, String requestUuid, ConcurEventNotificationProcessingResultsDTO resultsDTO) {
        boolean isValid =
                (resultsDTO.getProcessingResults() == ConcurEventNotificationVersion2ProcessingResults.validAccounts);
        LOG.info("updateRequestStatusInConcur, Will notify Concur that Request " + requestUuid
                + " had an overall validation of " + isValid);
        
        LOG.info("updateRequestStatusInConcur, Update of the Request's status in Concur has not been implemented yet");
    }

    protected ConcurRequestV4ReportDTO findTravelRequest(String accessToken, String requestUuid) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_REQUEST),
                requestUuid);
        String queryUrl = findRequestV4Endpoint() + CUKFSConstants.SLASH + UrlFactory.encode(requestUuid);
        
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ReportDTO.class, messageDetail);
    }

    protected Set<String> findIdsOfPendingRequestsApprovedByTestUsers(String accessToken) {
        Map<String, String> testApprovers = findRequestV4TestApproverMappings();
        Set<String> approverNames = testApprovers.keySet();
        LOG.info("findIdsOfPendingRequestsApprovedByTestUsers, Finding requests pending external validation "
                + "and approved by test approvers: " + approverNames.toString());
        Set<String> requestIds = testApprovers.values().stream()
                .flatMap(approverId -> findAndProcessPendingTravelRequests(
                        accessToken, Optional.of(approverId), this::findIdsOfPendingRequestsInListing))
                .collect(Collectors.toUnmodifiableSet());
        LOG.info("findIdsOfPendingRequestsApprovedByTestUsers, Found " + requestIds.size()
                + " requests pending external validation and approved by test approvers: " + approverNames.toString());
        return requestIds;
    }

    protected Stream<String> findIdsOfPendingRequestsInListing(
            String accessToken, ConcurRequestV4ListingDTO requestListing) {
        if (CollectionUtils.isEmpty(requestListing.getListItems())) {
            return Stream.empty();
        }
        return requestListing.getListItems().stream()
                .filter(this::isRequestPendingExternalValidation)
                .map(ConcurRequestV4ListItemDTO::getId);
    }

    protected boolean isRequestPendingExternalValidation(ConcurRequestV4ListItemDTO requestAsListItem) {
        ConcurRequestV4StatusDTO approvalStatus = requestAsListItem.getApprovalStatus();
        return ObjectUtils.isNotNull(approvalStatus) && StringUtils.equalsIgnoreCase(
                RequestV4StatusNames.PENDING_EXTERNAL_VALIDATION, approvalStatus.getName());
    }

    protected <T> Stream<T> findAndProcessPendingTravelRequests(String accessToken, Optional<String> approverId,
            BiFunction<String, ConcurRequestV4ListingDTO, Stream<T>> requestListingProcessor) {
        Stream.Builder<Stream<T>> subResults = Stream.builder();
        Stream<T> subResult;
        int startIndex = 0;
        int pageSize = findRequestV4QueryPageSize();
        ConcurRequestV4ListingDTO requestListing;
        
        do {
            requestListing = findPendingTravelRequests(accessToken, startIndex, approverId);
            subResult = requestListingProcessor.apply(accessToken, requestListing);
            subResults.add(subResult);
            startIndex += pageSize;
        } while (requestListingHasMorePages(requestListing));
        
        return subResults.build()
                .flatMap(subResultStream -> subResultStream);
    }

    protected boolean requestListingHasMorePages(ConcurRequestV4ListingDTO requestListing) {
        return CollectionUtils.isNotEmpty(requestListing.getOperations())
                && requestListing.getOperations().stream()
                        .anyMatch(operation -> StringUtils.equalsIgnoreCase(
                                ConcurApiOperations.NEXT, operation.getName()));
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

    protected String buildRequestQueryUrl(int startIndex, Optional<String> approverId) {
        String baseUrl = findRequestV4Endpoint();
        int pageSize = findRequestV4QueryPageSize();
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(ConcurApiParameters.VIEW, RequestV4Views.APPROVED);
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

    protected Map<String, String> findRequestV4TestApproverMappings() {
        String testApproversString = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_TEST_APPROVERS);
        if (StringUtils.isBlank(testApproversString)) {
            throw new IllegalStateException("Test approvers for Request V4 API were not specified in the parameter");
        }
        String[] testApproverEntries = StringUtils.split(testApproversString, CUKFSConstants.SEMICOLON);
        return Arrays.stream(testApproverEntries)
                .map(approverEntry -> StringUtils.split(approverEntry, CUKFSConstants.EQUALS_SIGN))
                .collect(Collectors.toMap(
                        keyValuePair -> keyValuePair[0], keyValuePair -> keyValuePair[1],
                        (value1, value2) -> value2, LinkedHashMap::new));
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

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
