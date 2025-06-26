package edu.cornell.kfs.sys.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticator;
import edu.cornell.kfs.sys.businessobject.ApiEndpointDescription;
import edu.cornell.kfs.sys.businessobject.ApiEndpointAuthenticator;
import edu.cornell.kfs.sys.service.ApiAuthenticationService;

public class ApiAuthenticationServiceImpl implements ApiAuthenticationService {
    private static final Logger LOG = LogManager.getLogger();
    
    private BusinessObjectService businessObjectService;

    @Override
    public boolean isAuthorized(String endpointCode, HttpServletRequest request) {
        LOG.debug("isAuthorized: Checking authorization for endpoint code {} from request", endpointCode);
        
        String authorizationHeader = request.getHeader(CUKFSConstants.AUTHORIZATION_HEADER_KEY);
        if (StringUtils.isBlank(authorizationHeader) || 
                !authorizationHeader.startsWith(CUKFSConstants.BASIC_AUTHENTICATION_STARTER)) {
            LOG.warn("isAuthorized: Authorization header is missing or not using Basic authentication");
            return false;
        }
        
        String encodedCredentials = authorizationHeader.substring(CUKFSConstants.BASIC_AUTHENTICATION_STARTER.length());
        byte[] decodedBytes = Base64.decodeBase64(encodedCredentials);
        String usernamePassword = new String(decodedBytes, StandardCharsets.UTF_8);
        
        return isAuthorized(endpointCode, usernamePassword);
    }

    @Override
    public boolean isAuthorized(String endpointCode, String usernamePassword) {
        LOG.debug("isAuthorized: Checking authorization for endpoint code {} with credentials", endpointCode);

        if (StringUtils.isEmpty(endpointCode)) {
            throw new IllegalArgumentException("An endpoint code must be provided");
        }
        
        ApiEndpointDescription endpointDescription = getEndpointDescription(endpointCode);
        if (endpointDescription == null || !endpointDescription.isActive()) {
            LOG.warn("isAuthorized: Endpoint code {} not found or not active", endpointCode);
            return false;
        }
        
        List<ApiEndpointAuthenticator> authenticationMappings = endpointDescription.getAuthenticationMappings();
        if (authenticationMappings == null || authenticationMappings.isEmpty()) {
            LOG.warn("isAuthorized: No authentication mappings found for endpoint code {}", endpointCode);
            return false;
        }
        
        for (ApiEndpointAuthenticator descriptionAuthenticator : authenticationMappings) {
            if (descriptionAuthenticator.isActive() && descriptionAuthenticator.getApiAuthenticator() != null && 
                    descriptionAuthenticator.getApiAuthenticator().isActive()) {
                
                ApiAuthenticator authenticator = descriptionAuthenticator.getApiAuthenticator();
                if (StringUtils.equals(usernamePassword, authenticator.getUsernamePassword())) {
                    LOG.debug("isAuthorized: Successfully authenticated for endpoint code {} with authenticator {}", 
                        endpointCode, authenticator.getAuthenticatorDescription());
                    return true;
                } else {
                    LOG.debug("isAuthorized, user name and password does not match authenticator {}", authenticator.getAuthenticatorDescription());
                }
            }
        }
        
        LOG.error("isAuthorized: No matching credentials found for endpoint code {}", endpointCode);
        return false;
    }
    
    private ApiEndpointDescription getEndpointDescription(String endpointCode) {
        return businessObjectService.findBySinglePrimaryKey(ApiEndpointDescription.class, endpointCode);
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
