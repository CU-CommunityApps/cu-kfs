package edu.cornell.kfs.concur.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurAccessTokenServiceImpl implements ConcurAccessTokenService {
	private static final Logger LOG = LogManager.getLogger(ConcurAccessTokenServiceImpl.class);

    private String concurRequestAccessTokenURL;
    private String concurRefreshAccessTokenURL;
    private String concurRevokeAccessTokenURL;
    protected WebServiceCredentialService webServiceCredentialService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Transactional
    @Override
    public void requestNewAccessToken() {
        AccessTokenDTO newToken = callConcurEndpoint(
                this::buildRequestAccessTokenClientRequest, AccessTokenDTO.class);
        setWebserivceCredentialValues(newToken.getToken(), newToken.getExpirationDate(), newToken.getRefreshToken());
    }
    
    protected void setWebserivceCredentialValues(String accessToken, String expirationDate, String refreshToken) {
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_ACCESS_TOKEN, accessToken);
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE, expirationDate);
        webServiceCredentialService.updateWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_REFRESH_TOKEN, refreshToken);
    }

    protected Invocation buildRequestAccessTokenClientRequest(Client client) {
        String credentials = buildCredentialsStringForRequestingNewAccessToken(getLoginUsername(), getLoginPassword());
        String encodedCredentials = ConcurUtils.base64Encode(credentials);
        
        URI uri;
        try {
            uri = new URI(getConcurRequestAccessTokenURL());
        } catch (URISyntaxException e) {
            LOG.error("buildRequestAccessTokenClientRequest, problem building request acces token request", e);
            throw new RuntimeException("An error occured while building the request access token URI: ", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildRequestAccessTokenClientRequest, URI: " + uri);
        }
        
        return client.target(uri)
                .request()
                .accept(MediaType.APPLICATION_XML)
                .header(ConcurConstants.AUTHORIZATION_PROPERTY,
                        ConcurConstants.BASIC_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + encodedCredentials)
                .header(ConcurConstants.CONSUMER_KEY_PROPERTY, getConsumerKey())
                .buildGet();
    }

    protected String buildCredentialsStringForRequestingNewAccessToken(String loginUsername, String loginPassword) {
        return loginUsername + ConcurConstants.USERNAME_PASSWORD_SEPARATOR + loginPassword;
    }

    @Transactional
    @Override
    public void refreshAccessToken() {
        if (shouldRefreshAccessToken()) {
            AccessTokenDTO refreshedToken = callConcurEndpoint(
                    this::buildRefreshAccessTokenClientRequest, AccessTokenDTO.class);
            webServiceCredentialService.updateWebServiceCredentialValue(
                    ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE,
                    ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE,
                    refreshedToken.getExpirationDate()
            );
        }
    }
    
    private boolean shouldRefreshAccessToken() {
        String refreshConcurToken = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_REFRESH_ACCESS_TOKEN);
        boolean shouldRefresh = StringUtils.equalsIgnoreCase(
                refreshConcurToken, KFSConstants.ParameterValues.YES);
        LOG.info("shouldRefreshAccessToken, shouldRefresh: " + shouldRefresh);
        return shouldRefresh;
    }

    protected Invocation buildRefreshAccessTokenClientRequest(Client client) {
        URI uri;
        try {
            uri = new URI(getConcurRefreshAccessTokenURL()
                    + ConcurConstants.REFRESH_TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getRefreshToken()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_ID_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getConsumerKey()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_SECRET_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getSecretKey());
        } catch (URISyntaxException e) {
            LOG.error("buildRefreshAccessTokenClientRequest, there was a problem building refresh access token client request.", e);
            throw new RuntimeException("An error occured while building the refresh access token URI: ", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildRefreshAccessTokenClientRequest, URI: " + uri);
        }
        
        return client.target(uri)
                .request()
                .accept(MediaType.APPLICATION_XML)
                .header(ConcurConstants.AUTHORIZATION_PROPERTY,
                        ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + getAccessToken())
                .buildGet();
    }

    protected <T> T callConcurEndpoint(Function<Client, Invocation> requestBuilder, Class<T> responseEntityType) {
        Client client = null;
        Response response = null;
        
        try {
            ClientConfig clientConfig = new ClientConfig();
            client = ClientBuilder.newClient(clientConfig);
            Invocation request = requestBuilder.apply(client);
            response = request.invoke();
            return response.readEntity(responseEntityType);
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
    }

    @Transactional
    @Override
    public void revokeAndReplaceAccessToken() {
        boolean success = executeRevokeAccessTokenRequest();
        
        if (success) {
            requestNewAccessToken();
        } else {
            throw new RuntimeException("Error revoking access token: Unsuccessful response from Concur");
        }
    }
    
    @Transactional
    @Override
    public void revokeAccessToken() {
        boolean success = executeRevokeAccessTokenRequest();
        if (success) {
            setWebserivceCredentialValues(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
        } else {
            throw new RuntimeException("Error revoking access token: Unsuccessful response from Concur");
        }
    }
    
    @Override
    public void resetTokenToEmptyStringInDatabase() {
        setWebserivceCredentialValues(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    /*
     * NOTE: For some reason, we keep getting HTTP 400 ("Bad Request") errors when trying
     * to invoke the revoke-single-token endpoint with the Jersey 1.x client API.
     * To work around the problem, we use Apache's HttpClient API for those calls instead.
     */
    protected boolean executeRevokeAccessTokenRequest() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = buildSecureHttpClient();
            HttpPost request = buildRevokeAccessTokenClientRequest();
            Boolean success = httpClient.execute(request, this::checkForRevokeTokenSuccess);
            return Boolean.TRUE.equals(success);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            LOG.error("executeRevokeAccessTokenRequest, there was a problem revoking an access token. ", e);
            throw new RuntimeException("Error executing revoke-token request", e);
        } finally {
            IOUtils.closeQuietly(httpClient);
        }
    }

    protected CloseableHttpClient buildSecureHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom()
                .setProtocol(ConcurConstants.TLS_V1_2_PROTOCOL)
                .build();
        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
        .setSslContext(sslContext)
        .setTlsVersions(new String[] {ConcurConstants.TLS_V1_2_PROTOCOL})
        .setHostnameVerifier(new DefaultHostnameVerifier())
        .build();
        
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    protected HttpPost buildRevokeAccessTokenClientRequest() {
        URI uri;
        try {
            uri = new URI(getConcurRevokeAccessTokenURL()
                    + ConcurConstants.TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + getAccessToken());
        } catch (URISyntaxException e) {
            LOG.error("buildRevokeAccessTokenClientRequest, there was an error building revoke access token client request.", e);
            throw new RuntimeException("An error occured while building the revoke access token URI: ", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildRevokeAccessTokenClientRequest, URI: " + uri);
        }
        HttpPost request = new HttpPost(uri);
        request.addHeader(ConcurConstants.AUTHORIZATION_PROPERTY, ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + getAccessToken());
        return request;
    }

    protected Boolean checkForRevokeTokenSuccess(HttpResponse response) throws IOException {
        int statusCode = response.getCode();
        String reasonPhrase = response.getReasonPhrase();
        ClassicHttpResponse classicResponse = (ClassicHttpResponse) response;
        String responseContent = getEntityContentAsString(classicResponse.getEntity());
        
        LOG.info("checkForRevokeTokenSuccess(): Response status code: " + statusCode + ", reason phrase: " + reasonPhrase);
        if (StringUtils.isNotBlank(responseContent)) {
            LOG.error("checkForRevokeTokenSuccess(): Revoke-token request returned a non-blank response; KFS may have invalid security settings!");
        }
        
        return Boolean.valueOf(
                StringUtils.isBlank(responseContent) && statusCode == Response.Status.OK.getStatusCode());
    }

    protected String getEntityContentAsString(HttpEntity entity) throws IOException {
        InputStream entityStream = null;
        try {
            entityStream = entity.getContent();
            return IOUtils.toString(entityStream, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(entityStream);
        }
    }

    @Override
    public boolean currentAccessTokenExists() {
        return StringUtils.isNotBlank(getAccessToken());
    }

    @Override
    public String getAccessToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_ACCESS_TOKEN);
    }

    @Override
    public String getRefreshToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_REFRESH_TOKEN);
    }

    @Override
    public String getConsumerKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_CONSUMER_KEY);
    }

    @Override
    public String getSecretKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_SECRET_KEY);
    }

    @Override
    public String getLoginUsername() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_LOGIN_USERNAME);
    }

    @Override
    public String getLoginPassword() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE, ConcurConstants.CONCUR_LOGIN_PASSWORD);
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

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
