package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class WebServiceCredential extends PersistableBusinessObjectBase implements MutableInactivatable {
    protected String credentialGroupCode;
    protected String credentialKey;
    protected String credentialValue;
    protected boolean active = true;

    public String getCredentialGroupCode() {
        return credentialGroupCode;
    }

    public void setCredentialGroupCode(String credentialGroupCode) {
        this.credentialGroupCode = credentialGroupCode;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
