package edu.cornell.kfs.ksr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;

public class SecurityRequestDocumentServiceImpl implements SecurityRequestDocumentService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SecurityRequestDocumentServiceImpl.class);

    private BusinessObjectService businessObjectService;

    @Override
    public List<SecurityGroup> getActiveSecurityGroups() {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("active", true);

        List<SecurityGroup> groupList = (List<SecurityGroup>) businessObjectService.findMatchingOrderBy(SecurityGroup.class, hashMap,
                KSRPropertyConstants.SECURITY_GROUP_NAME, true);

        return groupList;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
