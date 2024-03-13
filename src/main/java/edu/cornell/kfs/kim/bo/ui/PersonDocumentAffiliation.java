package edu.cornell.kfs.kim.bo.ui;

import org.kuali.kfs.kim.bo.ui.KimDocumentBoBase;

public class PersonDocumentAffiliation extends KimDocumentBoBase {

    private static final long serialVersionUID = 1L;

    private String affiliationTypeCode;
    private String affiliationStatus;
    private boolean primary;

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
