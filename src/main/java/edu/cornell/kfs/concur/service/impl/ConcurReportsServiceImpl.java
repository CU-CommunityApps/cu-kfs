package edu.cornell.kfs.concur.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.util.KRADConstants;
import org.springframework.beans.factory.DisposableBean;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ConcurReport;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.rest.xmlObjects.AllocationsDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ConcurEventNotificationListDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ExpenseEntryDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ExpenseReportDetailsDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.ItemizationEntryDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.TravelRequestDetailsDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.concur.service.ConcurReportsService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurReportsServiceImpl implements ConcurReportsService, DisposableBean {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurReportsServiceImpl.class);
    protected ConcurAccessTokenService concurAccessTokenService;
    protected ParameterService parameterService;
    private volatile Client client;
    private String concurExpenseWorkflowUpdateNamespace;
    private String concurRequestWorkflowUpdateNamespace;
    private String concurFailedRequestQueueEndpoint;
    private String concurFailedRequestDeleteNotificationEndpoint;

    @Override
    public void destroy() throws Exception {
        closeClientQuietly();
    }

    @Override
    public ConcurReport extractConcurReport(String reportURI) {
        LOG.info("Extract concur report with objectURI: " + reportURI);
        
        if (ConcurUtils.isExpenseReportURI(reportURI)) {
            return extractConcurReportFromExpenseDetails(reportURI);
        }
        
        if (ConcurUtils.isTravelRequestURI(reportURI)) {
            return extractConcurReportFromTravelRequestDetails(reportURI);
        }
        
        return null;
    }
    
    protected ConcurReport extractConcurReportFromExpenseDetails(String reportURI){
        ExpenseReportDetailsDTO expenseReportDetailsDTO = retrieveExpenseReportDetails(reportURI);
        List<ConcurAccountInfo> concurAccountInfos = extractAccountInfoFromExpenseReportDetails(expenseReportDetailsDTO);
        
        return new ConcurReport(expenseReportDetailsDTO.getReportId(), expenseReportDetailsDTO.getConcurStatusCode(), expenseReportDetailsDTO.getWorkflowActionURL(), concurAccountInfos);
    }
    
    protected ConcurReport extractConcurReportFromTravelRequestDetails(String reportURI){
        TravelRequestDetailsDTO travelRequestDetailsDTO = retrieveTravelRequestDetails(reportURI);
        List<ConcurAccountInfo> concurAccountInfos = extractAccountInfoFromTravelRequestDetails(travelRequestDetailsDTO);
        return new ConcurReport(travelRequestDetailsDTO.getRequestID(), travelRequestDetailsDTO.getConcurStatucCode(), travelRequestDetailsDTO.getWorkflowActionURL(), concurAccountInfos);

    }

    protected TravelRequestDetailsDTO retrieveTravelRequestDetails(String reportURI) {
        TravelRequestDetailsDTO travelRequestDetails = buildTravelRequestDetailsOutput(reportURI);
        return travelRequestDetails;
    }

    protected ExpenseReportDetailsDTO retrieveExpenseReportDetails(String reportURI) {
        ExpenseReportDetailsDTO reportDetails = buildReportDetailsOutput(reportURI);
        return reportDetails;
    }

    protected TravelRequestDetailsDTO buildTravelRequestDetailsOutput(String reportURI) {
        Response response = null;

        try {
            response = callReportDetailsEndpoint(reportURI, HttpMethod.GET);
            TravelRequestDetailsDTO reportDetails = response.readEntity(TravelRequestDetailsDTO.class);

            return reportDetails;
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }

    protected ExpenseReportDetailsDTO buildReportDetailsOutput(String reportURI) {
        Response response = null;

        try {
            response = callReportDetailsEndpoint(reportURI, HttpMethod.GET);
            ExpenseReportDetailsDTO reportDetails = response.readEntity(ExpenseReportDetailsDTO.class);

            return reportDetails;
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }

    protected Response callReportDetailsEndpoint(String reportURI, String httpMethod) {
        URI uri;
        try {
            uri = new URI(reportURI);
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occured while building the report details URI: ", e);
        }

        return getClient().target(uri)
                .request()
                .accept(MediaType.APPLICATION_XML)
                .header(ConcurConstants.AUTHORIZATION_PROPERTY,
                        ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + concurAccessTokenService.getAccessToken())
                .method(httpMethod);
    }

    protected List<ConcurAccountInfo> extractAccountInfoFromExpenseReportDetails(ExpenseReportDetailsDTO reportDetails) {
        List<ConcurAccountInfo> accountInfoList = new ArrayList<ConcurAccountInfo>();

        if (reportDetails.getEntries() != null) {
            for (ExpenseEntryDTO expenseEntry : reportDetails.getEntries()) {
                if (isPersonalOnCorporateCardExpense(expenseEntry)){
                    accountInfoList.add(extractAccountingInfoFromReportHeader(reportDetails));
                }
                else if (isNotPersonal(expenseEntry)){
                    if (expenseEntry.getItemizationsList() != null) {
                        for (ItemizationEntryDTO itemizationEntry : expenseEntry.getItemizationsList()) {
                            accountInfoList.addAll(extractConcurAccountInfoFromAllocations(itemizationEntry.getAllocationsList()));
                            accountInfoList.addAll(extractConcurAccountInfoFromAllocations(itemizationEntry.getAllocations()));
                        }
                    }
                    
                    accountInfoList.addAll(extractConcurAccountInfoFromAllocations(expenseEntry.getAllocations()));
                }
            }
        }

        return accountInfoList;
    }
    
    protected boolean isPersonalOnCorporateCardExpense(ExpenseEntryDTO expenseEntry){
        return KRADConstants.YES_INDICATOR_VALUE.equalsIgnoreCase(expenseEntry.getIsPersonal()) && KRADConstants.YES_INDICATOR_VALUE.equalsIgnoreCase(expenseEntry.getIsCreditCardCharge());
    }
    
    protected boolean isNotPersonal(ExpenseEntryDTO expenseEntry){
        return KRADConstants.NO_INDICATOR_VALUE.equalsIgnoreCase(expenseEntry.getIsPersonal());
    }
    
    protected ConcurAccountInfo extractAccountingInfoFromReportHeader(ExpenseReportDetailsDTO reportDetails){
        String chart = ConcurUtils.extractCodeFromCodeAndDescriptionValue(reportDetails.getOrgUnit1());
        String accountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(reportDetails.getOrgUnit2());
        String subAccountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(reportDetails.getOrgUnit3());
        String subObjectCode = ConcurUtils.extractCodeFromCodeAndDescriptionValue(reportDetails.getOrgUnit4());
        String projectCode = ConcurUtils.extractCodeFromCodeAndDescriptionValue(reportDetails.getOrgUnit5());
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(chart, accountNumber, subAccountNumber, null, subObjectCode, projectCode);
        concurAccountInfo.setForPersonalCorporateCardExpense(true);

        return concurAccountInfo;
    }
    
    protected List<ConcurAccountInfo> extractConcurAccountInfoFromAllocations(List<AllocationsDTO> allocations){
        List<ConcurAccountInfo> accountInfos = new ArrayList<ConcurAccountInfo>();
        
        if (allocations != null) {
            for (AllocationsDTO allocation : allocations) {
                ConcurAccountInfo concurAccountInfo = extractConcurAccountInfoFromAllocation(allocation);
                if(StringUtils.isNotBlank(concurAccountInfo.getChart()) && StringUtils.isNotBlank(concurAccountInfo.getAccountNumber()) && StringUtils.isNotBlank(concurAccountInfo.getObjectCode())){
                    accountInfos.add(concurAccountInfo);
                }
            }
        }
        return accountInfos;
    }
    
    protected ConcurAccountInfo extractConcurAccountInfoFromAllocation(AllocationsDTO allocation){
        String chart = ConcurUtils.extractCodeFromCodeAndDescriptionValue(allocation.getCustom1());
        String accountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(allocation.getCustom2());
        String subAccountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(allocation.getCustom3());
        String objectCode = allocation.getAccountCode1();
        String subObjectCode = allocation.getCustom4();
        String projectCode = ConcurUtils.extractCodeFromCodeAndDescriptionValue(allocation.getCustom5());
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(chart, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode);
        
        return concurAccountInfo;
    }

    private List<ConcurAccountInfo> extractAccountInfoFromTravelRequestDetails(TravelRequestDetailsDTO travelRequestDetails) {
        List<ConcurAccountInfo> accountInfoList = new ArrayList<ConcurAccountInfo>();
        String chart = ConcurUtils.extractCodeFromCodeAndDescriptionValue(travelRequestDetails.getCustom1());
        String accountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(travelRequestDetails.getCustom2());
        String subAccountNumber = ConcurUtils.extractCodeFromCodeAndDescriptionValue(travelRequestDetails.getCustom3());
        String objectCode = parameterService.getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE);
        String subObjectCode = travelRequestDetails.getCustom4();
        String projectCode = ConcurUtils.extractCodeFromCodeAndDescriptionValue(travelRequestDetails.getCustom5());
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(chart, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode);
        accountInfoList.add(concurAccountInfo);
        return accountInfoList;
    }

    @Override
    public void updateExpenseReportStatusInConcur(String workflowURI, ValidationResult validationResult) {
        LOG.info("updateExpenseReportStatusInConcur()");
        
        if(validationResult.isValid()){
            buildUpdateReportOutput(workflowURI, ConcurConstants.APPROVE_ACTION, ConcurConstants.APPROVE_COMMENT);  
        }
        else{
            buildUpdateReportOutput(workflowURI, ConcurConstants.SEND_BACK_TO_EMPLOYEE_ACTION, addConcurMessageHeaderAndTruncate(validationResult.getErrorMessagesAsOneFormattedString(), ConcurConstants.VALIDATION_RESULT_MESSAGE_MAX_LENGTH));  
        }          
    }
    
    protected void buildUpdateReportOutput(String workflowURI, String action, String comment) {
        LOG.info("buildUpdateReportOutput()");
        Response response = null;

        try {
            String workflowUpdateXml = buildWorkflowUpdateXML(workflowURI, action, comment);
            response = getClient().target(workflowURI)
                    .request()
                    .accept(MediaType.APPLICATION_XML)
                    .header(ConcurConstants.AUTHORIZATION_PROPERTY,
                            ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + concurAccessTokenService.getAccessToken())
                    .post(Entity.xml(workflowUpdateXml));

            response.bufferEntity();
            String result = response.readEntity(String.class);
            LOG.info("Update workflow response: " + result);
        } finally {
            CURestClientUtils.closeQuietly(response);
        }

    }

    private String buildWorkflowUpdateXML(String workflowURI, String action, String comment) {
        String xml = "<WorkflowAction xmlns=\"" + getNamespace(workflowURI)
                + "\"><Action>" + action + "</Action><Comment>" + comment
                + "</Comment></WorkflowAction>";
        
        LOG.info("buildWorkflowUpdateXML(): Update workflow xml: " + xml);
        return xml;
    }
    
    @Override
    public boolean deleteFailedEventQueueItemInConcur(String noticationId) {
        LOG.info("deleteFailedEventQueueItem(), noticationId: " + noticationId);
        String deleteItemURL = getConcurFailedRequestDeleteNotificationEndpoint() + noticationId;
        LOG.info("deleteFailedEventQueueItem(), the delete item URL: " + deleteItemURL);
        Response response = null;
        
        try {
            response = callReportDetailsEndpoint(deleteItemURL, HttpMethod.DELETE);
            int statusCode = response.getStatus();
            String statusResponsePhrase = response.getStatusInfo().getReasonPhrase();
            LOG.info("deleteFailedEventQueueItem(), the resonse status code was " + statusCode + " and the response phrase was " + statusResponsePhrase);
            return statusCode == Response.Status.OK.getStatusCode();
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }
    
    @Override
    public ConcurEventNotificationListDTO retrieveFailedEventQueueNotificationsFromConcur() {
        LOG.info("retrieveFailedEventQueueNotificationsFromConcur, the failed event queue endpoint: " + getConcurFailedRequestQueueEndpoint());
        Response response = null;
        
        try {
            response = callReportDetailsEndpoint(getConcurFailedRequestQueueEndpoint(), HttpMethod.GET);
            ConcurEventNotificationListDTO reportDetails = response.readEntity(ConcurEventNotificationListDTO.class);
            return reportDetails;
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }

    protected Client getClient() {
        // Use double-checked locking to lazy-load the Client object, similar to related locking in Rice.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
                if (jerseyClient == null) {
                    ClientConfig clientConfig = new ClientConfig();
                    jerseyClient = ClientBuilder.newClient(clientConfig);
                    client = jerseyClient;
                }
            }
        }
        return jerseyClient;
    }

    protected void closeClientQuietly() {
        // Use double-checked locking to retrieve the Client object, similar to related locking in Rice.
        // See effective java 2nd ed. pg. 71
        Client jerseyClient = client;
        if (jerseyClient == null) {
            synchronized (this) {
                jerseyClient = client;
            }
        }
        
        CURestClientUtils.closeQuietly(jerseyClient);
    }

    private String addConcurMessageHeaderAndTruncate(String message, int maxLength) {
        String errorMessagesString = addMessageHeader(message);
        errorMessagesString = truncateMessageLength(errorMessagesString, maxLength);
        return errorMessagesString;
    }

    private String addMessageHeader(String message) {
        if (message.length() > 0) {
            message = ConcurConstants.ERROR_MESSAGE_HEADER + message;
        }
        return message;
    }

    private String truncateMessageLength(String message, int maxLength) {
        if (message.length() > maxLength) {
            message = message.substring(0, maxLength);
        }
        return message;
    }
    
    private String getNamespace(String workflowURI){
        return ConcurUtils.isExpenseReportURI(workflowURI)? concurExpenseWorkflowUpdateNamespace: concurRequestWorkflowUpdateNamespace;
    }
    
    public ConcurAccessTokenService getConcurAccessTokenService() {
        return concurAccessTokenService;
    }

    public void setConcurAccessTokenService(ConcurAccessTokenService concurAccessTokenService) {
        this.concurAccessTokenService = concurAccessTokenService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public String getConcurExpenseWorkflowUpdateNamespace() {
        return concurExpenseWorkflowUpdateNamespace;
    }

    public void setConcurExpenseWorkflowUpdateNamespace(
            String concurExpenseWorkflowUpdateNamespace) {
        this.concurExpenseWorkflowUpdateNamespace = concurExpenseWorkflowUpdateNamespace;
    }

    public String getConcurRequestWorkflowUpdateNamespace() {
        return concurRequestWorkflowUpdateNamespace;
    }

    public void setConcurRequestWorkflowUpdateNamespace(
            String concurRequestWorkflowUpdateNamespace) {
        this.concurRequestWorkflowUpdateNamespace = concurRequestWorkflowUpdateNamespace;
    }

    public String getConcurFailedRequestQueueEndpoint() {
        return concurFailedRequestQueueEndpoint;
    }

    public void setConcurFailedRequestQueueEndpoint(String concurFailedRequestQueueEndpoint) {
        this.concurFailedRequestQueueEndpoint = concurFailedRequestQueueEndpoint;
    }

    public String getConcurFailedRequestDeleteNotificationEndpoint() {
        return concurFailedRequestDeleteNotificationEndpoint;
    }

    public void setConcurFailedRequestDeleteNotificationEndpoint(String concurFailedRequestDeleteNotificationEndpoint) {
        this.concurFailedRequestDeleteNotificationEndpoint = concurFailedRequestDeleteNotificationEndpoint;
    }

}
