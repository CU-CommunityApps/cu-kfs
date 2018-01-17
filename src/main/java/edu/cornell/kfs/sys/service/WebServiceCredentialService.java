package edu.cornell.kfs.sys.service;

import edu.cornell.kfs.sys.businessobject.WebServiceCredential;

import java.util.Collection;

public interface WebServiceCredentialService {
    
    String getWebServiceCredentialValue(String credentialGroupCode, String credentialKey);
    
    void updateWebServiceCredentialValue(String credentialGroupCode, String credentialKey, String credentialValue);

    Collection<WebServiceCredential> getWebServiceCredentialsByGroupCode(String credentialGroupCode);

}
