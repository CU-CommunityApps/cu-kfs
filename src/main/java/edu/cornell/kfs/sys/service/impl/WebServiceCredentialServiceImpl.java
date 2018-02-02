package edu.cornell.kfs.sys.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class WebServiceCredentialServiceImpl implements WebServiceCredentialService {
    protected BusinessObjectService businessObjectService;

    @Override
    public String getWebServiceCredentialValue(String credentialGroupCode, String credentialKey) {
        String credentialValue = StringUtils.EMPTY;
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE, credentialGroupCode);
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY, credentialKey);
        keyMap.put(KFSPropertyConstants.ACTIVE, KFSConstants.ParameterValues.YES);
        Collection<WebServiceCredential> webServiceCredentials = businessObjectService.findMatching(WebServiceCredential.class, keyMap);

        if (webServiceCredentials.size() > 0) {
            credentialValue = webServiceCredentials.iterator().next().getCredentialValue();
        }

        return credentialValue; 
    } 

    @Override
    public void updateWebServiceCredentialValue(String credentialGroupCode, String credentialKey, String credentialValue) {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE, credentialGroupCode);
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY, credentialKey);
        keyMap.put(KFSPropertyConstants.ACTIVE, KFSConstants.ParameterValues.YES);
        Collection<WebServiceCredential> webServiceCredentials = businessObjectService.findMatching(WebServiceCredential.class, keyMap);

        if (webServiceCredentials.size() > 0) {
            WebServiceCredential webServiceCredential = webServiceCredentials.iterator().next();
            webServiceCredential.setCredentialValue(credentialValue);
            businessObjectService.save(webServiceCredential);
        }
    }

    @Override
    public Collection<WebServiceCredential> getWebServiceCredentialsByGroupCode(String credentialGroupCode) {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE, credentialGroupCode);
        keyMap.put(KFSPropertyConstants.ACTIVE, KFSConstants.ParameterValues.YES);
        Collection<WebServiceCredential> webServiceCredentials = businessObjectService.findMatching(WebServiceCredential.class, keyMap);

        return webServiceCredentials;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
