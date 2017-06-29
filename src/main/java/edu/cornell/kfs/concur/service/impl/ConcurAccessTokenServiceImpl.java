package edu.cornell.kfs.concur.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenServiceImpl implements ConcurAccessTokenService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurAccessTokenServiceImpl.class);

    private String concurLoginUsername;
    private String concurLoginPassword;
    private String concurRequestAccessTokenURL;
    private String concurRefreshAccessTokenURL;
    private String concurRevokeAccessTokenURL;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public void requestNewAccessToken() {
        AccessTokenDTO newToken = buildRequestAccessTokenOutput();
        
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN, newToken.getToken());
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE, newToken.getExpirationDate());
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_REFRESH_TOKEN, newToken.getRefreshToken());
    }

    protected AccessTokenDTO buildRequestAccessTokenOutput() {
        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);

        ClientResponse response = client.handle(buildRequestAccessTokenClientRequest());
        AccessTokenDTO newToken = response.getEntity(AccessTokenDTO.class);

        return newToken;
    }

    protected ClientRequest buildRequestAccessTokenClientRequest() {
        String encodedCredentials = ConcurUtils.encodeCredentialsForRequestingNewAccessToken(getConcurLoginUsername(), getConcurLoginPassword());
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.accept(MediaType.APPLICATION_XML);
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY,
                ConcurConstants.BASIC_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + encodedCredentials);
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
        } else {
            throw new RuntimeException("Error revoking access token: Unsuccessful response from Concur");
        }
    }

    protected boolean executeRevokeAccessTokenRequest() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, buildHTTPSProperties());
        Client client = Client.create(clientConfig);

        ClientResponse response = client.handle(buildRevokeAccessTokenClientRequest());     
        int statusCode = response.getStatus();
        String reasonPhrase = response.getStatusInfo().getReasonPhrase();
        String responseContent = response.getEntity(String.class);

        LOG.info("executeRevokeAccessTokenRequest(): Response status code: " + statusCode + ", reason phrase: " + reasonPhrase);
        if (StringUtils.isNotBlank(responseContent)) {
            LOG.error("executeRevokeAccessTokenRequest(): Revoke-token request returned a non-blank response; KFS may have invalid TLS settings.");
        }
        return StringUtils.isBlank(responseContent) && statusCode == ClientResponse.Status.OK.getStatusCode();
    }

    protected ClientRequest buildRevokeAccessTokenClientRequest() {
        ClientRequest.Builder builder = new ClientRequest.Builder();
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY, ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + getAccessToken());
        URI uri;
        try {
            uri = new URI(getConcurRevokeAccessTokenURL()
                    + ConcurConstants.TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getAccessToken());
        } catch (URISyntaxException e) {
            throw new RuntimeException("An error occured while building the revoke access token URI: ", e);
        }
        return builder.build(uri, HttpMethod.POST);
    }

    protected HTTPSProperties buildHTTPSProperties() {
        HostnameVerifier allowEveryoneHostnameVerifier = (hostname, session) -> true;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance(ConcurConstants.TLS_V1_2_ALGORITHM);
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("An error occurred while preparing the SSL context: ", e);
        }
        return new HTTPSProperties(allowEveryoneHostnameVerifier, sslContext);
    }

    @Override
    public boolean isCurrentAccessTokenRevoked() {
        return StringUtils.equals(ConcurConstants.REVOKED_TOKEN_INDICATOR, getAccessToken());
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

    public String getConcurLoginUsername() {
        return concurLoginUsername;
    }

    public void setConcurLoginUsername(String concurLoginUsername) {
        this.concurLoginUsername = concurLoginUsername;
    }

    public String getConcurLoginPassword() {
        return concurLoginPassword;
    }

    public void setConcurLoginPassword(String concurLoginPassword) {
        this.concurLoginPassword = concurLoginPassword;
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

}
