package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class KualiAddressType extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String addressTypeCode;
    private String addressTypeName;
    private Boolean active;

    public KualiAddressType() {
        this.active = true;
    }

     @Override
     public void setActive(boolean active) {
         this.active = active;
     }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return active;
    }

    public String getAddressTypeCode() {
        return addressTypeCode;
    }

    public void setAddressTypeCode(String addressTypeCode) {
        this.addressTypeCode = addressTypeCode;
    }

    public String getAddressTypeName() {
        return addressTypeName;
    }

    public void setAddressTypeName(String addressTypeName) {
        this.addressTypeName = addressTypeName;
    }

}