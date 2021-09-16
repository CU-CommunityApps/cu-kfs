package edu.cornell.kfs.concur.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurOauth2PersistedValues;
import edu.cornell.kfs.concur.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAccessTokenV2ServiceImpl implements ConcurAccessTokenV2Service {
    private static final Logger LOG = LogManager.getLogger();
    
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public String retrieveNewAccessBearerToken() {
        ConcurOauth2PersistedValues credentialValues = getConcurOauth2PersistedValuesFromWebServiceCredentials();
        return null;
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

}
