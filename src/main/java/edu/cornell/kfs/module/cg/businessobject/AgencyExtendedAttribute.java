package edu.cornell.kfs.module.cg.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class AgencyExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    private static final long serialVersionUID = 2063791496740952591L;

    private String agencyNumber;
    private String agencyCommonName;
    private String agencyOriginCode;

    private AgencyOrigin agencyOrigin;

    public String getAgencyNumber() {
        return agencyNumber;
    }

    public void setAgencyNumber(String agencyNumber) {
        this.agencyNumber = agencyNumber;
    }

    public String getAgencyCommonName() {
        return agencyCommonName;
    }

    public void setAgencyCommonName(String agencyCommonName) {
        this.agencyCommonName = agencyCommonName;
    }

    public String getAgencyOriginCode() {
        return agencyOriginCode;
    }

    public void setAgencyOriginCode(String agencyOriginCode) {
        this.agencyOriginCode = agencyOriginCode;
    }

    public AgencyOrigin getAgencyOrigin() {
        return agencyOrigin;
    }

    public void setAgencyOrigin(AgencyOrigin agencyOrigin) {
        this.agencyOrigin = agencyOrigin;
    }

}
