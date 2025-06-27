package edu.cornell.kfs.sys.service;

import javax.servlet.http.HttpServletRequest;

public interface ApiAuthenticationService {

    boolean isAuthorized(String endpointCode, HttpServletRequest request);
    boolean isAuthorized(String endpointCode, String usernamePassword);

}
