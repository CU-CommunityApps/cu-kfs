package edu.cornell.kfs.sys.service.impl;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class WebServiceCredentialServiceImpl implements WebServiceCredentialService {
    protected BusinessObjectService businessObjectService;

    @Override
    public String getWebServiceCredentialValue(String credentialKey) {
        String credentialValue = StringUtils.EMPTY;
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY, credentialKey);
        WebServiceCredential webServiceCredential = businessObjectService.findByPrimaryKey(WebServiceCredential.class, keyMap);
        
        if(ObjectUtils.isNotNull(webServiceCredential)){
            credentialValue = webServiceCredential.getCredentialValue();
        }
        
        return credentialValue; 
    } 

    @Override
    public void updateWebServiceCredentialValue(String credentialKey, String credentialValue) {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY, credentialKey);
        WebServiceCredential webServiceCredential = businessObjectService.findByPrimaryKey(WebServiceCredential.class, keyMap);
        
        if(ObjectUtils.isNotNull(webServiceCredential)){
            webServiceCredential.setCredentialValue(credentialValue);
            businessObjectService.save(webServiceCredential);
        }
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
