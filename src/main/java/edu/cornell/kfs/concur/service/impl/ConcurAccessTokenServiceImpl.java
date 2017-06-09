package edu.cornell.kfs.concur.service.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.NewAccessTokenDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenServiceImpl implements ConcurAccessTokenService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurAccessTokenServiceImpl.class);

    private String concurRequestAccessTokenURL;
    private String concurRefreshAccessTokenURL;
    private String concurRevokeAccessTokenURL;
    protected WebServiceCredentialService webServiceCredentialService;
    protected ConfigurationService configurationService;

    @Override
    public void requestNewAccessToken() {
        NewAccessTokenDTO newToken = buildRequestAccessTokenOutput();
        
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN, newToken.getToken());
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE, newToken.getExpirationDate());
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_REFRESH_TOKEN, newToken.getRefreshToken());
    }

    protected NewAccessTokenDTO buildRequestAccessTokenOutput() {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);

        ClientResponse response = client.handle(buildRequestAccessTokenClientRequest());
        NewAccessTokenDTO newToken = response.getEntity(NewAccessTokenDTO.class);

        return newToken;
    }

    protected ClientRequest buildRequestAccessTokenClientRequest() {
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_XML);
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY,ConcurConstants.BASIC_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE
                + getEncodedUsernameAndPassword());
        builder.header(ConcurConstants.CONSUMER_KEY_PROPERTY, getConsumerKey());
        URI uri;
        try {
            uri = new URI(getConcurRequestAccessTokenURL());
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occured while building the request access token URI: ", e);
        }
        
        return builder.build(uri, HttpMethod.GET);
    }

    @Override
    public void refreshAccessToken() {
        AccessTokenDTO refreshedToken = buildRefreshAccessTokenOutput();
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE, refreshedToken.getExpirationDate()); 
    }

    private AccessTokenDTO buildRefreshAccessTokenOutput() {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);

        ClientResponse response = client.handle(buildRefreshAccessTokenClientRequest());     
        AccessTokenDTO refreshedToken = response.getEntity(AccessTokenDTO.class);

        return refreshedToken;
    }

    protected ClientRequest buildRefreshAccessTokenClientRequest() {
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_XML);
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY,ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + getAccessToken());
        URI uri;
        try {
            uri = new URI(getConcurRefreshAccessTokenURL()
                    + ConcurConstants.REFRESH_TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getRefreshToken()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_ID_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getConsumerKey()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_SECRET_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getSecretKey());
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occured while building the refresh access token URI: ", e);
        }
        
        return builder.build(uri, HttpMethod.GET);
    }

    @Override
    public void revokeAccessToken() {
        boolean success = executeRevokeAccessTokenRequest();
        
        if (success) {
            webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN, ConcurConstants.REVOKED_TOKEN_INDICATOR);
            webServiceCredentialService.updateWebServiceCredentialValue(
                    ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE, ConcurConstants.REVOKED_TOKEN_INDICATOR);
            webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_REFRESH_TOKEN, ConcurConstants.REVOKED_TOKEN_INDICATOR);
        }
    }

    protected boolean executeRevokeAccessTokenRequest() {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);

        ClientResponse response = client.handle(buildRevokeAccessTokenClientRequest());     
        int statusCode = response.getStatus();
        String reasonPhrase = response.getStatusInfo().getReasonPhrase();

        LOG.info("executeRevokeAccessTokenRequest(): Response status code: " + statusCode + ", reason phrase: " + reasonPhrase);
        return statusCode == ClientResponse.Status.OK.getStatusCode();
    }

    protected ClientRequest buildRevokeAccessTokenClientRequest() {
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_XML);
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY,ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + getAccessToken());
        URI uri;
        try {
            uri = new URI(getConcurRevokeAccessTokenURL()
                    + ConcurConstants.TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getAccessToken());
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occured while building the revoke access token URI: ", e);
        }
        
        return builder.build(uri, HttpMethod.POST);
    }

    @Override
    public boolean isCurrentAccessTokenRevoked() {
        return StringUtils.equals(ConcurConstants.REVOKED_TOKEN_INDICATOR, getAccessToken());
    }

    protected String getEncodedUsernameAndPassword() {
        return configurationService.getPropertyValueAsString(ConcurConstants.CONCUR_ENCODED_USERNAME_PASSWORD);
    }

    @Override
    public String getAccessToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN);
    }

    @Override
    public String getRefreshToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_REFRESH_TOKEN);
    }

    @Override
    public String getConsumerKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_CONSUMER_KEY);
    }

    @Override
    public String getSecretKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_SECRET_KEY);
    }

    public String getConcurRequestAccessTokenURL() {
        return concurRequestAccessTokenURL;
    }

    public void setConcurRequestAccessTokenURL(String concurRequestAccessTokenURL) {
        this.concurRequestAccessTokenURL = concurRequestAccessTokenURL;
    }

    public String getConcurRefreshAccessTokenURL() {
        return concurRefreshAccessTokenURL;
    }

    public void setConcurRefreshAccessTokenURL(String concurRefreshAccessTokenURL) {
        this.concurRefreshAccessTokenURL = concurRefreshAccessTokenURL;
    }

    public String getConcurRevokeAccessTokenURL() {
        return concurRevokeAccessTokenURL;
    }

    public void setConcurRevokeAccessTokenURL(String concurRevokeAccessTokenURL) {
        this.concurRevokeAccessTokenURL = concurRevokeAccessTokenURL;
    }

    public WebServiceCredentialService getWebServiceCredentialService() {
        return webServiceCredentialService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
