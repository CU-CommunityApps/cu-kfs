package edu.cornell.kfs.module.cg.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;

public class AgencyOrigin extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = 452550276748816849L;

    private String agencyOriginCode;
    private boolean active;

    public String getAgencyOriginCode() {
        return agencyOriginCode;
    }

    public void setAgencyOriginCode(String agencyOriginCode) {
        this.agencyOriginCode = agencyOriginCode;
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
