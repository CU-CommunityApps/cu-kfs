package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ApiResourceDescriptionAuthenticator extends PersistableBusinessObjectBase implements MutableInactivatable {

    private Integer id;
    private Integer authenticatorId;
    private String resourceCode;
    private boolean active;

    private ApiResourceDescription apiResourceDescription;
    private ApiResourceAuthenticator apiResourceAuthenticator;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAuthenticatorId() {
        return authenticatorId;
    }

    public void setAuthenticatorId(Integer authenticatorId) {
        this.authenticatorId = authenticatorId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public ApiResourceDescription getApiResourceDescription() {
        return apiResourceDescription;
    }

    public void setApiResourceDescription(ApiResourceDescription apiResourceDescription) {
        this.apiResourceDescription = apiResourceDescription;
        this.resourceCode = apiResourceDescription.getResourceCode();
    }

    public ApiResourceAuthenticator getApiResourceAuthenticator() {
        return apiResourceAuthenticator;
    }

    public void setApiResourceAuthenticator(ApiResourceAuthenticator apiResourceAuthenticator) {
        this.apiResourceAuthenticator = apiResourceAuthenticator;
        this.authenticatorId = apiResourceAuthenticator.getAuthenticatorId();
    }

}
