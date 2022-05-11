package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.MutableDateTime;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.factory.InitializingBean;

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

public class ConcurRequestV4ServiceImpl implements ConcurRequestV4Service, InitializingBean {

    private static final Logger LOG = LogManager.getLogger();

    protected static final String PROCESSING_ERROR_MESSAGE = "Encountered an error while processing travel request";

    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;

    protected final AtomicReference<Map<String, String>> testUserIdMappings = new AtomicReference<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        String testUserIdMappingsAsString = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.REQUEST_V4_TEST_USERS);
        refreshRequestV4TestUserMappingsFromParameterValue(testUserIdMappingsAsString);
        concurBatchUtilityService.watchConcurParameter(
                ConcurParameterConstants.REQUEST_V4_TEST_USERS, this::refreshRequestV4TestUserMappingsFromParameter);
    }

    @Override
    public List<ConcurEventNotificationProcessingResultsDTO> processTravelRequests(String accessToken) {
        LOG.info("processTravelRequests, Starting processing of "
                + (isProduction() ? "Production" : "Non-Production") + " travel requests");
        
        Stream.Builder<Stream<ConcurEventNotificationProcessingResultsDTO>> subResults = Stream.builder();
        Stream<ConcurEventNotificationProcessingResultsDTO> subResult;
        String initialQueryUrl = buildInitialRequestQueryUrl();
        String currentQueryUrl = initialQueryUrl;
        int page = 1;
        ConcurRequestV4ListingDTO requestListing;
        
        do {
            requestListing = getPendingTravelRequests(accessToken, page, currentQueryUrl);
            subResult = processTravelRequestsSubset(accessToken, requestListing);
            subResults.add(subResult);
            page++;
            currentQueryUrl = getQueryUrlForNextResultsPageIfPresent(requestListing);
        } while (StringUtils.isNotBlank(currentQueryUrl));
        
        List<ConcurEventNotificationProcessingResultsDTO> results = subResults.build()
                .flatMap(subResultStream -> subResultStream)
                .collect(Collectors.toUnmodifiableList());
        
        LOG.info("processTravelRequests, Finished processing " + results.size() + " travel requests");
        return results;
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
            LOG.error("verifyQueryUrlForNextResultsPageIsValid, Detected an unexpected or malformed search URL: "
                    + queryUrl);
            throw new RuntimeException("Query URL for next page of search results is malformed");
        }
    }

    protected ConcurRequestV4ListingDTO getPendingTravelRequests(String accessToken, int page, String queryUrl) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_LISTING),
                page);
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ListingDTO.class, messageDetail);
    }

    protected Stream<ConcurEventNotificationProcessingResultsDTO> processTravelRequestsSubset(
            String accessToken, ConcurRequestV4ListingDTO requestListing) {
        if (CollectionUtils.isEmpty(requestListing.getListItems())) {
            return Stream.empty();
        }
        Stream.Builder<ConcurEventNotificationProcessingResultsDTO> subResults = Stream.builder();
        
        for (ConcurRequestV4ListItemDTO requestAsListItem : requestListing.getListItems()) {
            if (StringUtils.isBlank(requestAsListItem.getId())) {
                throw new IllegalStateException("Found a request item with a blank ID; this should NEVER happen");
            }
            if (isRequestPendingExternalValidation(requestAsListItem)
                    && requestHasAppropriateOwner(requestAsListItem)) {
                ConcurEventNotificationProcessingResultsDTO processingResultForRequest = processTravelRequest(
                        accessToken, requestAsListItem);
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

    protected boolean requestHasAppropriateOwner(ConcurRequestV4ListItemDTO requestAsListItem) {
        ConcurRequestV4PersonDTO owner = requestAsListItem.getOwner();
        if (ObjectUtils.isNull(owner) || StringUtils.isBlank(owner.getId())) {
            return false;
        }
        boolean isTestUser = testUserIdMappings.get().containsKey(owner.getId());
        return isProduction() != isTestUser;
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
            ConcurRequestV4ReportDTO request = getTravelRequest(accessToken, requestUuid);
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

    protected ConcurRequestV4ReportDTO getTravelRequest(String accessToken, String requestUuid) {
        String messageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REQUESTV4_REQUEST),
                requestUuid);
        String queryUrl = getRequestV4Endpoint() + CUKFSConstants.SLASH + UrlFactory.encode(requestUuid);
        
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(
                accessToken, queryUrl, ConcurRequestV4ReportDTO.class, messageDetail);
    }

    protected String buildInitialRequestQueryUrl() {
        Date currentDate = dateTimeService.getCurrentDate();
        String currentUTCDate = ConcurUtils.formatAsUTCDate(currentDate);
        String baseUrl = getRequestV4Endpoint();
        int pageSize = getRequestV4QueryPageSize();
        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(ConcurApiParameters.VIEW, RequestV4Views.APPROVED);
        urlParameters.put(ConcurApiParameters.START, Integer.toString(0));
        urlParameters.put(ConcurApiParameters.LIMIT, Integer.toString(pageSize));
        urlParameters.put(ConcurApiParameters.MODIFIED_AFTER, calculateLastModifiedFromDateInUTCFormat(currentDate));
        urlParameters.put(ConcurApiParameters.MODIFIED_BEFORE, currentUTCDate);
        urlParameters.put(ConcurApiParameters.SORT_FIELD, ConcurConstants.REQUEST_QUERY_START_DATE_FIELD);
        urlParameters.put(ConcurApiParameters.SORT_ORDER, ConcurConstants.REQUEST_QUERY_SORT_ORDER_DESC);
        return UrlFactory.parameterizeUrl(baseUrl, urlParameters);
    }

    protected String getRequestV4Endpoint() {
        return getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_REQUESTS_ENDPOINT);
    }

    protected int getRequestV4QueryPageSize() {
        return getConcurParameterValueAsPositiveInteger(ConcurParameterConstants.REQUEST_V4_QUERY_PAGE_SIZE);
    }

    protected void refreshRequestV4TestUserMappingsFromParameter(Parameter parameter) {
        LOG.info("refreshRequestV4TestUserMappingsFromParameter, The value of the parameter "
                + parameter.getName() + " has been updated");
        refreshRequestV4TestUserMappingsFromParameterValue(parameter.getValue());
    }

    protected void refreshRequestV4TestUserMappingsFromParameterValue(String parameterValue) {
        Map<String, String> newMappings = getRequestV4TestUserMappingsFromParameterValue(parameterValue);
        testUserIdMappings.set(newMappings);
    }

    protected Map<String, String> getRequestV4TestUserMappingsFromParameterValue(String parameterValue) {
        if (StringUtils.isBlank(parameterValue)) {
            return Map.of();
        }
        String testUsersString = getNonBlankConcurParameterValue(ConcurParameterConstants.REQUEST_V4_TEST_USERS);
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

    protected String calculateLastModifiedFromDateInUTCFormat(Date currentDate) {
        int numDaysOld = getConcurParameterValueAsPositiveInteger(
                ConcurParameterConstants.REQUEST_V4_NUMBER_OF_DAYS_OLD);
        MutableDateTime dateTime = new MutableDateTime(currentDate.getTime());
        dateTime.addDays(-numDaysOld);
        dateTime.setTime(0, 0, 0, 0);
        Date lastModifiedFromDate = dateTime.toDate();
        return ConcurUtils.formatAsUTCDate(lastModifiedFromDate);
    }

    protected String getNonBlankConcurParameterValue(String parameterName) {
        String value = concurBatchUtilityService.getConcurParameterValue(parameterName);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("Invalid blank value detected for Concur parameter " + parameterName);
        }
        return value;
    }

    protected int getConcurParameterValueAsPositiveInteger(String parameterName) {
        String value = concurBatchUtilityService.getConcurParameterValue(parameterName);
        if (!StringUtils.isNumeric(value)) {
            throw new IllegalStateException("Concur parameter " + parameterName
                    + " was either blank or was not a positive integer");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Unexpected error encountered when parsing Concur integer parameter " + parameterName, e);
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

}
