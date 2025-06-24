package edu.cornell.kfs.sys.service;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.kfs.sys.CUKFSConstants.EndpointCodes;

public interface ApiAuthenticationService {

    boolean isAuthorized(EndpointCodes endpointCode, HttpServletRequest request);
    boolean isAuthorized(EndpointCodes endpointCode, String usernamePassword);

}
