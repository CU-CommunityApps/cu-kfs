package edu.cornell.kfs.concur.batch.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.businessobjects.ConcurOauth2PersistedValues;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOauth2TokenResponseDTO;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurAccessTokenV2ServiceImpl implements ConcurAccessTokenV2Service {
    private static final Logger LOG = LogManager.getLogger();
    
    protected WebServiceCredentialService webServiceCredentialService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public String retrieveNewAccessBearerToken() {
        ConcurOauth2PersistedValues credentialValues = getConcurOauth2PersistedValuesFromWebServiceCredentials();
        ConcurOauth2TokenResponseDTO tokenResopnse = getAccessTokenFromConcurEndPoint(credentialValues);
        updateRefreshTokenIfRequired(credentialValues, tokenResopnse);
        return tokenResopnse.getAccess_token();
    }
    
    private ConcurOauth2PersistedValues getConcurOauth2PersistedValuesFromWebServiceCredentials() {
        ConcurOauth2PersistedValues values = new ConcurOauth2PersistedValues();
        values.setClientId(getWebserviceCredentailValue(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_CLIENT_ID));
        values.setSecretId(getWebserviceCredentailValue(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_SECRET_ID));
        values.setUserName(getWebserviceCredentailValue(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_USER_NAME));
        values.setRefreshToken(getWebserviceCredentailValue(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_REFRESH_TOKEN));
        values.setRequestToken(getWebserviceCredentailValue(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_REQUEST_TOKEN));
        return values;
    }
    
    private String getWebserviceCredentailValue(String credentialKey) {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE_OAUTH_2, 
                credentialKey);
    }
    
    protected ConcurOauth2TokenResponseDTO getAccessTokenFromConcurEndPoint(ConcurOauth2PersistedValues credentialValues) {
        //ConcurOauth2TokenResponseDTO tokenResponse = null;
        String tokenResponse = null;
        int maxRetryCount = findMaxRetries();
        String accessTokenEndpoint = findAccesTokenEndpoint();
        int retryCount = 0;
        while (retryCount < maxRetryCount && tokenResponse == null) {
            LOG.info("getAccessTokenFromConcurEndPoint, trying to get an access token from Concur, try number " + retryCount);
            tokenResponse = callConcurAccessTokenEndpoint(credentialValues, accessTokenEndpoint);
            retryCount++;
        }
        if (tokenResponse == null) {
            throw new RuntimeException("Unable to retrieve an access token.");
        }
        
        return convertJsonTokenToConcurOauth2TokenResponseDTO(tokenResponse);
    }
    
    protected int findMaxRetries() {
        String retryCountString = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES);
        LOG.info("findMaxRetries, the maximum number of retries is " + retryCountString);
        return Integer.valueOf(retryCountString);
    }
    
    protected String findAccesTokenEndpoint() {
        String endpoint = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_ACCESS_TOKEN_ENDPOINT);
        LOG.info("findAccesTokenEndpoint, the access token endpoint is " + endpoint);
        return endpoint;
    }

    protected String callConcurAccessTokenEndpoint(ConcurOauth2PersistedValues credentialValues, String accessTokenEndpoint) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonJsonProvider());
            client = ClientBuilder.newClient(clientConfig);
            response = buildRefreshAccessTokenClientRequest(client, credentialValues, accessTokenEndpoint);
            if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                LOG.info("callConcurAccessTokenEndpoint, successfully got a new access token");
                return response.readEntity(String.class);
            } else {
                LOG.error("callConcurAccessTokenEndpoint, unsuccessful response code returned when trying to get access token: " + response.getStatus());
            }
        } catch (Exception e) {
          LOG.error("callConcurAccessTokenEndpoint, had an error trying to get an access token", e);  
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
        return null;
    }
    
    protected Response buildRefreshAccessTokenClientRequest(Client client, ConcurOauth2PersistedValues credentialValues, String accessTokenEndpoint) {
        URI uri;
        try {
            uri = new URI(accessTokenEndpoint);
        } catch (URISyntaxException e) {
            LOG.error("buildRefreshAccessTokenClientRequest, there was a problem building refresh access token client request.", e);
            throw new RuntimeException("An error occured while building the refresh access token URI: ", e);
        }
        final MultivaluedHashMap<String, String> entity = new MultivaluedHashMap<>();
        entity.add("client_id", credentialValues.getClientId());
        entity.add("client_secret", credentialValues.getSecretId());
        entity.add("refresh_token", credentialValues.getRefreshToken());
        entity.add("grant_type", "refresh_token");
        LOG.info("buildRefreshAccessTokenClientRequest, built the enity map");
        return client.target(uri)
                .request()
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(entity));
    }
    
    private void updateRefreshTokenIfRequired(ConcurOauth2PersistedValues credentialValues, ConcurOauth2TokenResponseDTO tokenResopnse) {
        if (StringUtils.equals(credentialValues.getRefreshToken(), tokenResopnse.getRefresh_token())) {
            LOG.info("updateRefreshTokenIfRequired, refresh token from Concur is the same as we have in storage, no need to update");
        } else {
            LOG.info("updateRefreshTokenIfRequired, Concur sent a new refresh token, we must update the value in storage");
            webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE_OAUTH_2, 
                    ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_REFRESH_TOKEN, tokenResopnse.getRefresh_token());
        }
    }
    
    private ConcurOauth2TokenResponseDTO convertJsonTokenToConcurOauth2TokenResponseDTO(String tokenResponse) {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        ConcurOauth2TokenResponseDTO dto;
        try {
            dto = objectMapper.readValue(tokenResponse, ConcurOauth2TokenResponseDTO.class);
        } catch (JsonProcessingException e) {
            LOG.error("convertJsonTokenToConcurOauth2TokenResponseDTO, unable to conver json to ConcurOauth2TokenResponseDTO", e);
            throw new RuntimeException(e);
        }
        return dto;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
