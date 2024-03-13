package edu.cornell.kfs.kim.impl.identity;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PersonAffiliation extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String principalId;
    private String affiliationTypeCode;
    private String affiliationStatus;
    private boolean primary;

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getAffiliationTypeCode() {
        return affiliationTypeCode;
    }

    public void setAffiliationTypeCode(String affiliationTypeCode) {
        this.affiliationTypeCode = affiliationTypeCode;
    }

    public String getAffiliationStatus() {
        return affiliationStatus;
    }

    public void setAffiliationStatus(String affiliationStatus) {
        this.affiliationStatus = affiliationStatus;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

}
