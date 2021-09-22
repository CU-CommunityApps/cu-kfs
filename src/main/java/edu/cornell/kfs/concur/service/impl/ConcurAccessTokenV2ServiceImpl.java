package edu.cornell.kfs.concur.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurOauth2PersistedValues;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOauth2TokenResponseDTO;
import edu.cornell.kfs.concur.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenV2ServiceImpl implements ConcurAccessTokenV2Service {
    private static final Logger LOG = LogManager.getLogger();
    
    protected WebServiceCredentialService webServiceCredentialService;

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
        values.setUserName(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_USER_NAME);
        values.setRefreshToken(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_REFRESH_TOKEN);
        values.setRequestToken(ConcurConstants.CONCUR_OAUTH2_WEB_SERVICE_CREDENTIAL_REQUEST_TOKEN);
        return values;
    }
    
    private String getWebserviceCredentailValue(String credentialKey) {
        return webServiceCredentialService.getWebServiceCredentialValue(ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE_OAUTH_2, credentialKey);
    }
    
    private ConcurOauth2TokenResponseDTO getAccessTokenFromConcurEndPoint(ConcurOauth2PersistedValues credentialValues) {
        return null;
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

}
