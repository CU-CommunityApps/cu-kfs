package edu.cornell.kfs.sys.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.businessobject.ResourceAuthenticator;
import edu.cornell.kfs.sys.service.ResourceAuthenticatorService;

public class ResourceAuthenticatorServiceImpl implements ResourceAuthenticatorService {
    
    protected BusinessObjectService businessObjectService;
    
    @Override
    public List<ResourceAuthenticator> getResourceAuthenticatorsByResourceCode(String resourceCode) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(KFSPropertyConstants.ACTIVE, Boolean.TRUE);
        criteria.put("resourceDescriptions.resourceCode", resourceCode);
        
        return (List<ResourceAuthenticator>) businessObjectService.findMatching(ResourceAuthenticator.class, criteria);
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
