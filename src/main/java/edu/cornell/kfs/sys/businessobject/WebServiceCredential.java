package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class WebServiceCredential extends PersistableBusinessObjectBase{
    protected String credentialKey;
    protected String credentialValue;
    
    public String getCredentialKey() {
        return credentialKey;
    }
    
    public void setCredentialKey(String credentialKey) {
        this.credentialKey = credentialKey;
    }
    
    public String getCredentialValue() {
        return credentialValue;
    }
    
    public void setCredentialValue(String credentialValue) {
        this.credentialValue = credentialValue;
    }
}
