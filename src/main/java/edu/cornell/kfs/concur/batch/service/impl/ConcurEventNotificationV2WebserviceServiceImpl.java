package edu.cornell.kfs.concur.batch.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurEventNotificationV2WebserviceServiceImpl implements ConcurEventNotificationV2WebserviceService {
    private static final Logger LOG = LogManager.getLogger();

    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType, String logMessageDetail) {
        LOG.info("buildConcurDTOFromEndpoint, " + logMessageDetail + " about to call endpoint: " + concurEndPoint);
        String jsonResponseString = null;
        int maxRetryCount = findMaxRetries();
        int retryCount = 1;
        while (retryCount <= maxRetryCount && jsonResponseString == null) {
            LOG.info("buildConcurDTOFromEndpoint, trying to build " + dtoType + " from concur endpoint, attempt number "
                    + retryCount);
            jsonResponseString = callConcurEndpoint(accessToken, concurEndPoint);
            retryCount++;
        }
        if (StringUtils.isBlank(jsonResponseString)) {
            throw new RuntimeException("buildConcurClientRequest, unable to call concur endpoint " + concurEndPoint);
        }

        return convertJsonToConcurDTO(jsonResponseString, dtoType);
    }

    protected int findMaxRetries() {
        String retryCountString = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findMaxRetries, the maximum number of retries is " + retryCountString);
        }
        return Integer.valueOf(retryCountString);
    }

    protected String callConcurEndpoint(String accessToken, String concurEndPoint) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonJsonProvider());
            client = ClientBuilder.newClient(clientConfig);
            response = buildConcurClientRequest(client, accessToken, concurEndPoint);
            if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                String responseString = response.readEntity(String.class);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("callConcurEndpoint, got a successful response, responseString: " + responseString);
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

    protected Response buildConcurClientRequest(Client client, String accessToken, String concurEndPoint) {
        URI uri;
        try {
            uri = new URI(concurEndPoint);
        } catch (URISyntaxException e) {
            LOG.error("buildConcurClientRequest, there was a problem building client request.", e);
            throw new RuntimeException(
                    "buildConcurClientRequest, An error occurred while building URI: " + concurEndPoint, e);
        }
        return client.target(uri)
                .request()
                .header(ConcurOAuth2.REQUEST_HEADER_CONTENT_TYPE_KEY_NAME, MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();
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
