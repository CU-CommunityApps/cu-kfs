package edu.cornell.kfs.sys.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class KualiAddressType extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 1L;

    private String addressTypeCode;
    private String addressTypeName;
    private Boolean active;
	
    public KualiAddressType() {

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

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("addressTypeCode", this.addressTypeCode);
        return m;
    }
}




