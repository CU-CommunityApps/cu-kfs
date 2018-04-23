package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksTokenRefreshConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsRootDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class PaymentWorksWebServiceCallsServiceImpl implements PaymentWorksWebServiceCallsService, Serializable {
    private static final long serialVersionUID = -4282596886353845280L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksWebServiceCallsServiceImpl.class);

    private static final String REFRESH_TOKEN_URL_FORMAT = "%susers/%s/refresh_auth_token/";
    private static final String EMPTY_JSON_WRAPPER = "{}";

    private String paymentWorksUrl;
    protected PaymentWorksDtoToPaymentWorksVendorConversionService paymentWorksDtoToPaymentWorksVendorConversionService;
    protected WebServiceCredentialService webServiceCredentialService;
    
    @Override
    public List<String> obtainPmwIdentifiersForPendingNewVendorRequests() {
        LOG.info("obtainPmwIdentifiersForPendingNewVendorRequests: Processing started.");
        List<String> pmwNewVendorIdentifiers = new ArrayList<String>();
        List<PaymentWorksNewVendorRequestDTO> paymentWorksNewVendorRequestDTOs = retrieveAllPaymentWorksPendingNewVendorRequests();
        pmwNewVendorIdentifiers = getAllPaymentWorksIdentifiersFromDTO(paymentWorksNewVendorRequestDTOs);
        LOG.info("obtainPmwIdentifiersForPendingNewVendorRequests: Processing completed.");
        return pmwNewVendorIdentifiers;
    }
    
    private List<PaymentWorksNewVendorRequestDTO> retrieveAllPaymentWorksPendingNewVendorRequests() {
        Client clientForNewVendorRequestsRootResults = null;
        Response responseForNewVendorRequestsRootResults = null;
        List<PaymentWorksNewVendorRequestDTO> pmwNewVendorIdentifiers = new ArrayList<PaymentWorksNewVendorRequestDTO>();
        
        try{
            clientForNewVendorRequestsRootResults = constructClientToUseForPagedResponses();
            responseForNewVendorRequestsRootResults = constructXmlResponseToUseForPagedData(clientForNewVendorRequestsRootResults, buildPaymentWorksPendingNewVendorRequestsURI());
            PaymentWorksNewVendorRequestsRootDTO newVendorsRoot = responseForNewVendorRequestsRootResults.readEntity(PaymentWorksNewVendorRequestsRootDTO.class);
            LOG.info("retrieveAllPaymentWorksPendingNewVendorRequests: newVendorsRoot.getCount()=" + newVendorsRoot.getCount());
            
            while (thereAreMorePmwVendorIdsToRetrieve(newVendorsRoot)) {
                pmwNewVendorIdentifiers.addAll(newVendorsRoot.getPmwNewVendorRequestsDTO().getPmwNewVendorRequests()); 
                closeResponseJustObtained(responseForNewVendorRequestsRootResults);
                
                if (additionalPagesOfPmwVendorIdsExist(newVendorsRoot)) {
                    responseForNewVendorRequestsRootResults = constructXmlResponseToUseForPagedData(clientForNewVendorRequestsRootResults, buildURI(newVendorsRoot.getNext()));
                    newVendorsRoot = responseForNewVendorRequestsRootResults.readEntity(PaymentWorksNewVendorRequestsRootDTO.class);
                } else {
                    newVendorsRoot = null;
                }
            }
            
            for (int i=0; i < pmwNewVendorIdentifiers.size(); i++) {
                LOG.info("retrieveAllPaymentWorksPendingNewVendorRequests: PMW-Vendor-id=" + pmwNewVendorIdentifiers.get(i).getId());
            }
            
            return pmwNewVendorIdentifiers;
            
        } finally {
            CURestClientUtils.closeQuietly(clientForNewVendorRequestsRootResults);
            CURestClientUtils.closeQuietly(responseForNewVendorRequestsRootResults);
        }
    }
    
    private boolean thereAreMorePmwVendorIdsToRetrieve(PaymentWorksNewVendorRequestsRootDTO newVendorsRoot) {
        return ((ObjectUtils.isNotNull(newVendorsRoot)) && (newVendorsRoot.getCount() > 0));
    }
    
    private boolean additionalPagesOfPmwVendorIdsExist(PaymentWorksNewVendorRequestsRootDTO newVendorsRoot) {
        return (StringUtils.isNotEmpty(newVendorsRoot.getNext()));
    }
    
    private void closeResponseJustObtained(Response responseForNewVendorRequestsRootResults) {
        CURestClientUtils.closeQuietly(responseForNewVendorRequestsRootResults);
    }
    
    private URI buildPaymentWorksPendingNewVendorRequestsURI() {
        String URL = (new StringBuilder(getPaymentWorksUrl())
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUESTS)
                .append(PaymentWorksWebServiceConstants.QUESTION_MARK)
                .append(PaymentWorksWebServiceConstants.STATUS)
                .append(CUKFSConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PENDING.code)).toString();
        LOG.info("buildPaymentWorksPendingNewVendorRequestsURI: URL =" + URL);
        return buildURI(URL);
    }
    
    @Override
    public PaymentWorksVendor obtainPmwNewVendorRequestDetailForPmwIdentifier(String pmwNewVendorRequestId, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        PaymentWorksNewVendorRequestDetailDTO pmwDetailForSpecificVendorDTO = retrieveAllPaymentWorksDetailsForRequestedVendor(buildPaymentWorksPendingNewVendorRequestDetailURI(pmwNewVendorRequestId));
        PaymentWorksVendor pmwDetailForSpecificVendor = getPaymentWorksDtoToPaymentWorksVendorConversionService().createPaymentWorksVendorFromPaymentWorksNewVendorRequestDetailDTO(pmwDetailForSpecificVendorDTO, reportData);
        return pmwDetailForSpecificVendor;
    }
    
    private PaymentWorksNewVendorRequestDetailDTO retrieveAllPaymentWorksDetailsForRequestedVendor(URI newVendorRequestDetailURI) {
        LOG.info("retrieveAllPaymentWorksDetailsForRequestedVendor: newVendorRequestDetailURI=" + newVendorRequestDetailURI);
        Response responseForNewVendorRequestSpecificDetail = null;
        PaymentWorksNewVendorRequestDetailDTO pmwRequestedNewVendorDetail = null;
        try{
            responseForNewVendorRequestSpecificDetail = buildXmlOutput(newVendorRequestDetailURI);
            pmwRequestedNewVendorDetail = responseForNewVendorRequestSpecificDetail.readEntity(PaymentWorksNewVendorRequestDetailDTO.class);
            LOG.info("retrieveAllPaymentWorksDetailsForRequestedVendor: legalName=" + pmwRequestedNewVendorDetail.getRequesting_company().getLegal_name());
            return pmwRequestedNewVendorDetail;
        } finally {
            CURestClientUtils.closeQuietly(responseForNewVendorRequestSpecificDetail);
        }
    }
    
    private URI buildPaymentWorksPendingNewVendorRequestDetailURI(String pmwNewVendorRequestId) {
        String URL = (new StringBuilder(getPaymentWorksUrl())
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUEST_DETAILS_PREFIX)
                .append(PaymentWorksWebServiceConstants.FORWARD_SLASH)
                .append(pmwNewVendorRequestId)
                .append(PaymentWorksWebServiceConstants.FORWARD_SLASH)
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUEST_DETAILS_SUFFIX)).toString();
        return buildURI(URL);
    }
 
    @Override
    public void sendApprovedStatusToPaymentWorksForNewVendor(String approvedVendorId) {
        LOG.info("sendApprovedStatusToPaymentWorksForNewVendor: Processing started.");
        String jsonString = buildPaymentWorksNewVendorUpdateStatusJson(approvedVendorId, PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.getCodeAsString());
        updateNewVendorStatusInPaymentWorks(jsonString);
        LOG.info("sendApprovedStatusToPaymentWorksForNewVendor: Processing complete.");
    }
    
    @Override
    public void sendRejectedStatusToPaymentWorksForNewVendor(String rejectedVendorId) {
        LOG.info("sendRejectedStatusToPaymentWorksForNewVendor: Processing started.");
        String jsonString = buildPaymentWorksNewVendorUpdateStatusJson(rejectedVendorId, PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.REJECTED.getCodeAsString());
        updateNewVendorStatusInPaymentWorks(jsonString);
        LOG.info("sendRejectedStatusToPaymentWorksForNewVendor: Processing complete.");
    }
    
    private void updateNewVendorStatusInPaymentWorks(String jsonString) {
        URI statusUpdateURI = buildPaymentWorksNewVendorUpdateStatusURI();
        Response updateResponse = null;
        try{
            updateResponse = buildJsonResponse(statusUpdateURI, jsonString);
            String result = updateResponse.readEntity(String.class);
            LOG.info("updateNewVendorStatusInPaymentWorks: Response was : " + result);
        } finally {
            CURestClientUtils.closeQuietly(updateResponse);
        }
    }
    
    private String buildPaymentWorksNewVendorUpdateStatusJson(String vendorId, String statusCode) {
        String jsonString = (new StringBuilder("[{\"id\":").append(vendorId).append(",\"request_status\":").append(statusCode).append("}]")).toString();
        LOG.info("buildNewVendorUpdateStatusJson: jsonString is " + jsonString);
        return jsonString;
    }
    
    private URI buildPaymentWorksNewVendorUpdateStatusURI() {
        String URL = (new StringBuilder(getPaymentWorksUrl())
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUEST_UPDATE_STATUS)).toString();
        return buildURI(URL);
    }
    
    private Response buildJsonResponse(URI uri, String jsonString) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            client = ClientBuilder.newClient(clientConfig);
            Invocation request = buildJsonClientRequest(client, uri, jsonString);
            response = request.invoke();
            response.bufferEntity();
            return response;
        } finally {
            CURestClientUtils.closeQuietly(client);
        }
    }
    
    private Invocation buildJsonClientRequest(Client client, URI uri, String jsonString) {
        return client.target(uri)
                     .request()
                     .accept(MediaType.APPLICATION_JSON)
                     .header(PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY, 
                             PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER + getPaymentWorksAuthorizationToken())
                     .buildPut(Entity.json(jsonString));
    }
    
    private Response buildXmlOutput(URI uri) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            client = ClientBuilder.newClient(clientConfig);
            Invocation request = buildXmlClientRequest(client, uri);
            response = request.invoke();
            response.bufferEntity();
            return response;
        } finally {
            CURestClientUtils.closeQuietly(client);
        }
    }
    
    private Client constructClientToUseForPagedResponses() {
        Client client = null;
        ClientConfig clientConfig = new ClientConfig();
        client = ClientBuilder.newClient(clientConfig);
        return client;
    }
    
    private  Response constructXmlResponseToUseForPagedData(Client client, URI uri) {
        Response response = null;
        Invocation request = buildXmlClientRequest(client, uri);
        response = request.invoke();
        response.bufferEntity();
        return response;
    }
    
    private URI buildURI(String URL) {
        try {
            return new URI(URL);
        } catch (URISyntaxException e) {
            LOG.error("buildURI(): URL: " + URL );
            throw new RuntimeException(e);
        }
    }
    
    private Invocation buildXmlClientRequest(Client client, URI uri) {
        return client.target(uri)
                     .request()
                     .accept(MediaType.APPLICATION_XML)
                     .header(PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY, 
                             PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER + getPaymentWorksAuthorizationToken())
                     .buildGet();
    }
    
    private List<String> getAllPaymentWorksIdentifiersFromDTO(List<PaymentWorksNewVendorRequestDTO> paymentWorksNewVendorRequestDTOs) {
        List<String> allPmwVendorIdentifiers = new ArrayList<String>();
        if (!paymentWorksNewVendorRequestDTOs.isEmpty()) {
            paymentWorksNewVendorRequestDTOs.stream()
                                            .forEach(paymentWorksNewVendorRequestDTO -> {
                                                allPmwVendorIdentifiers.add(paymentWorksNewVendorRequestDTO.getId());
                                            });
        }
        return allPmwVendorIdentifiers;
    }

    public void refreshPaymentWorksAuthorizationToken() {
        LOG.info("refreshPaymentWorksAuthorizationToken(): Processing started");
        Response refreshTokenResponse = null;
        
        try {
            URI refreshTokenURI = buildRefreshAuthorizationTokenURI();
            refreshTokenResponse = buildJsonResponse(refreshTokenURI, EMPTY_JSON_WRAPPER);
            String jsonResponse = refreshTokenResponse.readEntity(String.class);
            String newToken = getPaymentWorksAuthorizationTokenFromResponse(jsonResponse);
            webServiceCredentialService.updateWebServiceCredentialValue(
                    PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE,
                    PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN, newToken);
            LOG.info("refreshPaymentWorksAuthorizationToken(): Token was successfully refreshed");
        } finally {
            CURestClientUtils.closeQuietly(refreshTokenResponse);
        }
    }

    private URI buildRefreshAuthorizationTokenURI() {
        String userId = getPaymentWorksUserId();
        if (!StringUtils.isAlphanumeric(userId)) {
            LOG.error("buildRefreshAuthorizationTokenURI(): PaymentWorks User ID was not alphanumeric");
            throw new IllegalStateException("Malformed PaymentWorks User ID");
        }
        String url = String.format(REFRESH_TOKEN_URL_FORMAT, getPaymentWorksUrl(), userId);
        return buildURI(url);
    }

    private String getPaymentWorksAuthorizationTokenFromResponse(String jsonResponse) {
        if (StringUtils.isBlank(jsonResponse)) {
            throw new RuntimeException("Token refresh failed: No response content was received from PaymentWorks");
        }
        
        JsonNode rootNode = readJsonTree(jsonResponse);
        checkForSuccessfulTokenRefreshStatus(rootNode);
        
        JsonNode tokenNode = rootNode.findValue(PaymentWorksTokenRefreshConstants.AUTH_TOKEN_FIELD);
        if (ObjectUtils.isNull(tokenNode)) {
            LOG.error("getPaymentWorksAuthorizationTokenFromResponse(): Did not receive new token from PaymentWorks");
            throw new RuntimeException("Token refresh failed: PaymentWorks did not send a new token");
        }
        
        String newToken = tokenNode.textValue();
        if (StringUtils.isBlank(newToken)) {
            LOG.error("getPaymentWorksAuthorizationTokenFromResponse(): Received a blank token from PaymentWorks");
            throw new RuntimeException("Token refresh failed: PaymentWorks sent a blank token");
        }
        
        return newToken;
    }

    private JsonNode readJsonTree(String jsonText) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonText);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void checkForSuccessfulTokenRefreshStatus(JsonNode rootNode) {
        JsonNode detailNode = rootNode.findValue(PaymentWorksTokenRefreshConstants.DETAIL_FIELD);
        if (ObjectUtils.isNotNull(detailNode)) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Token refresh failed. Detail message: " + detailNode.textValue());
            handleDetailMessageFromTokenRefreshFailure(detailNode.textValue());
            throw new RuntimeException("Token refresh failed: Received failure response from PaymentWorks");
        }
        
        JsonNode statusNode = rootNode.findValue(PaymentWorksTokenRefreshConstants.STATUS_FIELD);
        if (ObjectUtils.isNull(statusNode)) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Did not receive a refresh status from PaymentWorks");
            throw new RuntimeException("Token refresh failed: Did not receive a refresh status from PaymentWorks");
        } else if (!StringUtils.equalsIgnoreCase(PaymentWorksTokenRefreshConstants.STATUS_OK, statusNode.textValue())) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Unexpected status from PaymentWorks response: " + statusNode.textValue());
            throw new RuntimeException("Token refresh failed: Received an unexpected refresh status from PaymentWorks");
        }
    }

    protected void handleDetailMessageFromTokenRefreshFailure(String detailMessage) {
        // This is just a hook for unit testing convenience.
    }

    public String getPaymentWorksUserId() {
        return getWebServiceCredentialValue(PaymentWorksCredentialKeys.PAYMENTWORKS_USER_ID);
    }

    public String getPaymentWorksAuthorizationToken() {
        return getWebServiceCredentialValue(PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN);
    }

    protected String getWebServiceCredentialValue(String credentialKey) {
        return webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, credentialKey);
    }

    public String getPaymentWorksUrl() {
        return paymentWorksUrl;
    }

    public void setPaymentWorksUrl(String paymentWorksUrl) {
        this.paymentWorksUrl = paymentWorksUrl;
    }

    public PaymentWorksDtoToPaymentWorksVendorConversionService getPaymentWorksDtoToPaymentWorksVendorConversionService() {
        return paymentWorksDtoToPaymentWorksVendorConversionService;
    }

    public void setPaymentWorksDtoToPaymentWorksVendorConversionService(
            PaymentWorksDtoToPaymentWorksVendorConversionService paymentWorksDtoToPaymentWorksVendorConversionService) {
        this.paymentWorksDtoToPaymentWorksVendorConversionService = paymentWorksDtoToPaymentWorksVendorConversionService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }
}
