package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.APIAuthenticator;

public interface APIAuthenticatorService {
    
    List<APIAuthenticator> getAuthenticatorsForAPICode(String apiCode);
    
}
