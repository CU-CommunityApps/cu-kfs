package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.Country;
import edu.cornell.kfs.sys.businessobject.ISOCountry;

/**
 * CU Generic ISO-FIPS Country modification
 */
public class ISOFIPSCountryMap extends PersistableBusinessObjectBase implements MutableInactivatable {
	
	private static final long serialVersionUID = -225492256254471124L;
	
	private String isoCountryCode;
	private String fipsCountryCode;
	private boolean active;
	private Country fipsCountry;
	private ISOCountry isoCountry;
	
	public ISOFIPSCountryMap() {
	    super();
	}
	
	public ISOFIPSCountryMap(String isoCountryCode, String fipsCountryCode, boolean active, Country fipsCountry,
            ISOCountry isoCountry) {
        super();
        this.isoCountryCode = isoCountryCode;
        this.fipsCountryCode = fipsCountryCode;
        this.active = active;
        this.fipsCountry = fipsCountry;
        this.isoCountry = isoCountry;
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

    public Country getFipsCountry() {
        return fipsCountry;
    }

    public void setFipsCountry(Country fipsCountry) {
        this.fipsCountry = fipsCountry;
    }

    public ISOCountry getIsoCountry() {
        return isoCountry;
    }

    public void setIsoCountry(ISOCountry isoCountry) {
        this.isoCountry = isoCountry;
    }
	
}
