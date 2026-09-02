package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class CemiAwardLegacyNovelutionBo extends PersistableBusinessObjectBase {
    
    /* Legacy data from the Novelution system needed for Submit Award data extract. */
    
    private String proposalNumber;
    
    private String spreadsheetKey;
    private String awardSignedDate;
    private String costShareTotalAmount;
    private String anticipatedSponsorDirectCostAmount;
    private String anticipatedFacilitiesAndAdministrationAmount;
    private String federalAwardIdNumber;
    private String cfdaNumber;
    
    public CemiAwardLegacyNovelutionBo(String spreadsheetKey, String awardSignedDate,String costShareTotalAmount,
            String anticipatedSponsorDirectCostAmount, String anticipatedFacilitiesAndAdministrationAmount,
            String federalAwardIdNumber, String cfdaNumber) {
        this.spreadsheetKey = spreadsheetKey;
        this.awardSignedDate = awardSignedDate;
        this.costShareTotalAmount = costShareTotalAmount;
        this.anticipatedSponsorDirectCostAmount = anticipatedSponsorDirectCostAmount;
        this.anticipatedFacilitiesAndAdministrationAmount = anticipatedFacilitiesAndAdministrationAmount;
        this.anticipatedFacilitiesAndAdministrationAmount = anticipatedFacilitiesAndAdministrationAmount;
        this.cfdaNumber = cfdaNumber;
    }
    
    
    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }
    
    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAwardSignedDate() {
        return awardSignedDate;
    }

    public void setAwardSignedDate(String awardSignedDate) {
        this.awardSignedDate = awardSignedDate;
    }

    public String getCostShareTotalAmount() {
        return costShareTotalAmount;
    }

    public void setCostShareTotalAmount(String costShareTotalAmount) {
        this.costShareTotalAmount = costShareTotalAmount;
    }

    public String getAnticipatedSponsorDirectCostAmount() {
        return anticipatedSponsorDirectCostAmount;
    }

    public void setAnticipatedSponsorDirectCostAmount(String anticipatedSponsorDirectCostAmount) {
        this.anticipatedSponsorDirectCostAmount = anticipatedSponsorDirectCostAmount;
    }

    public String getAnticipatedFacilitiesAndAdministrationAmount() {
        return anticipatedFacilitiesAndAdministrationAmount;
    }

    public void setAnticipatedFacilitiesAndAdministrationAmount(String anticipatedFacilitiesAndAdministrationAmount) {
        this.anticipatedFacilitiesAndAdministrationAmount = anticipatedFacilitiesAndAdministrationAmount;
    }

    public String getFederalAwardIdNumber() {
        return federalAwardIdNumber;
    }

    public void setFederalAwardIdNumber(String federalAwardIdNumber) {
        this.federalAwardIdNumber = federalAwardIdNumber;
    }

    public String getCfdaNumber() {
        return cfdaNumber;
    }

    public void setCfdaNumber(String cfdaNumber) {
        this.cfdaNumber = cfdaNumber;
    }
    
}
