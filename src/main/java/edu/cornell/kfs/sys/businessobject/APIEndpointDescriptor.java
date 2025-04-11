package edu.cornell.kfs.sys.businessobject;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class APIEndpointDescriptor extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String apiCode;
    private String apiDescription;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getApiCode() {
        return apiCode;
    }
    
    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }
    
    public String getApiDescription() {
        return apiDescription;
    }
    
    public void setApiDescription(String apiDescription) {
        this.apiDescription = apiDescription;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof APIEndpointDescriptor)) {
            return false;
        }
        APIEndpointDescriptor other = (APIEndpointDescriptor) obj;
        return new EqualsBuilder()
                .append(this.id, other.getId())
                .append(this.apiCode, other.getApiCode())
                .append(this.apiDescription, other.getApiDescription())
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(37, 41)
                .append(this.id)
                .append(this.apiCode)
                .append(this.apiDescription)
                .toHashCode();
    }
}
