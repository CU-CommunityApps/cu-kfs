package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.businessobject.APIAuthenticator;
import edu.cornell.kfs.sys.businessobject.APIEndpointDescriptor;
import edu.cornell.kfs.sys.service.APIAuthenticatorService;

public class APIAuthenticatorServiceImpl implements APIAuthenticatorService {
    
    private BusinessObjectService businessObjectService;
    
    @Override
    public List<APIAuthenticator> getAuthenticatorsForAPICode(String apiCode) {
        List<APIAuthenticator> authenticators = new ArrayList<>();
        
        // Find the API endpoint descriptor with the given API code
        APIEndpointDescriptor endpointDescriptor = findEndpointDescriptorByApiCode(apiCode);
        
        if (endpointDescriptor != null) {
            // Find all authenticators associated with this endpoint descriptor
            authenticators = findAuthenticatorsForEndpointDescriptor(endpointDescriptor);
        }
        
        return authenticators;
    }
    
    private APIEndpointDescriptor findEndpointDescriptorByApiCode(String apiCode) {
        java.util.Map<String, Object> criteria = new java.util.HashMap<>();
        criteria.put("apiCode", apiCode);
        return businessObjectService.findByPrimaryKey(APIEndpointDescriptor.class, criteria);
    }
    
    private List<APIAuthenticator> findAuthenticatorsForEndpointDescriptor(APIEndpointDescriptor endpointDescriptor) {
        // This query is more complex as it involves a many-to-many relationship
        // We need to find all authenticators that have this endpoint descriptor in their collection
        // This would typically be done with a custom DAO method, but for simplicity,
        // we'll use a direct query approach here
        
        java.util.Map<String, Object> criteria = new java.util.HashMap<>();
        criteria.put("apiEndpointDescriptors.id", endpointDescriptor.getId());
        return (List<APIAuthenticator>) businessObjectService.findMatching(APIAuthenticator.class, criteria);
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
