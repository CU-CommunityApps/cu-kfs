package edu.cornell.kfs.sys.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticator;
import edu.cornell.kfs.sys.businessobject.ApiEndpointDescriptor;
import edu.cornell.kfs.sys.businessobject.ApiAuthenticationMapping;
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
        
        ApiEndpointDescriptor endpointDescriptor = getEndpointDescriptor(endpointCode);
        if (ObjectUtils.isNull(endpointDescriptor) || !endpointDescriptor.isActive()) {
            LOG.warn("isAuthorized: Endpoint code {} not found or not active", endpointCode);
            return false;
        }
        
        List<ApiAuthenticationMapping> authenticationMappings = endpointDescriptor.getAuthenticationMappings();
        if (ObjectUtils.isNull(authenticationMappings) || authenticationMappings.isEmpty()) {
            LOG.warn("isAuthorized: No authentication mappings found for endpoint code {}", endpointCode);
            return false;
        }
        
        for (ApiAuthenticationMapping authenticationMapping : authenticationMappings) {
            if (ObjectUtils.isNotNull(authenticationMapping) && authenticationMapping.isActive() && 
                authenticationMapping.getApiAuthenticator() != null && 
                authenticationMapping.getApiAuthenticator().isActive()) {
                
                ApiAuthenticator authenticator = authenticationMapping.getApiAuthenticator();
                if (StringUtils.equals(usernamePassword, authenticator.getUsernamePassword())) {
                    LOG.debug("isAuthorized: Successfully authenticated for endpoint code {} with authenticator {}", 
                        endpointCode, authenticator.getAuthenticatorDescription());
                    return true;
                } else {
                    LOG.debug("isAuthorized, user name and password does not match authenticator {}", authenticator.getAuthenticatorDescription());
                }
            }
        }
        
        LOG.debug("isAuthorized: No matching credentials found for endpoint code {}", endpointCode);
        return false;
    }
    
    private ApiEndpointDescriptor getEndpointDescriptor(String endpointCode) {
        return businessObjectService.findBySinglePrimaryKey(ApiEndpointDescriptor.class, endpointCode);
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
