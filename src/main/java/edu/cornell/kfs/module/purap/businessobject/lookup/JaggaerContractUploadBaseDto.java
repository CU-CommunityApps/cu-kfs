package edu.cornell.kfs.module.purap.businessobject.lookup;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractPartyUploadRowType;

public abstract class JaggaerContractUploadBaseDto {
    private JaggaerContractPartyUploadRowType rowType;
    private String sciQuestID;
    private String active;
    private String ERPNumber;

    public String getERPNumber() {
        return ERPNumber;
    }

    public void setERPNumber(String eRPNumber) {
        ERPNumber = eRPNumber;
    }

    public JaggaerContractPartyUploadRowType getRowType() {
        return rowType;
    }

    public void setRowType(JaggaerContractPartyUploadRowType rowType) {
        this.rowType = rowType;
    }

    public String getSciQuestID() {
        return sciQuestID;
    }

    public void setSciQuestID(String sciQuestID) {
        this.sciQuestID = sciQuestID;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

}
