package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiOperations;
import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Status;
import edu.cornell.kfs.concur.ConcurConstants.RequestV4Views;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestV4Service;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4CustomItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4OperationDTO;
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
        RequestV4QuerySettings querySettings = new RequestV4QuerySettings(
                accessToken, dateTimeService.getCurrentDate(), Optional.empty());
        Set<String> testRequestIds = findIdsOfPendingRequestsApprovedByTestUsers(querySettings);
        Predicate<String> requestIdFilter;
        if (isProduction()) {
            LOG.info("processTravelRequests, Only Production travel requests will be processed for this run");
            requestIdFilter = requestId -> !testRequestIds.contains(requestId);
        } else {
            LOG.info("processTravelRequests, Only non-Production travel requests will be processed for this run");
            requestIdFilter = requestId -> testRequestIds.contains(requestId);
        }
        
        Stream<ConcurEventNotificationProcessingResultsDTO> streamResults = findAndProcessPendingTravelRequests(
                querySettings,
                (token, requestListing) -> processTravelRequestsSubset(token, requestListing, requestIdFilter));
        
        List<ConcurEventNotificationProcessingResultsDTO> finalResults = streamResults.collect(
                Collectors.toUnmodifiableList());
        
        updateLastProcessedDateIfNecessary(querySettings);
        
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
        String queryUrl = getRequestV4Endpoint() + CUKFSConstants.SLASH + UrlFactory.encode(requestUuid);
        
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ReportDTO.class, messageDetail);
    }

    protected Set<String> findIdsOfPendingRequestsApprovedByTestUsers(RequestV4QuerySettings basicSettings) {
        Map<String, String> testApprovers = getRequestV4TestApproverMappings();
        Set<String> approverNames = testApprovers.keySet();
        LOG.info("findIdsOfPendingRequestsApprovedByTestUsers, Finding requests pending external validation "
                + "and approved by test approvers: " + approverNames.toString());
        Set<String> requestIds = testApprovers.values().stream()
                .map(approverId -> new RequestV4QuerySettings(
                        basicSettings.accessToken, basicSettings.currentTimeInMilliseconds, Optional.of(approverId)))
                .flatMap(querySettings -> findAndProcessPendingTravelRequests(
                        querySettings, this::findIdsOfPendingRequestsInListing))
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
                RequestV4Status.PENDING_EXTERNAL_VALIDATION.name, approvalStatus.getName());
    }

    protected <T> Stream<T> findAndProcessPendingTravelRequests(RequestV4QuerySettings querySettings,
            BiFunction<String, ConcurRequestV4ListingDTO, Stream<T>> requestListingProcessor) {
        String initialQueryUrl = buildInitialRequestQueryUrl(querySettings);
        return findAndProcessPendingTravelRequests(
                querySettings.accessToken, initialQueryUrl, requestListingProcessor);
    }

    protected <T> Stream<T> findAndProcessPendingTravelRequests(String accessToken, String initialQueryUrl,
            BiFunction<String, ConcurRequestV4ListingDTO, Stream<T>> requestListingProcessor) {
        Stream.Builder<Stream<T>> subResults = Stream.builder();
        Stream<T> subResult;
        String currentQueryUrl = initialQueryUrl;
        int page = 1;
        ConcurRequestV4ListingDTO requestListing;
        
        do {
            requestListing = findPendingTravelRequests(accessToken, page, currentQueryUrl);
            subResult = requestListingProcessor.apply(accessToken, requestListing);
            subResults.add(subResult);
            page++;
            currentQueryUrl = getQueryUrlForNextResultsPageIfPresent(requestListing);
        } while (StringUtils.isNotBlank(currentQueryUrl));
        
        return subResults.build()
                .flatMap(subResultStream -> subResultStream);
    }

    protected String getQueryUrlForNextResultsPageIfPresent(ConcurRequestV4ListingDTO requestListing) {
        return Stream.of(requestListing.getOperations())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(operation -> StringUtils.equalsIgnoreCase(ConcurApiOperations.NEXT, operation.getName()))
                .map(ConcurRequestV4OperationDTO::getHref)
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);
    }

    protected ConcurRequestV4ListingDTO findPendingTravelRequests(String accessToken, int page, String queryUrl) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING),
                page);
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ListingDTO.class, messageDetail);
    }

    protected String buildInitialRequestQueryUrl(RequestV4QuerySettings querySettings) {
        String baseUrl = getRequestV4Endpoint();
        int pageSize = getRequestV4QueryPageSize();
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(ConcurApiParameters.VIEW, RequestV4Views.APPROVED);
        urlParameters.put(ConcurApiParameters.START, Integer.toString(0));
        urlParameters.put(ConcurApiParameters.LIMIT, Integer.toString(pageSize));
        urlParameters.put(ConcurApiParameters.MODIFIED_AFTER, getLastModifiedFromDateInUTCFormat());
        urlParameters.put(ConcurApiParameters.MODIFIED_BEFORE,
                getLastModifiedToDateInUTCFormat(querySettings.currentTimeInMilliseconds));
        if (querySettings.approverId.isPresent()) {
            urlParameters.put(ConcurApiParameters.USER_ID, querySettings.approverId.get());
        }
        return UrlFactory.parameterizeUrl(baseUrl, urlParameters);
    }

    protected String getRequestV4Endpoint() {
        String requestEndpoint = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT);
        if (StringUtils.isBlank(requestEndpoint)) {
            throw new IllegalStateException("Endpoint for Request V4 API was not specified in the parameter");
        }
        return requestEndpoint;
    }

    protected int getRequestV4QueryPageSize() {
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

    protected Map<String, String> getRequestV4TestApproverMappings() {
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

    protected String getLastModifiedFromDateInUTCFormat() {
        String dateString = getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_QUERY_FROM_DATE);
        if (StringUtils.equalsIgnoreCase(dateString, ConcurConstants.REQUEST_QUERY_LAST_DATE_INDICATOR)) {
            dateString = getNonBlankConcurParameterValue(
                    ConcurParameterConstants.REQUEST_V4_QUERY_LAST_PROCESSED_DATE);
        }
        return convertToUTCDateString(dateString);
    }

    protected String getLastModifiedToDateInUTCFormat(long currentTimeInMilliseconds) {
        String dateString = getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_QUERY_TO_DATE);
        if (StringUtils.equalsIgnoreCase(dateString, ConcurConstants.REQUEST_QUERY_CURRENT_DATE_INDICATOR)) {
            return ConcurUtils.formatAsUTCDate(new Date(currentTimeInMilliseconds));
        } else {
            return convertToUTCDateString(dateString);
        }
    }

    protected String getNonBlankConcurParameterValue(String parameterName) {
        String value = concurBatchUtilityService.getConcurParameterValue(parameterName);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("Invalid blank value detected for parameter " + parameterName);
        }
        return value;
    }

    protected String convertToUTCDateString(String dateString) {
        try {
            Date dateValue = dateTimeService.convertToDateTime(dateString);
            return ConcurUtils.formatAsUTCDate(dateValue);
        } catch (ParseException e) {
            throw new IllegalStateException("Could not parse date string", e);
        }
    }

    protected MutableDateTime convertToStartOfDayInTimeZone(Date date, TimeZone timeZone) {
        MutableDateTime dateTime = new MutableDateTime(date, DateTimeZone.forTimeZone(timeZone));
        dateTime.setTime(0, 0, 0, 0);
        return dateTime;
    }

    protected void updateLastProcessedDateIfNecessary(RequestV4QuerySettings querySettings) {
        String fromDate = getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_QUERY_FROM_DATE);
        String toDate = getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_QUERY_TO_DATE);
        if (StringUtils.equalsIgnoreCase(fromDate, ConcurConstants.REQUEST_QUERY_LAST_DATE_INDICATOR)
                && StringUtils.equalsIgnoreCase(toDate, ConcurConstants.REQUEST_QUERY_CURRENT_DATE_INDICATOR)) {
            LOG.info("updateLastProcessedDateIfNecessary, Updating "
                    + ConcurParameterConstants.REQUEST_V4_QUERY_LAST_PROCESSED_DATE
                    + " to current date and time that was used for this run");
            Date currentDateFromSearch = new Date(querySettings.currentTimeInMilliseconds);
            String newLastProcessedDateString = dateTimeService.toDateTimeString(currentDateFromSearch);
            concurBatchUtilityService.updateConcurParameterValue(
                    ConcurParameterConstants.REQUEST_V4_QUERY_LAST_PROCESSED_DATE, newLastProcessedDateString);
        } else {
            LOG.info("updateLastProcessedDateIfNecessary, Query from/to parameters have been overridden; "
                    + ConcurParameterConstants.REQUEST_V4_QUERY_LAST_PROCESSED_DATE + " will remain as-is");
        }
    }

    protected void resetParameterToEmptyValueIfNecessary(String parameterName) {
        String currentValue = concurBatchUtilityService.getConcurParameterValue(parameterName);
        if (StringUtils.isNotBlank(currentValue)) {
            concurBatchUtilityService.updateConcurParameterValue(parameterName, KFSConstants.EMPTY_STRING);
        }
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

    protected static final class RequestV4QuerySettings {
        public final String accessToken;
        public final long currentTimeInMilliseconds;
        public final Optional<String> approverId;
        
        public RequestV4QuerySettings(String accessToken, Date currentDate, Optional<String> approverId) {
            this(accessToken, currentDate.getTime(), approverId);
        }
        
        public RequestV4QuerySettings(String accessToken, long currentTimeInMilliseconds,
                Optional<String> approverId) {
            this.accessToken = accessToken;
            this.currentTimeInMilliseconds = currentTimeInMilliseconds;
            this.approverId = approverId;
        }
    }

}
