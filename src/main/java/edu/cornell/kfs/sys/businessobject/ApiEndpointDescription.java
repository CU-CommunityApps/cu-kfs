package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ApiEndpointDescription extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String endpointCode;
    private String endpointDescription;
    private boolean active;

    private List<ApiAuthenticationMapping> authenticationMappings;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEndpointCode() {
        return endpointCode;
    }

    public void setEndpointCode(String endpointCode) {
        this.endpointCode = endpointCode;
    }

    public String getEndpointDescription() {
        return endpointDescription;
    }

    public void setEndpointDescription(String endpointDescription) {
        this.endpointDescription = endpointDescription;
    }

    public List<ApiAuthenticationMapping> getAuthenticationMappings() {
        if (authenticationMappings == null) {
            authenticationMappings = new ArrayList<>();
        }
        return authenticationMappings;
    }

    public void setAuthenticationMappings(List<ApiAuthenticationMapping> authenticationMappings) {
        this.authenticationMappings = authenticationMappings;
    }

}
