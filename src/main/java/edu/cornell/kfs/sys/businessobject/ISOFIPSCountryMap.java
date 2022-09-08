package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ISOFIPSCountryMap extends PersistableBusinessObjectBase implements MutableInactivatable {
	
	private static final long serialVersionUID = -225492256254471124L;
	
	private String isoCountryCode;
	private String fipsCountryCode;
	private boolean active;
	
	public ISOFIPSCountryMap() {
	}
	
	public String getIsoCountryCode() {
		return isoCountryCode;
	}
	
	public void setIsoCountryCode(String isoCountryCode) {
		this.isoCountryCode = isoCountryCode;
	}
	
	public String getFipsCountryCode() {
		return fipsCountryCode;
	}
	
	public void setFipsCountryCode(String fipsCountryCode) {
		this.fipsCountryCode = fipsCountryCode;
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
