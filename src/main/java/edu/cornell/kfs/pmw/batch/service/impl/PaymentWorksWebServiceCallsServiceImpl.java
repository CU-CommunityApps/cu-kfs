package edu.cornell.kfs.pmw.batch.service.impl;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDtoToPaymentWorksVendorConversionService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDetailDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsRootDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class PaymentWorksWebServiceCallsServiceImpl implements PaymentWorksWebServiceCallsService, Serializable {
    private static final long serialVersionUID = -4282596886353845280L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksWebServiceCallsServiceImpl.class);
    
    private String paymentWorksUrl;
    private String paymentWorksAuthorizationToken;
    protected PaymentWorksDtoToPaymentWorksVendorConversionService paymentWorksDtoToPaymentWorksVendorConversionService;
    
    @Override
    public List<String> obtainPmwIdentifiersForPendingNewVendorRequests() {
        LOG.info("obtainPmwIdentifiersForPendingNewVendorRequests: Processing started.");
        List<String> pmwNewVendorIdentifiers = new ArrayList<String>();
        List<PaymentWorksNewVendorRequestDTO> paymentWorksNewVendorRequestDTOs = retrieveAllPaymentWorksPendingNewVendorRequests(buildPaymentWorksPendingNewVendorRequestsURI());
        pmwNewVendorIdentifiers = getAllPaymentWorksIdentifiersFromDTO(paymentWorksNewVendorRequestDTOs);
        LOG.info("obtainPmwIdentifiersForPendingNewVendorRequests: Processing completed.");
        return pmwNewVendorIdentifiers;
    }
    
    private List<PaymentWorksNewVendorRequestDTO> retrieveAllPaymentWorksPendingNewVendorRequests(URI pendingNewVendorsURI) {
        LOG.info("retrieveAllPaymentWorksPendingNewVendorRequests: pendingNewVendorsURI=" + pendingNewVendorsURI);
        Response responseForNewVendorRequestsRootResults = null;
        List<PaymentWorksNewVendorRequestDTO> pmwNewVendorIdentifiers = new ArrayList<PaymentWorksNewVendorRequestDTO>();
        try{
            responseForNewVendorRequestsRootResults = buildXmlOutput(pendingNewVendorsURI);
            PaymentWorksNewVendorRequestsRootDTO newVendorsRoot = responseForNewVendorRequestsRootResults.readEntity(PaymentWorksNewVendorRequestsRootDTO.class);
            LOG.info("retrieveAllPaymentWorksPendingNewVendorRequests: newVendorsRoot.getCount()=" + newVendorsRoot.getCount());
            if (newVendorsRoot.getCount() > 0) {
                pmwNewVendorIdentifiers.addAll(newVendorsRoot.getPmwNewVendorRequestsDTO().getPmwNewVendorRequests()); 
            }
            for (int i=0; i < pmwNewVendorIdentifiers.size(); i++) {
                LOG.info("retrieveAllPaymentWorksPendingNewVendorRequests: PMW-Vendor-id=" + pmwNewVendorIdentifiers.get(i).getId());
            }
            return pmwNewVendorIdentifiers;
        } finally {
            CURestClientUtils.closeQuietly(responseForNewVendorRequestsRootResults);
        }
    }
    
    private URI buildPaymentWorksPendingNewVendorRequestsURI() {
        String URL = (new StringBuilder(getPaymentWorksUrl())
                .append(PaymentWorksWebServiceConstants.NEW_VENDOR_REQUESTS)
                .append(PaymentWorksWebServiceConstants.QUESTION_MARK)
                .append(PaymentWorksWebServiceConstants.STATUS)
                .append(CUKFSConstants.EQUALS_SIGN)
                .append(PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.PENDING.code)).toString();
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
    
    public String getPaymentWorksUrl() {
        return paymentWorksUrl;
    }

    public void setPaymentWorksUrl(String paymentWorksUrl) {
        this.paymentWorksUrl = paymentWorksUrl;
    }

    public String getPaymentWorksAuthorizationToken() {
        return paymentWorksAuthorizationToken;
    }

    public void setPaymentWorksAuthorizationToken(String paymentWorksAuthorizationToken) {
        this.paymentWorksAuthorizationToken = paymentWorksAuthorizationToken;
    }

    public PaymentWorksDtoToPaymentWorksVendorConversionService getPaymentWorksDtoToPaymentWorksVendorConversionService() {
        return paymentWorksDtoToPaymentWorksVendorConversionService;
    }

    public void setPaymentWorksDtoToPaymentWorksVendorConversionService(
            PaymentWorksDtoToPaymentWorksVendorConversionService paymentWorksDtoToPaymentWorksVendorConversionService) {
        this.paymentWorksDtoToPaymentWorksVendorConversionService = paymentWorksDtoToPaymentWorksVendorConversionService;
    }
}
