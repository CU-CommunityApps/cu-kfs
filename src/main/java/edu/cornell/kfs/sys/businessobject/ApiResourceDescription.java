package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ApiResourceDescription extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String resourceCode;
    private String resourceDescription;
    private boolean active;

    private List<ApiResourceDescriptionAuthenticator> descriptionAuthenticators;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public List<ApiResourceDescriptionAuthenticator> getDescriptionAuthenticators() {
        if (descriptionAuthenticators == null) {
            descriptionAuthenticators = new ArrayList<>();
        }
        return descriptionAuthenticators;
    }

    public void setDescriptionAuthenticators(List<ApiResourceDescriptionAuthenticator> descriptionAuthenticators) {
        this.descriptionAuthenticators = descriptionAuthenticators;
    }

}
