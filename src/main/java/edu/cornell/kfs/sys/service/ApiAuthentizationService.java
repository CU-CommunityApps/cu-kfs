package edu.cornell.kfs.sys.service;

import javax.servlet.http.HttpServletRequest;

public interface ApiAuthentizationService {

    boolean isAuthorized(String resourceCode, HttpServletRequest request);
    boolean isAuthorized(String resourceCode, String usernamePassword);

}
