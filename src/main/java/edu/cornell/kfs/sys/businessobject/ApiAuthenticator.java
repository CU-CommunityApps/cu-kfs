package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ApiAuthenticator extends PersistableBusinessObjectBase implements MutableInactivatable {

    private Integer authenticatorId;
    private String authenticatorDescription;
    private String usernamePassword;
    private boolean active;

    private List<ApiEndpointAuthenticator> authenticationMappings;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getAuthenticatorId() {
        return authenticatorId;
    }

    public void setAuthenticatorId(Integer authenticatorId) {
        this.authenticatorId = authenticatorId;
    }

    public String getAuthenticatorDescription() {
        return authenticatorDescription;
    }

    public void setAuthenticatorDescription(String authenticatorDescription) {
        this.authenticatorDescription = authenticatorDescription;
    }

    public String getUsernamePassword() {
        return usernamePassword;
    }

    public void setUsernamePassword(String usernamePassword) {
        this.usernamePassword = usernamePassword;
    }

    public List<ApiEndpointAuthenticator> getAuthenticationMappings() {
        if (authenticationMappings == null) {
            authenticationMappings = new ArrayList<>();
        }
        return authenticationMappings;
    }

    public void setAuthenticationMappings(List<ApiEndpointAuthenticator> authenticationMappings) {
        this.authenticationMappings = authenticationMappings;
    }

}
