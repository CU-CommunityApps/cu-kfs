package edu.cornell.kfs.concur.batch.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
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
import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.businessobjects.ConcurOAuth2PersistedValues;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOAuth2TokenResponseDTO;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurAccessTokenV2ServiceImpl implements ConcurAccessTokenV2Service {
    private static final Logger LOG = LogManager.getLogger();
    
    protected WebServiceCredentialService webServiceCredentialService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected Pattern concurBaseUrlPattern;
    protected String concurAccessTokenApiPath;

    @Override
    public String retrieveNewAccessBearerToken() {
        ConcurOAuth2PersistedValues credentialValues = getConcurOAuth2PersistedValuesFromWebServiceCredentials();
        ConcurOAuth2TokenResponseDTO tokenResponse = getConcurOAuth2TokenResponseDTOFromConcurEndpoint(credentialValues, 
                this::buildRefreshAccessTokenClientRequest);
        updateRefreshTokenIfRequired(credentialValues, tokenResponse);
        return tokenResponse.getAccess_token();
    }
    
    @Override
    public void retrieveAndPersistNewRefreshToken() {
        ConcurOAuth2PersistedValues credentialValues = getConcurOAuth2PersistedValuesFromWebServiceCredentials();
        ConcurOAuth2TokenResponseDTO tokenResponse = getConcurOAuth2TokenResponseDTOFromConcurEndpoint(credentialValues, 
                this::buildGetNewRefreshTokenClientRequest);
        if (StringUtils.equals(credentialValues.getRefreshToken(), tokenResponse.getRefresh_token())) {
            throw new RuntimeException("Did NOT retrieve a new refresh token.");
        }
        updateRefreshTokenIfRequired(credentialValues, tokenResponse);
    }
    
    private ConcurOAuth2PersistedValues getConcurOAuth2PersistedValuesFromWebServiceCredentials() {
        ConcurOAuth2PersistedValues values = new ConcurOAuth2PersistedValues();
        values.setClientId(getWebserviceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.CLIENT_ID));
        values.setSecretId(getWebserviceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.SECRET_ID));
        values.setUserName(getWebserviceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.USER_NAME));
        values.setRefreshToken(getWebserviceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REFRESH_TOKEN));
        values.setRequestToken(getWebserviceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REQUEST_TOKEN));
        return values;
    }
    
    private String getWebserviceCredentialValue(String credentialKey) {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.GROUP_CODE, 
                credentialKey);
    }
    
    protected ConcurOAuth2TokenResponseDTO getConcurOAuth2TokenResponseDTOFromConcurEndpoint(ConcurOAuth2PersistedValues credentialValues, 
            Function<ConcurClientRequestHelper, Response> concurEndpointBuilder) {
        String tokenResponseString = null;
        int maxRetryCount = findMaxRetries();
        int retryCount = 0;
        while (retryCount < maxRetryCount && tokenResponseString == null) {
            LOG.info("getConcurOAuth2TokenResponseDTOFromConcurEndpoint, trying to get an access token from Concur, try number " + retryCount);
            tokenResponseString = callConcurAccessTokenEndpoint(credentialValues, concurEndpointBuilder);
            retryCount++;
        }
        if (StringUtils.isBlank(tokenResponseString)) {
            throw new RuntimeException("Unable to retrieve an access token.");
        }
        
        return convertJsonTokenToConcurOAuth2TokenResponseDTO(tokenResponseString);
    }
    
    protected int findMaxRetries() {
        String retryCountString = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES);
        LOG.info("findMaxRetries, the maximum number of retries is " + retryCountString);
        return Integer.valueOf(retryCountString);
    }
    
    protected String findAccessTokenEndpoint() {
        String endpoint = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_ACCESS_TOKEN_ENDPOINT);
        LOG.info("findAccessTokenEndpoint, the access token endpoint is " + endpoint);
        return endpoint;
    }

    protected String callConcurAccessTokenEndpoint(ConcurOAuth2PersistedValues credentialValues, Function<ConcurClientRequestHelper, Response> concurEndpointBuilder) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonJsonProvider());
            client = ClientBuilder.newClient(clientConfig);
            response = concurEndpointBuilder.apply(new ConcurClientRequestHelper(client, credentialValues));
            if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                LOG.info("callConcurAccessTokenEndpoint, successfully got a new access token");
                return response.readEntity(String.class);
            } else {
                LOG.error("callConcurAccessTokenEndpoint, unsuccessful response code returned when trying to get access token: " 
                        + response.getStatus());
                LOG.debug("callConcurAccessTokenEndpoint, the response: " + response.toString());
            }
        } catch (Exception e) {
            LOG.error("callConcurAccessTokenEndpoint, had an error trying to get an access token", e);
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
        return null;
    }
    
    protected Response buildRefreshAccessTokenClientRequest(ConcurClientRequestHelper helper) {
        URI uri;
        try {
            uri = new URI(findAccessTokenEndpoint());
        } catch (URISyntaxException e) {
            LOG.error("buildRefreshAccessTokenClientRequest, there was a problem building refresh access token client request.", e);
            throw new RuntimeException("An error occured while building the refresh access token URI: ", e);
        }
        final MultivaluedHashMap<String, String> entity = new MultivaluedHashMap<>();
        entity.add(ConcurOAuth2.FormFieldKeys.CLIENT_ID, helper.getCredentialValues().getClientId());
        entity.add(ConcurOAuth2.FormFieldKeys.CLIENT_SECRET, helper.getCredentialValues().getSecretId());
        entity.add(ConcurOAuth2.FormFieldKeys.REFRESH_TOKEN, helper.getCredentialValues().getRefreshToken());
        entity.add(ConcurOAuth2.FormFieldKeys.GRANT_TYPE, ConcurOAuth2.GRANT_TYPE_REFRESH_TOKEN_VALUE);
        return helper.getClient().target(uri)
                .request()
                .header(ConcurOAuth2.REQUEST_HEADER_CONTENT_TYPE_KEY_NAME, MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(entity));
    }
    
    protected Response buildGetNewRefreshTokenClientRequest(ConcurClientRequestHelper helper) {
        URI uri;
        try {
            uri = new URI(findAccessTokenEndpoint());
        } catch (URISyntaxException e) {
            LOG.error("buildGetNewRefreshTokenClientRequest, there was a problem building refresh access token client request.", e);
            throw new RuntimeException("An error occured while building the refresh access token URI: ", e);
        }
        final MultivaluedHashMap<String, String> entity = new MultivaluedHashMap<>();
        entity.add(ConcurOAuth2.FormFieldKeys.CLIENT_ID, helper.getCredentialValues().getClientId());
        entity.add(ConcurOAuth2.FormFieldKeys.CLIENT_SECRET, helper.getCredentialValues().getSecretId());
        entity.add(ConcurOAuth2.FormFieldKeys.USER_NAME, helper.getCredentialValues().getUserName());
        entity.add(ConcurOAuth2.FormFieldKeys.GRANT_TYPE, ConcurOAuth2.GRANT_TYPE_PASSWORD_VALUE);
        entity.add(ConcurOAuth2.FormFieldKeys.CREDTYPE, ConcurOAuth2.CRED_TYPE_AUTHTOKEN_VALUE);
        entity.add(ConcurOAuth2.FormFieldKeys.PASSWORD, helper.getCredentialValues().getRequestToken());
        LOG.info("buildGetNewRefreshTokenClientRequest, about to try and get a new refresh token");
        return helper.getClient().target(uri)
                .request()
                .header(ConcurOAuth2.REQUEST_HEADER_CONTENT_TYPE_KEY_NAME, MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(entity));
    }
    
    private void updateRefreshTokenIfRequired(ConcurOAuth2PersistedValues credentialValues, ConcurOAuth2TokenResponseDTO tokenResponse) {
        if (StringUtils.equals(credentialValues.getRefreshToken(), tokenResponse.getRefresh_token())) {
            LOG.info("updateRefreshTokenIfRequired, refresh token from Concur is the same as we have in storage, no need to update");
        } else {
            LOG.info("updateRefreshTokenIfRequired, Concur sent a new refresh token, we must update the value in storage");
            webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.GROUP_CODE, 
                    ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REFRESH_TOKEN, tokenResponse.getRefresh_token());
        }
    }
    
    private ConcurOAuth2TokenResponseDTO convertJsonTokenToConcurOAuth2TokenResponseDTO(String tokenResponse) {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        ConcurOAuth2TokenResponseDTO dto;
        try {
            dto = objectMapper.readValue(tokenResponse, ConcurOAuth2TokenResponseDTO.class);
        } catch (JsonProcessingException e) {
            LOG.error("convertJsonTokenToConcurOAuth2TokenResponseDTO, unable to convert json to ConcurOAuth2TokenResponseDTO", e);
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
    
    public void setConcurBaseUrlPattern(String concurBaseUrlPattern) {
        Pattern pattern = Pattern.compile(concurBaseUrlPattern, Pattern.CASE_INSENSITIVE);
        setConcurBaseUrlPattern(pattern);
    }
    
    public void setConcurBaseUrlPattern(Pattern concurBaseUrlPattern) {
        this.concurBaseUrlPattern = concurBaseUrlPattern;
    }
    
    public void setConcurAccessTokenApiPath(String concurAccessTokenApiPath) {
        this.concurAccessTokenApiPath = concurAccessTokenApiPath;
    }
    
    private class ConcurClientRequestHelper {
        private Client client;
        private ConcurOAuth2PersistedValues credentialValues;
        
        public ConcurClientRequestHelper(Client client, ConcurOAuth2PersistedValues credentialValues) {
            this.client = client;
            this.credentialValues = credentialValues;
        }

        public Client getClient() {
            return client;
        }

        public ConcurOAuth2PersistedValues getCredentialValues() {
            return credentialValues;
        }
    }

}
