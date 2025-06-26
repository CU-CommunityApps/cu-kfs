package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;

public class ApiAuthenticationMapping extends PersistableBusinessObjectBase implements MutableInactivatable {

    private Integer id;
    private Integer authenticatorId;
    private String endpointCode;
    private boolean active;

    private ApiEndpointDescriptor apiEndpointDescriptor;
    private ApiAuthenticator apiAuthenticator;

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

    public String getEndpointCode() {
        return endpointCode;
    }

    public void setEndpointCode(String endpointCode) {
        this.endpointCode = endpointCode;
    }

    public ApiEndpointDescriptor getApiEndpointDescriptor() {
        return apiEndpointDescriptor;
    }

    public void setApiEndpointDescriptor(ApiEndpointDescriptor apiEndpointDescriptor) {
        this.apiEndpointDescriptor = apiEndpointDescriptor;
        if (ObjectUtils.isNotNull(apiEndpointDescriptor)) {
            this.endpointCode = apiEndpointDescriptor.getEndpointCode();
        }
    }

    public ApiAuthenticator getApiAuthenticator() {
        return apiAuthenticator;
    }

    public void setApiAuthenticator(ApiAuthenticator apiAuthenticator) {
        this.apiAuthenticator = apiAuthenticator;
        if (ObjectUtils.isNotNull(apiAuthenticator)) {
            this.authenticatorId = apiAuthenticator.getAuthenticatorId();
        }
    }

}
