package edu.cornell.kfs.sys.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.beans.factory.annotation.Autowired;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.ApiResourceAuthenticator;
import edu.cornell.kfs.sys.businessobject.ApiResourceDescription;
import edu.cornell.kfs.sys.businessobject.ApiResourceDescriptionAuthenticator;
import edu.cornell.kfs.sys.service.ApiAuthentizationService;

public class ApiAuthentizationServiceImpl implements ApiAuthentizationService {
    private static final Logger LOG = LogManager.getLogger();
    
    @Autowired
    private BusinessObjectService businessObjectService;

    @Override
    public boolean isAuthorized(String resourceCode, HttpServletRequest request) {
        LOG.debug("isAuthorized: Checking authorization for resource code {} from request", resourceCode);
        
        if (StringUtils.isBlank(resourceCode) || request == null) {
            LOG.warn("isAuthorized: Resource code is blank or request is null");
            return false;
        }
        
        String authorizationHeader = request.getHeader(CUKFSConstants.AUTHORIZATION_HEADER_KEY);
        if (StringUtils.isBlank(authorizationHeader) || 
                !authorizationHeader.startsWith(CUKFSConstants.BASIC_AUTHENTICATION_STARTER)) {
            LOG.warn("isAuthorized: Authorization header is missing or not using Basic authentication");
            return false;
        }
        
        String encodedCredentials = authorizationHeader.substring(CUKFSConstants.BASIC_AUTHENTICATION_STARTER.length());
        byte[] decodedBytes = Base64.decodeBase64(encodedCredentials);
        String usernamePassword = new String(decodedBytes, StandardCharsets.UTF_8);
        
        return isAuthorized(resourceCode, usernamePassword);
    }

    @Override
    public boolean isAuthorized(String resourceCode, String usernamePassword) {
        LOG.debug("isAuthorized: Checking authorization for resource code {} with credentials", resourceCode);
        
        if (StringUtils.isBlank(resourceCode) || StringUtils.isBlank(usernamePassword)) {
            LOG.warn("isAuthorized: Resource code or credentials are blank");
            return false;
        }
        
        ApiResourceDescription resourceDescription = getResourceDescription(resourceCode);
        if (resourceDescription == null || !resourceDescription.isActive()) {
            LOG.warn("isAuthorized: Resource code {} not found or not active", resourceCode);
            return false;
        }
        
        List<ApiResourceDescriptionAuthenticator> descriptionAuthenticators = resourceDescription.getDescriptionAuthenticators();
        if (descriptionAuthenticators == null || descriptionAuthenticators.isEmpty()) {
            LOG.warn("isAuthorized: No descritpion authenticators found for resource code {}", resourceCode);
            return false;
        }
        
        for (ApiResourceDescriptionAuthenticator descriptionAuthenticator : descriptionAuthenticators) {
            if (descriptionAuthenticator.isActive() && descriptionAuthenticator.getApiResourceAuthenticator() != null && 
                    descriptionAuthenticator.getApiResourceAuthenticator().isActive()) {
                
                ApiResourceAuthenticator authenticator = descriptionAuthenticator.getApiResourceAuthenticator();
                if (StringUtils.equals(usernamePassword, authenticator.getUsernamePassword())) {
                    LOG.info("isAuthorized: Successfully authenticated for resource code {}", resourceCode);
                    return true;
                } else {
                    LOG.debug("isAuthorized, user name and password does not match authenticator {}", authenticator.getAuthenticatorDescription());
                }
            }
        }
        
        LOG.debug("isAuthorized: No matching credentials found for resource code {}", resourceCode);
        return false;
    }
    
    private ApiResourceDescription getResourceDescription(String resourceCode) {
        return businessObjectService.findBySinglePrimaryKey(ApiResourceDescription.class, resourceCode);
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
