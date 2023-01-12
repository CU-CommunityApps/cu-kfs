package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.http.HttpMethod;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.ConcurWorkflowActions;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.ConcurWebRequest;
import edu.cornell.kfs.concur.batch.ConcurWebRequestBuilder;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.ConcurExpenseV3Service;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDetailDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurV4WorkflowDTO;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurExpenseV3ServiceImpl implements ConcurExpenseV3Service {
    private static final Logger LOG = LogManager.getLogger();

    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;

    @Override
    public void processExpenseReports(String accessToken,
            List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        ConcurExpenseV3ListingDTO expenseList = getConcurStartingExpenseListing(accessToken);
        processExpenseListing(accessToken, expenseList, processingResults);
    }

    protected ConcurExpenseV3ListingDTO getConcurStartingExpenseListing(String accessToken) {
        String logMessageDetail = configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_INTIAL_EXPENSE_LISTING);
        ConcurExpenseV3ListingDTO expenseList = concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                findDefaultExpenseListingEndPoint(), ConcurExpenseV3ListingDTO.class, logMessageDetail);
        return expenseList;
    }

    protected String findDefaultExpenseListingEndPoint() {
        String geolocation = findGeolocationUrl();
        String baseUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V3_LISTING_ENDPOINT);
        baseUrl = baseUrl + !isProduction();
        return geolocation + baseUrl;
    }

    protected void processExpenseListing(String accessToken, ConcurExpenseV3ListingDTO expenseList,
            List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        for (ConcurExpenseV3ListItemDTO partialExpenseReportFromListing : expenseList.getItems()) {
            ConcurExpenseV3ListItemDTO fullExpenseReport = getConcurExpenseReport(accessToken,
                    partialExpenseReportFromListing.getId(), partialExpenseReportFromListing.getOwnerLoginID());
            
            List<ConcurExpenseAllocationV3ListItemDTO> allocationItems = getConcurExpenseAllocationV3ListItemsForReport(accessToken, fullExpenseReport.getId());
            
            String reportNumber = fullExpenseReport.getId();
            String reportName = fullExpenseReport.getName();
            String reportStatus = fullExpenseReport.getApprovalStatusName();
            String travelerName = fullExpenseReport.getOwnerName();
            String travelerEmail = fullExpenseReport.getOwnerLoginID();
            
            validateExpenseAllocations(accessToken, processingResults, allocationItems, reportNumber, reportName, reportStatus, travelerName, travelerEmail);
        }
        if (StringUtils.isNotBlank(expenseList.getNextPage())) {
            String logMessageDetail = configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_LISTING_NEXT_PAGE);
            ConcurExpenseV3ListingDTO nextConcurExpenseV3ListingDTO = concurEventNotificationV2WebserviceService
                    .buildConcurDTOFromEndpoint(accessToken, expenseList.getNextPage(), ConcurExpenseV3ListingDTO.class, logMessageDetail);
            processExpenseListing(accessToken, nextConcurExpenseV3ListingDTO, processingResults);
        } 
    }
    
    protected ConcurExpenseV3ListItemDTO getConcurExpenseReport(String accessToken, String reportId, String userName) {
        String expenseReportEndpoint = findBaseExpenseReportEndPoint() + reportId + ConcurConstants.QUESTION_MARK_USER_EQUALS + userName;
        String logMessageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_REPORT), reportId);
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                expenseReportEndpoint, ConcurExpenseV3ListItemDTO.class, logMessageDetail);
    }
    
    protected List<ConcurExpenseAllocationV3ListItemDTO> getConcurExpenseAllocationV3ListItemsForReport(String accessToken, String reportId) {
        String baseAllocationEndpoint = findBaseAllocationEndPoint() + reportId;
        
        String logMessageDetail = MessageFormat.format(
                configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_ALLOCATION_LISTING), reportId);
        ConcurExpenseAllocationV3ListingDTO allocationList = getConcurExpenseAllocationV3ListingDTO(accessToken, baseAllocationEndpoint, logMessageDetail);
        
        List<ConcurExpenseAllocationV3ListItemDTO> allocationItems = allocationList.getItems();
        
        while(StringUtils.isNotBlank(allocationList.getNextPage())) {
            String nextPageLogMessageDetail = MessageFormat.format(
                    configurationService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_ALLOCATION_LISTING_NEXT_PAGE), reportId);
            allocationList = getConcurExpenseAllocationV3ListingDTO(accessToken, allocationList.getNextPage(), nextPageLogMessageDetail);
            allocationItems.addAll(allocationList.getItems());
        }
        
        return allocationItems;
    }
    
    protected ConcurExpenseAllocationV3ListingDTO getConcurExpenseAllocationV3ListingDTO(String accessToken, String allocationEndpoint, String logMessageDetail) {
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                allocationEndpoint, ConcurExpenseAllocationV3ListingDTO.class, logMessageDetail);
    }


    protected void validateExpenseAllocations(String accessToken, List<ConcurEventNotificationProcessingResultsDTO> processingResults,
            List<ConcurExpenseAllocationV3ListItemDTO> allocationItems, String reportNumber, String reportName, String reportStatus, String travelerName,
            String travelerEmail) {
        boolean reportValid = true;
        ArrayList<String> validationMessages = new ArrayList<>();
        ConcurEventNotificationVersion2ProcessingResults reportResults = ConcurEventNotificationVersion2ProcessingResults.validAccounts;
        try {
            for (ConcurExpenseAllocationV3ListItemDTO allocationItem : allocationItems) {
                ConcurAccountInfo info = buildConcurAccountInfo(allocationItem);
                LOG.info("validateExpenseAllocations, for report " + reportNumber + " account info: " + info.toString());
                ValidationResult results = concurAccountValidationService.validateConcurAccountInfo(info);
                reportValid &= results.isValid();
                validationMessages.addAll(results.getMessages());
            }
            if (!reportValid) {
                reportResults = ConcurEventNotificationVersion2ProcessingResults.invalidAccounts;
            }
        } catch (Exception e) {
            reportValid = false;
            reportResults = ConcurEventNotificationVersion2ProcessingResults.processingError;
            validationMessages.add("Encountered an error validating this report");
            LOG.error("validateExpenseAllocations, had an error validating report " + reportNumber, e);
        }
        
        ConcurEventNotificationProcessingResultsDTO resultsDTO = new ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType.ExpenseReport,
                reportResults, reportNumber, reportName, reportStatus, travelerName, travelerEmail, validationMessages);
        processingResults.add(resultsDTO);
        updateStatusInConcur(accessToken, reportNumber, reportValid, resultsDTO);
        
    }
    
    protected void updateStatusInConcur(String accessToken, String reportId, boolean reportValid, ConcurEventNotificationProcessingResultsDTO resultsDTO) {
        LOG.info("updateStatusInConcur, for report id " + reportId + " the over all validation status will be set to " + reportValid);
        if (!shouldUpdateStatusInConcur()) {
            LOG.info("updateStatusInConcur, Concur workflow actions are currently disabled in this KFS environment");
            return;
        }

        String workflowAction = reportValid ? ConcurWorkflowActions.APPROVE : ConcurWorkflowActions.SEND_BACK;
        String logMessageDetail = buildLogMessageDetailForExpenseWorkflowAction(workflowAction, reportId);
        ConcurWebRequest<Void> webRequest = buildWebRequestForExpenseWorkflowAction(
                workflowAction, reportId, resultsDTO);

        concurEventNotificationV2WebserviceService.callConcurEndpoint(
                accessToken, webRequest, logMessageDetail);
    }
    
    protected boolean shouldUpdateStatusInConcur() {
        return isProduction() || shouldUpdateStatusInConcurForNonProductionEnvironment();
    }
    
    protected boolean shouldUpdateStatusInConcurForNonProductionEnvironment() {
        String concurTestWorkflowIndicator = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_TEST_WORKFLOW_ACTIONS_ENABLED_IND);
        return StringUtils.equalsIgnoreCase(concurTestWorkflowIndicator, KFSConstants.ACTIVE_INDICATOR);
    }
    
    protected String buildLogMessageDetailForExpenseWorkflowAction(String workflowAction, String reportId) {
        String expenseV4WorkflowMessageFormat = configurationService.getPropertyValueAsString(
                ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV4_EXPENSE_REPORT_WORKFLOW);
        return MessageFormat.format(expenseV4WorkflowMessageFormat, workflowAction, reportId);
    }
    
    protected ConcurWebRequest<Void> buildWebRequestForExpenseWorkflowAction(String workflowAction, String reportId,
            ConcurEventNotificationProcessingResultsDTO resultsDTO) {
        String workflowComment = StringUtils.equals(workflowAction, ConcurWorkflowActions.APPROVE)
                ? ConcurConstants.APPROVE_COMMENT
                : ConcurUtils.buildValidationErrorMessageForWorkflowAction(resultsDTO);
        ConcurV4WorkflowDTO workflowDTO = new ConcurV4WorkflowDTO(workflowComment);
        String workflowActionUrl = buildFullUrlForExpenseWorkflowAction(reportId, workflowAction);
        
        return ConcurWebRequestBuilder.forRequestExpectingEmptyResponse()
                .withUrl(workflowActionUrl)
                .withHttpMethod(HttpMethod.PATCH)
                .withJsonBody(workflowDTO)
                .build();
    }
    
    protected String buildFullUrlForExpenseWorkflowAction(String reportId, String workflowAction) {
        String baseWorkflowUrl = findBaseWorkflowEndPoint();
        return StringUtils.joinWith(CUKFSConstants.SLASH,
                baseWorkflowUrl, UrlFactory.encode(reportId), UrlFactory.encode(workflowAction));
    }
    
    protected ConcurAccountInfo buildConcurAccountInfo(ConcurExpenseAllocationV3ListItemDTO allocationItem) {
        String chart = getValueFromAllocationDetailDTO(allocationItem.getChart());
        String account = getValueFromAllocationDetailDTO(allocationItem.getAccount());
        String subAccount = getValueFromAllocationDetailDTO(allocationItem.getSubAccount());
        String objectCode = allocationItem.getObjectCode();
        String subObject = getValueFromAllocationDetailDTO(allocationItem.getSubObject());
        String project = getValueFromAllocationDetailDTO(allocationItem.getProjectCode());
        String orgRefId = getValueFromAllocationDetailDTO(allocationItem.getOrgRefId());
        ConcurAccountInfo info = new ConcurAccountInfo(chart, account, subAccount, objectCode, subObject, project, orgRefId);
        return info;
    }
    
    protected String getValueFromAllocationDetailDTO(ConcurExpenseAllocationV3ListItemDetailDTO dto) {
        return ObjectUtils.isNull(dto) ? StringUtils.EMPTY : dto.getCodeOrValue();
    }

    protected String findBaseExpenseReportEndPoint() {
        String geolocation = findGeolocationUrl();
        String reportUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V3_REPORT_ENDPOINT);
        return geolocation + reportUrl;
    }
    
    protected String findBaseAllocationEndPoint() {
        String geolocation = findGeolocationUrl();
        String allocationUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V3_ALLOCATION_ENDPOINT);
        return geolocation + allocationUrl;
    }

    protected String findBaseWorkflowEndPoint() {
        String geolocation = findGeolocationUrl();
        String workflowUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V4_WORKFLOW_ENDPOINT);
        return geolocation + workflowUrl;
    }

    protected String findGeolocationUrl() {
        return concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_GEOLOCATION_URL);
    }

    protected boolean isProduction() {
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
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
