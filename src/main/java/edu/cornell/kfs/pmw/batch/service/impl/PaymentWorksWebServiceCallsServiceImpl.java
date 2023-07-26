package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCommonJsonConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksSupplierUploadConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksTokenRefreshConstants;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsRootDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.CURestClientUtils;
import edu.cornell.kfs.sys.web.CuMultiPartWriter;

public class PaymentWorksWebServiceCallsServiceImpl extends DisposableClientServiceImplBase implements PaymentWorksWebServiceCallsService, Serializable {
    private static final long serialVersionUID = -4282596886353845280L;
    private static final Logger LOG = LogManager.getLogger(PaymentWorksWebServiceCallsServiceImpl.class);

    protected PaymentWorksDtoToPaymentWorksVendorConversionService paymentWorksDtoToPaymentWorksVendorConversionService;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public List<String> obtainPmwIdentifiersForApprovedNewVendorRequests() {
        LOG.info("obtainPmwIdentifiersForApprovedNewVendorRequests: Processing started.");
        List<String> pmwNewVendorIdentifiers = new ArrayList<String>();
        List<PaymentWorksNewVendorRequestDTO> paymentWorksNewVendorRequestDTOs = retrieveAllPaymentWorksApprovedNewVendorRequests();
        pmwNewVendorIdentifiers = getAllPaymentWorksIdentifiersFromDTO(paymentWorksNewVendorRequestDTOs);
        LOG.info("obtainPmwIdentifiersForApprovedNewVendorRequests: Processing completed.");
        return pmwNewVendorIdentifiers;
    }

    @Override
    protected Client getClient() {
        return super.getClient(CuMultiPartWriter.class);
    }

    private List<PaymentWorksNewVendorRequestDTO> retrieveAllPaymentWorksApprovedNewVendorRequests() {
        Response responseForNewVendorRequestsRootResults = null;
        List<PaymentWorksNewVendorRequestDTO> pmwNewVendorIdentifiers = new ArrayList<PaymentWorksNewVendorRequestDTO>();
        
        try{
            responseForNewVendorRequestsRootResults = constructXmlResponseToUseForPagedData(getClient(), buildPaymentWorksApprovedNewVendorRequestsURI());
            PaymentWorksNewVendorRequestsRootDTO newVendorsRoot = responseForNewVendorRequestsRootResults.readEntity(PaymentWorksNewVendorRequestsRootDTO.class);
            LOG.info("retrieveAllPaymentWorksApprovedNewVendorRequests: newVendorsRoot.getCount()=" + newVendorsRoot.getCount());
            
            while (thereAreMorePmwVendorIdsToRetrieve(newVendorsRoot)) {
                pmwNewVendorIdentifiers.addAll(newVendorsRoot.getPmwNewVendorRequestsDTO().getPmwNewVendorRequests()); 
                closeResponseJustObtained(responseForNewVendorRequestsRootResults);
                
                if (additionalPagesOfPmwVendorIdsExist(newVendorsRoot)) {
                    responseForNewVendorRequestsRootResults = constructXmlResponseToUseForPagedData(getClient(), buildURI(newVendorsRoot.getNext()));
                    newVendorsRoot = responseForNewVendorRequestsRootResults.readEntity(PaymentWorksNewVendorRequestsRootDTO.class);
                } else {
                    newVendorsRoot = null;
                }
            }
            
            for (int i=0; i < pmwNewVendorIdentifiers.size(); i++) {
                LOG.info("retrieveAllPaymentWorksApprovedNewVendorRequests: PMW-Vendor-id=" + pmwNewVendorIdentifiers.get(i).getId());
            }
            
            return pmwNewVendorIdentifiers;
            
        } finally {
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
    
    private URI buildPaymentWorksApprovedNewVendorRequestsURI() {
        String URL = (new StringBuilder(getPaymentWorksUrl())
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUESTS)
                .append(PaymentWorksWebServiceConstants.QUESTION_MARK)
                .append(PaymentWorksWebServiceConstants.STATUS)
                .append(CUKFSConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.code)).toString();
        LOG.info("buildPaymentWorksApprovedNewVendorRequestsURI: URL =" + URL);
        return buildURI(URL);
    }
    
    @Override
    public PaymentWorksVendor obtainPmwNewVendorRequestDetailForPmwIdentifier(String pmwNewVendorRequestId, PaymentWorksNewVendorRequestsBatchReportData reportData) {
        PaymentWorksNewVendorRequestDetailDTO pmwDetailForSpecificVendorDTO = null;
        PaymentWorksVendor pmwDetailForSpecificVendor = null;
        try {
            pmwDetailForSpecificVendorDTO = retrieveAllPaymentWorksDetailsForRequestedVendor(buildPaymentWorksPendingNewVendorRequestDetailURI(pmwNewVendorRequestId));
        } catch (RuntimeException rte) {
            LOG.error("obtainPmwNewVendorRequestDetailForPmwIdentifier, unable to create PaymentWorksNewVendorRequestDetailDTO from payment works web service.", rte);
        }
        if (ObjectUtils.isNotNull(pmwDetailForSpecificVendorDTO)) {
            pmwDetailForSpecificVendor = getPaymentWorksDtoToPaymentWorksVendorConversionService().createPaymentWorksVendorFromPaymentWorksNewVendorRequestDetailDTO(
                    pmwDetailForSpecificVendorDTO, reportData);
        }
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
    public void sendProcessedStatusToPaymentWorksForNewVendor(String approvedVendorId) {
        LOG.info("sendProcessedStatusToPaymentWorksForNewVendor: Processing started.");
        String jsonString = buildPaymentWorksNewVendorUpdateStatusJson(approvedVendorId, PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PROCESSED.getCodeAsString());
        updateNewVendorStatusInPaymentWorks(jsonString);
        LOG.info("sendProcessedStatusToPaymentWorksForNewVendor: Processing complete.");
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
        Invocation request = buildJsonClientRequest(getClient(), uri, jsonString);
        Response response = request.invoke();
        response.bufferEntity();
        return response;
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
        Invocation request = buildXmlClientRequest(getClient(), uri);
        Response response = request.invoke();
        response.bufferEntity();
        return response;
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
            refreshTokenResponse = buildJsonResponse(refreshTokenURI, PaymentWorksWebServiceConstants.EMPTY_JSON_WRAPPER);
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
        String url = String.format(PaymentWorksTokenRefreshConstants.REFRESH_TOKEN_URL_FORMAT, getPaymentWorksUrl(), userId);
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
        JsonNode detailNode = rootNode.findValue(PaymentWorksCommonJsonConstants.DETAIL_FIELD);
        if (ObjectUtils.isNotNull(detailNode)) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Token refresh failed. Detail message: " + detailNode.textValue());
            handleDetailMessageFromTokenRefreshFailure(detailNode.textValue());
            throw new RuntimeException("Token refresh failed: Received failure response from PaymentWorks");
        }
        
        JsonNode statusNode = rootNode.findValue(PaymentWorksCommonJsonConstants.STATUS_FIELD);
        if (ObjectUtils.isNull(statusNode)) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Did not receive a refresh status from PaymentWorks");
            throw new RuntimeException("Token refresh failed: Did not receive a refresh status from PaymentWorks");
        } else if (!StringUtils.equalsIgnoreCase(PaymentWorksCommonJsonConstants.STATUS_OK, statusNode.textValue())) {
            LOG.error("checkForSuccessfulTokenRefreshStatus(): Unexpected status from PaymentWorks response: " + statusNode.textValue());
            throw new RuntimeException("Token refresh failed: Received an unexpected refresh status from PaymentWorks");
        } else {
            LOG.info("checkForSuccessfulTokenRefreshStatus(): Received a successful refresh status from PaymentWorks");
        }
    }

    @Override
    public int uploadVendorsToPaymentWorks(InputStream vendorCsvDataStream) {
        Response response = null;
        
        try {
            response = performSupplierUpload(vendorCsvDataStream);
            response.bufferEntity();
            String responseContent = response.readEntity(String.class);
            return getReceivedSuppliersCountIfSupplierUploadSucceeded(responseContent);
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }

    private Response performSupplierUpload(InputStream vendorCsvDataStream) {
        Invocation request = buildMultiPartRequestForSupplierUpload(getClient(), buildSupplierUploadURI(), vendorCsvDataStream);
        return request.invoke();
    }

    private URI buildSupplierUploadURI() {
        String url = getPaymentWorksUrl() + PaymentWorksWebServiceConstants.SUPPLIERS_LOAD;
        return buildURI(url);
    }

    private Invocation buildMultiPartRequestForSupplierUpload(Client client, URI uri, InputStream vendorCsvDataStream) {
        StreamDataBodyPart csvPart = new StreamDataBodyPart(
                PaymentWorksSupplierUploadConstants.SUPPLIERS_FIELD, vendorCsvDataStream, PaymentWorksSupplierUploadConstants.DUMMY_SUPPLIERS_FILENAME);
        
        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        multiPart.bodyPart(csvPart);
        
        WebTarget target = client.target(uri);
        Invocation.Builder requestBuilder = target.request();
        disableRequestChunkingIfNecessary(client, requestBuilder);
        
        return requestBuilder
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY, 
                             PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER + getPaymentWorksAuthorizationToken())
                .buildPost(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));
    }
    
    private int getReceivedSuppliersCountIfSupplierUploadSucceeded(String uploadResponse) {
        if (StringUtils.isBlank(uploadResponse)) {
            throw new RuntimeException("Supplier upload failed: No response was received from PaymentWorks");
        }
        
        JsonNode rootNode = readJsonTree(uploadResponse);
        checkForSuccessfulSupplierUploadStatus(rootNode);
        return getReceivedSuppliersCount(rootNode);
    }

    private void checkForSuccessfulSupplierUploadStatus(JsonNode rootNode) {
        checkForSupplierUploadErrors(rootNode, PaymentWorksSupplierUploadConstants.ERROR_FIELD, PaymentWorksCommonJsonConstants.DETAIL_FIELD);
        
        JsonNode statusNode = rootNode.findValue(PaymentWorksCommonJsonConstants.STATUS_FIELD);
        if (ObjectUtils.isNull(statusNode)) {
            LOG.error("checkForSuccessfulSupplierUploadStatus: Did not receive an upload status from PaymentWorks");
            throw new RuntimeException("Supplier upload failed: Did not receive an upload status from PaymentWorks");
        } else if (!StringUtils.equalsIgnoreCase(statusNode.textValue(), PaymentWorksCommonJsonConstants.STATUS_OK)) {
            LOG.error("checkForSuccessfulSupplierUploadStatus: Unexpected status from PaymentWorks response: " + statusNode.textValue());
            throw new RuntimeException("Supplier upload failed: Received an unexpected upload status from PaymentWorks: " + statusNode.textValue());
        } else {
            LOG.info("checkForSuccessfulSupplierUploadStatus: Received a successful upload status from PaymentWorks");
        }
    }

    private void checkForSupplierUploadErrors(JsonNode rootNode, String... errorFieldNames) {
        for (String errorFieldName : errorFieldNames) {
            JsonNode errorNode = rootNode.findValue(errorFieldName);
            if (ObjectUtils.isNotNull(errorNode)) {
                LOG.error("checkForSupplierUploadErrors: Supplier upload failed. Error message from \""
                        + errorFieldName + "\" field: " + errorNode.textValue());
                handleErrorMessageFromSupplierUploadFailure(errorFieldName, errorNode.textValue());
                throw new RuntimeException("Supplier upload failed: Received the following error message from PaymentWorks: " + errorNode.textValue());
            }
        }
    }

    private int getReceivedSuppliersCount(JsonNode rootNode) {
        JsonNode suppliersCountNode = rootNode.findValue(PaymentWorksSupplierUploadConstants.NUM_RCVD_SUPPLIERS_FIELD);
        if (ObjectUtils.isNull(suppliersCountNode)) {
            LOG.error("getReceivedSuppliersCount: Did not receive a num-received-suppliers count from PaymentWorks");
            throw new RuntimeException("Supplier upload failed: Did not receive a num-received-suppliers count from PaymentWorks");
        } else if (!suppliersCountNode.isInt()) {
            LOG.error("getReceivedSuppliersCount: Received a non-integer num-received-suppliers count from PaymentWorks");
            throw new RuntimeException("Supplier upload failed: Received a badly formatted num-received-suppliers count from PaymentWorks");
        }
        return suppliersCountNode.asInt();
    }

    protected void handleDetailMessageFromTokenRefreshFailure(String detailMessage) {
        // This is just a hook for unit testing convenience.
    }

    protected void handleErrorMessageFromSupplierUploadFailure(String errorFieldName, String errorMessage) {
        // This is just a hook for unit testing convenience.
    }

    public String getPaymentWorksUserId() {
        return getWebServiceCredentialValue(PaymentWorksCredentialKeys.PAYMENTWORKS_USER_ID);
    }

    public String getPaymentWorksAuthorizationToken() {
        return getWebServiceCredentialValue(PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN);
    }

    public String getPaymentWorksUrl() {
        return getWebServiceCredentialValue(PaymentWorksCredentialKeys.PAYMENTWORKS_API_URL);
    }

    protected String getWebServiceCredentialValue(String credentialKey) {
        return webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, credentialKey);
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
