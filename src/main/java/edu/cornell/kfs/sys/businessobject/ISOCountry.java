package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ISOCountry extends PersistableBusinessObjectBase implements MutableInactivatable {
    
    private static final long serialVersionUID = -8693365503723440174L;
    
    private String name;
    private String code;
    private String alternateCode;
    private boolean active;
    
    public ISOCountry() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getAlternateCode() {
        return alternateCode;
    }
    
    public void setAlternateCode(String alternateCode) {
        this.alternateCode = alternateCode;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
}
