package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.businessobject.ResourceAuthenticator;

public interface ResourceAuthenticatorService {
    
    /**
     * Retrieves a list of ResourceAuthenticator objects that are associated with the given resourceCode.
     * 
     * @param resourceCode The code of the resource to find authenticators for
     * @return A list of ResourceAuthenticator objects associated with the resourceCode
     */
    List<ResourceAuthenticator> getResourceAuthenticatorsByResourceCode(String resourceCode);
    
}
