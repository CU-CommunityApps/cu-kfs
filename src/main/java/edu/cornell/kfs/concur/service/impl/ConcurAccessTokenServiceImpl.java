package edu.cornell.kfs.concur.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenServiceImpl implements ConcurAccessTokenService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurAccessTokenServiceImpl.class);

    protected String concurRefreshAccessTokenURL;
    protected WebServiceCredentialService webServiceCredentialService;

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
        builder.header(ConcurConstants.AUTHORIZATION_PROPERTY,ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + getConcurAccessToken());
        URI uri;
        try {
            uri = new URI(getConcurRefreshAccessTokenURL()
                    + ConcurConstants.REFRESH_TOKEN_URL_PARAM
                    + getConcurRefreshToken()
                    + ConcurConstants.CLIENT_ID_URL_PARAM
                    + getConcurConsumerKey()
                    + ConcurConstants.CLIENT_SECRET_URL_PARAM
                    + getConcurSecretKey());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        ClientRequest request = builder.build(uri, HttpMethod.GET);
        return request;
    }

    @Override
    public String getConcurAccessToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN);
    }

    @Override
    public String getConcurRefreshToken() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_REFRESH_TOKEN);
    }

    @Override
    public String getConcurConsumerKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_CONSUMER_KEY);
    }

    @Override
    public String getConcurSecretKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_SECRET_KEY);
    }
    
    public String getConcurRefreshAccessTokenURL() {
        return concurRefreshAccessTokenURL;
    }

    public void setConcurRefreshAccessTokenURL(String concurRefreshAccessTokenURL) {
        this.concurRefreshAccessTokenURL = concurRefreshAccessTokenURL;
    }

    public WebServiceCredentialService getWebServiceCredentialService() {
        return webServiceCredentialService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }
}
