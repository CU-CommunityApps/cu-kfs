package edu.cornell.kfs.sys.businessobject;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class APIAuthenticator extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;
    
    private String id;
    private String authenticationValue;
    private List<APIEndpointDescriptor> apiEndpointDescriptors;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAuthenticationValue() {
        return authenticationValue;
    }
    
    public void setAuthenticationValue(String authenticationValue) {
        this.authenticationValue = authenticationValue;
    }
    
    public List<APIEndpointDescriptor> getApiEndpointDescriptors() {
        return apiEndpointDescriptors;
    }
    
    public void setApiEndpointDescriptors(List<APIEndpointDescriptor> apiEndpointDescriptors) {
        this.apiEndpointDescriptors = apiEndpointDescriptors;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof APIAuthenticator)) {
            return false;
        }
        APIAuthenticator other = (APIAuthenticator) obj;
        return new EqualsBuilder()
                .append(this.id, other.getId())
                .append(this.authenticationValue, other.getAuthenticationValue())
                .append(this.apiEndpointDescriptors, other.getApiEndpointDescriptors())
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 41)
                .append(this.id)
                .append(this.authenticationValue)
                .append(this.apiEndpointDescriptors)
                .toHashCode();
    }
}
