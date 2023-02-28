package edu.cornell.kfs.concur.batch.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationApiService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.ConcurWebRequest;
import edu.cornell.kfs.concur.batch.ConcurWebRequestBuilder;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurEventNotificationApiServiceImpl implements ConcurEventNotificationApiService {
    private static final Logger LOG = LogManager.getLogger();

    private static final String EMPTY_JSON_OBJECT = "{}";

    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail) {
        ConcurWebRequest<T> webRequest = ConcurWebRequestBuilder.forRequestExpectingResponseOfType(dtoType)
                .withUrl(concurEndPoint)
                .withHttpMethod(HttpMethod.GET)
                .withEmptyBody()
                .build();
        return callConcurEndpoint(accessToken, webRequest, logMessageDetail);
    }

    @Override
    public <T> T callConcurEndpoint(String accessToken, ConcurWebRequest<T> webRequest, String logMessageDetail) {
        LOG.info("buildConcurDTOFromEndpoint, " + logMessageDetail + " about to call endpoint: " + webRequest.getUrl());
        String callPurpose = webRequest.expectsEmptyResponse()
                ? "run no-response-content operation"
                : "build " + webRequest.getResponseType();
        String jsonResponseString = null;
        int maxRetryCount = findMaxRetries();
        int retryCount = 1;
        while (retryCount <= maxRetryCount && jsonResponseString == null) {
            LOG.info("buildConcurDTOFromEndpoint, trying to " + callPurpose + " from concur endpoint, attempt number "
                    + retryCount);
            jsonResponseString = callConcurEndpoint(accessToken, webRequest);
            retryCount++;
        }
        if (StringUtils.isBlank(jsonResponseString)) {
            throw new RuntimeException("buildConcurClientRequest, unable to call concur endpoint " + webRequest.getUrl());
        }

        if (webRequest.expectsEmptyResponse()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("buildConcurDTOFromEndpoint, The call was not expected to return a value; any "
                        + "actual or placeholder values from the call will be discarded.");
            }
            return null;
        } else {
            return convertJsonToConcurDTO(jsonResponseString, webRequest.getResponseType());
        }
    }

    protected int findMaxRetries() {
        String retryCountString = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findMaxRetries, the maximum number of retries is " + retryCountString);
        }
        return Integer.valueOf(retryCountString);
    }

    protected String callConcurEndpoint(String accessToken, ConcurWebRequest<?> webRequest) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonJsonProvider());
            client = ClientBuilder.newClient(clientConfig);
            response = buildConcurClientRequest(client, accessToken, webRequest);
            if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                String responseString = response.readEntity(String.class);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("callConcurEndpoint, got a successful response, responseString: " + responseString);
                }
                if (webRequest.expectsEmptyResponse()) {
                    if (StringUtils.isNotBlank(responseString)) {
                        LOG.warn("callConcurEndpoint, The endpoint was expected to return an empty response body "
                                + "but it actually returned a non-empty one. The response body will be ignored.");
                    }
                    responseString = EMPTY_JSON_OBJECT;
                }
                return responseString;
            } else {
                LOG.error("callConcurEndpoint, unsuccessful response code returned when trying to call endpoint: "
                        + response.getStatus() + " with details of " + response.toString());
            }
        } catch (Exception e) {
            LOG.error("callConcurEndpoint, had an error trying to call concur end point", e);
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
        return null;
    }

    protected Response buildConcurClientRequest(Client client, String accessToken, ConcurWebRequest<?> webRequest) {
        URI uri;
        try {
            uri = new URI(webRequest.getUrl());
        } catch (URISyntaxException e) {
            LOG.error("buildConcurClientRequest, there was a problem building client request.", e);
            throw new RuntimeException(
                    "buildConcurClientRequest, An error occurred while building URI: " + webRequest.getUrl(), e);
        }
        Invocation.Builder invocation = client.target(uri)
                .request()
                .header(ConcurOAuth2.REQUEST_HEADER_CONTENT_TYPE_KEY_NAME, MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON);
        if (webRequest.hasJsonBody()) {
            return invocation.method(webRequest.getHttpMethodAsString(), Entity.json(webRequest.getJsonBody()));
        } else {
            return invocation.method(webRequest.getHttpMethodAsString());
        }
    }

    protected <T> T convertJsonToConcurDTO(String jsonString, Class<T> dtoType) {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        T dto;
        try {
            dto = objectMapper.readValue(jsonString, dtoType);
        } catch (JsonProcessingException e) {
            LOG.error("convertJsonToConcurDTO, unable to convert json to " + dtoType, e);
            throw new RuntimeException("convertJsonToConcurDTO, unable to convert token response to a java dto ", e);
        }
        return dto;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }
}
