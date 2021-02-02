package edu.cornell.kfs.concur.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.function.Function;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurAwsKeyNames;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.aws.ConcurStaticConfig;
import edu.cornell.kfs.concur.aws.ConcurTokenConfig;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.AwsSecretService;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurAccessTokenServiceImpl implements ConcurAccessTokenService {
	private static final Logger LOG = LogManager.getLogger(ConcurAccessTokenServiceImpl.class);

    private String concurRequestAccessTokenURL;
    private String concurRefreshAccessTokenURL;
    private String concurRevokeAccessTokenURL;
    protected AwsSecretService awsSecretService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    protected ConcurStaticConfig getStaticConfig() {
        return getConcurPojoFromAws(ConcurAwsKeyNames.STATIC_CONFIG, ConcurStaticConfig.class);
    }

    protected ConcurTokenConfig getTokenConfig() {
        return getConcurPojoFromAws(ConcurAwsKeyNames.TOKEN_CONFIG, ConcurTokenConfig.class);
    }

    protected <T> T getConcurPojoFromAws(String awsKeyName, Class<T> pojoClass) {
        try {
            return awsSecretService.getPojoFromAwsSecret(awsKeyName, true, pojoClass);
        } catch (JsonProcessingException e) {
            LOG.error("getConcurPojoFromAws: Unable to parse object from JSON value in AWS", e);
            throw new RuntimeException(e);
        }
    }

    protected void updateTokenConfig(String accessToken, Date expirationDate, String refreshToken) {
        ConcurTokenConfig tokenConfig = new ConcurTokenConfig();
        tokenConfig.setAccess_token(accessToken);
        tokenConfig.setAccess_token_expiration_date(expirationDate);
        tokenConfig.setRefresh_token(refreshToken);
        try {
            awsSecretService.updatePojo(ConcurAwsKeyNames.TOKEN_CONFIG, true, tokenConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public void requestNewAccessToken() {
        AccessTokenDTO newToken = callConcurEndpoint(
                this::buildRequestAccessTokenClientRequest, AccessTokenDTO.class);
        updateTokenConfig(newToken.getToken(), newToken.getExpirationDate(), newToken.getRefreshToken());
    }

    protected Invocation buildRequestAccessTokenClientRequest(Client client) {
        ConcurStaticConfig staticConfig = getStaticConfig();
        String credentials = buildCredentialsStringForRequestingNewAccessToken(
                staticConfig.getLogin_username(), staticConfig.getLogin_password());
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
                .header(ConcurConstants.CONSUMER_KEY_PROPERTY, staticConfig.getConsumer_key())
                .buildGet();
    }

    protected String buildCredentialsStringForRequestingNewAccessToken(String loginUsername, String loginPassword) {
        return loginUsername + ConcurConstants.USERNAME_PASSWORD_SEPARATOR + loginPassword;
    }

    @Transactional
    @Override
    public void refreshAccessToken() {
        if (isAccessTokenRefreshEnabled()) {
            ConcurTokenConfig oldTokenConfig = getTokenConfig();
            AccessTokenDTO refreshedToken = callConcurEndpoint(
                    this::buildRefreshAccessTokenClientRequest, AccessTokenDTO.class);
            updateTokenConfig(
                    refreshedToken.getToken(), refreshedToken.getExpirationDate(), oldTokenConfig.getRefresh_token());
        }
    }
    
    @Override
    public boolean isAccessTokenRefreshEnabled() {
        String refreshConcurToken = concurBatchUtilityService.getConcurParameterValue(
                ConcurParameterConstants.CONCUR_REFRESH_ACCESS_TOKEN);
        boolean shouldRefresh = StringUtils.equalsIgnoreCase(
                refreshConcurToken, KFSConstants.ParameterValues.YES);
        LOG.info("isAccessTokenRefreshEnabled, shouldRefresh: " + shouldRefresh);
        return shouldRefresh;
    }

    protected Invocation buildRefreshAccessTokenClientRequest(Client client) {
        ConcurStaticConfig staticConfig = getStaticConfig();
        ConcurTokenConfig tokenConfig = getTokenConfig();
        URI uri;
        try {
            uri = new URI(getConcurRefreshAccessTokenURL()
                    + ConcurConstants.REFRESH_TOKEN_URL_PARAM + CUKFSConstants.EQUALS_SIGN + tokenConfig.getRefresh_token()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_ID_URL_PARAM + CUKFSConstants.EQUALS_SIGN + staticConfig.getConsumer_key()
                    + CUKFSConstants.AMPERSAND + ConcurConstants.CLIENT_SECRET_URL_PARAM + CUKFSConstants.EQUALS_SIGN + staticConfig.getSecret_key());
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
            updateTokenConfig(StringUtils.EMPTY, null, StringUtils.EMPTY);
        } else {
            throw new RuntimeException("Error revoking access token: Unsuccessful response from Concur");
        }
    }
    
    @Override
    public void resetTokenToEmptyStringInDataStorage() {
        updateTokenConfig(StringUtils.EMPTY, null, StringUtils.EMPTY);
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
                .useProtocol(ConcurConstants.TLS_V1_2_PROTOCOL)
                .build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, new String[] {ConcurConstants.TLS_V1_2_PROTOCOL}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
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
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        String reasonPhrase = statusLine.getReasonPhrase();
        String responseContent = getEntityContentAsString(response.getEntity());
        
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
        return getTokenConfig().getAccess_token();
    }

    @Override
    public Date getAccessTokenExpirationDate() {
        return getTokenConfig().getAccess_token_expiration_date();
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

    public void setAwsSecretService(AwsSecretService awsSecretService) {
        this.awsSecretService = awsSecretService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
