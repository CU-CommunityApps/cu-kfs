package edu.cornell.kfs.sys.service;


public interface WebServiceCredentialService {
    
    String getWebServiceCredentialValue(String credentialKey);
    
    void updateWebServiceCredentialValue(String credentialKey, String credentialValue);

}
