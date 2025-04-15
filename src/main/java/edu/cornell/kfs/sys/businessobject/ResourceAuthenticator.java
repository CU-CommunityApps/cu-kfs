package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

public class ResourceAuthenticator extends PersistableBusinessObjectBase implements MutableInactivatable {
    protected Integer authenticatorId;
    protected String authenticatorDescrtion;
    protected String userNamePassword;
    protected boolean active = true;
    protected List<ResourceDescription> resourceDescriptions;

    public ResourceAuthenticator() {
        resourceDescriptions = new ArrayList<ResourceDescription>();
    }

    public Integer getAuthenticatorId() {
        return authenticatorId;
    }

    public void setAuthenticatorId(Integer authenticatorId) {
        this.authenticatorId = authenticatorId;
    }

    public String getAuthenticatorDescrtion() {
        return authenticatorDescrtion;
    }

    public void setAuthenticatorDescrtion(String authenticatorDescrtion) {
        this.authenticatorDescrtion = authenticatorDescrtion;
    }

    public String getUserNamePassword() {
        return userNamePassword;
    }

    public void setUserNamePassword(String userNamePassword) {
        this.userNamePassword = userNamePassword;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ResourceDescription> getResourceDescriptions() {
        return resourceDescriptions;
    }

    public void setResourceDescriptions(List<ResourceDescription> resourceDescriptions) {
        this.resourceDescriptions = resourceDescriptions;
    }
}
