package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4PersonDTO;
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
                accessToken, dateTimeService.getCurrentDate(), buildUserIdFilter());
        
        List<ConcurEventNotificationProcessingResultsDTO> results = findAndProcessPendingTravelRequests(
                querySettings, this::processTravelRequestsSubset);
        
        updateLastProcessedDateIfNecessary(querySettings);
        
        LOG.info("processTravelRequests, Finished processing " + results.size() + " travel requests");
        return results;
    }

    protected Predicate<String> buildUserIdFilter() {
        Map<String, String> testRequestUsers = getRequestV4TestUserMappings();
        if (isProduction()) {
            LOG.info("buildUserIdFilter, Only Production travel requests will be processed for this run");
            return userId -> !testRequestUsers.containsKey(userId);
        } else {
            LOG.info("buildUserIdFilter, Only non-Production travel requests will be processed for this run");
            return userId -> testRequestUsers.containsKey(userId);
        }
    }

    protected <T> List<T> findAndProcessPendingTravelRequests(RequestV4QuerySettings querySettings,
            BiFunction<RequestV4QuerySettings, ConcurRequestV4ListingDTO, Stream<T>> requestListingProcessor) {
        Stream.Builder<Stream<T>> subResults = Stream.builder();
        Stream<T> subResult;
        String initialQueryUrl = buildInitialRequestQueryUrl(querySettings);
        String currentQueryUrl = initialQueryUrl;
        int page = 1;
        ConcurRequestV4ListingDTO requestListing;
        
        do {
            requestListing = findPendingTravelRequests(querySettings.accessToken, page, currentQueryUrl);
            subResult = requestListingProcessor.apply(querySettings, requestListing);
            subResults.add(subResult);
            page++;
            currentQueryUrl = getQueryUrlForNextResultsPageIfPresent(requestListing);
        } while (StringUtils.isNotBlank(currentQueryUrl));
        
        return subResults.build()
                .flatMap(subResultStream -> subResultStream)
                .collect(Collectors.toUnmodifiableList());
    }

    protected String getQueryUrlForNextResultsPageIfPresent(ConcurRequestV4ListingDTO requestListing) {
        return Stream.of(requestListing.getOperations())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .filter(operation -> StringUtils.equalsIgnoreCase(ConcurApiOperations.NEXT, operation.getName()))
                .map(ConcurRequestV4OperationDTO::getHref)
                .peek(this::verifyQueryUrlForNextResultsPageIsValid)
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);
    }

    protected void verifyQueryUrlForNextResultsPageIsValid(String queryUrl) {
        if (!ConcurUtils.validateFormatAndPrefixOfParameterizedUrl(queryUrl, getRequestV4Endpoint())) {
            throw new RuntimeException("Query URL for next page of search results is malformed");
        }
    }

    protected ConcurRequestV4ListingDTO findPendingTravelRequests(String accessToken, int page, String queryUrl) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING),
                page);
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ListingDTO.class, messageDetail);
    }

    protected Stream<ConcurEventNotificationProcessingResultsDTO> processTravelRequestsSubset(
            RequestV4QuerySettings querySettings, ConcurRequestV4ListingDTO requestListing) {
        if (CollectionUtils.isEmpty(requestListing.getListItems())) {
            return Stream.empty();
        }
        Stream.Builder<ConcurEventNotificationProcessingResultsDTO> subResults = Stream.builder();
        
        for (ConcurRequestV4ListItemDTO requestAsListItem : requestListing.getListItems()) {
            if (StringUtils.isBlank(requestAsListItem.getId())) {
                throw new IllegalStateException("Found a request item with a blank ID; this should NEVER happen");
            }
            if (isRequestPendingExternalValidation(requestAsListItem)
                    && requestHasAppropriateOwner(requestAsListItem, querySettings.userIdFilter)) {
                ConcurEventNotificationProcessingResultsDTO processingResultForRequest = processTravelRequest(
                        querySettings.accessToken, requestAsListItem);
                subResults.add(processingResultForRequest);
            }
        }
        
        return subResults.build();
    }

    protected boolean isRequestPendingExternalValidation(ConcurRequestV4ListItemDTO requestAsListItem) {
        ConcurRequestV4StatusDTO approvalStatus = requestAsListItem.getApprovalStatus();
        return ObjectUtils.isNotNull(approvalStatus) && StringUtils.equalsIgnoreCase(
                RequestV4Status.PENDING_EXTERNAL_VALIDATION.name, approvalStatus.getName());
    }

    protected boolean requestHasAppropriateOwner(ConcurRequestV4ListItemDTO requestAsListItem,
            Predicate<String> userIdFilter) {
        ConcurRequestV4PersonDTO owner = requestAsListItem.getOwner();
        return ObjectUtils.isNotNull(owner) && StringUtils.isNotBlank(owner.getId())
                && userIdFilter.test(owner.getId());
    }

    protected ConcurEventNotificationProcessingResultsDTO processTravelRequest(String accessToken,
            ConcurRequestV4ListItemDTO requestAsListItem) {
        String requestUuid = requestAsListItem.getId();
        if (!StringUtils.isAlphanumeric(requestUuid)) {
            throw new RuntimeException("Found a request with an unexpected non-alphanumeric ID: " + requestUuid);
        }
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
        urlParameters.put(ConcurApiParameters.SORT_FIELD, ConcurConstants.REQUEST_QUERY_START_DATE_FIELD);
        urlParameters.put(ConcurApiParameters.SORT_ORDER, ConcurConstants.REQUEST_QUERY_SORT_ORDER_DESC);
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

    protected Map<String, String> getRequestV4TestUserMappings() {
        String testUsersString = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_TEST_USERS);
        if (StringUtils.isBlank(testUsersString)) {
            throw new IllegalStateException("Test users for Request V4 API were not specified in the parameter");
        }
        String[] testUserEntries = StringUtils.split(testUsersString, CUKFSConstants.SEMICOLON);
        return Arrays.stream(testUserEntries)
                .collect(Collectors.toUnmodifiableMap(
                        this::getTestUserUuidFromEntry, this::getTestUserNameFromEntry));
    }

    protected String getTestUserNameFromEntry(String keyValuePair) {
        return StringUtils.substringBefore(keyValuePair, CUKFSConstants.EQUALS_SIGN);
    }

    protected String getTestUserUuidFromEntry(String keyValuePair) {
        return StringUtils.substringAfter(keyValuePair, CUKFSConstants.EQUALS_SIGN);
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
        public final Predicate<String> userIdFilter;
        
        public RequestV4QuerySettings(String accessToken, Date currentDate, Predicate<String> userIdFilter) {
            this(accessToken, currentDate.getTime(), userIdFilter);
        }
        
        public RequestV4QuerySettings(String accessToken, long currentTimeInMilliseconds,
                Predicate<String> userIdFilter) {
            this.accessToken = accessToken;
            this.currentTimeInMilliseconds = currentTimeInMilliseconds;
            this.userIdFilter = userIdFilter;
        }
    }

}
