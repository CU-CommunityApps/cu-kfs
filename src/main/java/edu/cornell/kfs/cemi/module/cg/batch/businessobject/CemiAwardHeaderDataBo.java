package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiAwardHeaderDataBo extends TransientBusinessObjectBase {

    /* Submit Award File Award Header Data */
    
    private String proposalNumberUsedForDataRow;

    private String spreadsheetKey;
    private String addOnly;
    private String award;
    private String autoComplete;
    private String awardReferenceId;
    private String awardNumber;
    private String company;
    private String sponsorAwardReferenceNumber;
    private String awardName;
    private String awardDescription;
    private String awardEffectiveDate;
    private String awardSignedDate;
    private String awardType;
    private String purposeCode;
    private String paymentTerms;
    private String institutionalId;
    private String awardSalaryCapDefault;
    private String spendRestrictionDefault;
    private String relatedAward;
    private String awardGroup;
    private String awardCostCenter; // template field name duplicated as costCenter
    private String awardFund; // template field name duplicated as fund
    private String awardProgram; // template field name duplicated as program
    private String awardContractOwner;
    private String awardLifecycleStatus; // template field name duplicated as awardLifeCycleStatus
    private String subAward;
    private String primeSponsor;
    private String sponsor;
    private String billToSponsor;
    private String paymentType;
    private String letterOfCredit;
    private String letterOfCreditDocumentId;
    private String awardSequenceBillingActiveReference;
    private String awardBillingSequenceNumberFormatSyntaxReference;
    private String currentAwardBillingSequenceNumberUsedReference;
    private String awardBillingSequenceGeneratorRule;
    private String currency;
    private String zeroAmountAward;
    private String sponsorDirectCostAmount;
    private String sponsorFacilitiesAndAdministrationAmount;
    private String costShareTotalAmount;
    private String authorizedAmount;
    private String billingLimitOverride;
    private String costShareRequiredBySponsor;
    private String anticipatedSponsorDirectCostAmount;
    private String anticipatedFacilitiesAndAdministrationAmount;
    private String awardSchedule;
    private String federalAwardIdNumber;
    private String cfdaNumber;
    private String proposalId;
    private String proposalVersion;
    private String originalProposal;
    private String awardAmendmentReason;
    private String awardAmendmentEffectiveDate;
    private String awardNotes;
    private String billingNotes;

    public String getProposalNumberUsedForDataRow() {
        return proposalNumberUsedForDataRow;
    }

    public void setProposalNumberUsedForDataRow(String proposalNumberUsedForDataRow) {
        this.proposalNumberUsedForDataRow = proposalNumberUsedForDataRow;
    }

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAddOnly() {
        return addOnly;
    }

    public void setAddOnly(String addOnly) {
        this.addOnly = addOnly;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
    }

    public String getAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(String autoComplete) {
        this.autoComplete = autoComplete;
    }

    public String getAwardReferenceId() {
        return awardReferenceId;
    }

    public void setAwardReferenceId(String awardReferenceId) {
        this.awardReferenceId = awardReferenceId;
    }

    public String getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSponsorAwardReferenceNumber() {
        return sponsorAwardReferenceNumber;
    }

    public void setSponsorAwardReferenceNumber(String sponsorAwardReferenceNumber) {
        this.sponsorAwardReferenceNumber = sponsorAwardReferenceNumber;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    public String getAwardDescription() {
        return awardDescription;
    }

    public void setAwardDescription(String awardDescription) {
        this.awardDescription = awardDescription;
    }

    public String getAwardEffectiveDate() {
        return awardEffectiveDate;
    }

    public void setAwardEffectiveDate(String awardEffectiveDate) {
        this.awardEffectiveDate = awardEffectiveDate;
    }

    public String getAwardSignedDate() {
        return awardSignedDate;
    }

    public void setAwardSignedDate(String awardSignedDate) {
        this.awardSignedDate = awardSignedDate;
    }

    public String getAwardType() {
        return awardType;
    }

    public void setAwardType(String awardType) {
        this.awardType = awardType;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getInstitutionalId() {
        return institutionalId;
    }

    public void setInstitutionalId(String institutionalId) {
        this.institutionalId = institutionalId;
    }

    public String getAwardSalaryCapDefault() {
        return awardSalaryCapDefault;
    }

    public void setAwardSalaryCapDefault(String awardSalaryCapDefault) {
        this.awardSalaryCapDefault = awardSalaryCapDefault;
    }

    public String getSpendRestrictionDefault() {
        return spendRestrictionDefault;
    }

    public void setSpendRestrictionDefault(String spendRestrictionDefault) {
        this.spendRestrictionDefault = spendRestrictionDefault;
    }

    public String getRelatedAward() {
        return relatedAward;
    }

    public void setRelatedAward(String relatedAward) {
        this.relatedAward = relatedAward;
    }

    public String getAwardGroup() {
        return awardGroup;
    }

    public void setAwardGroup(String awardGroup) {
        this.awardGroup = awardGroup;
    }

    public String getAwardCostCenter() {
        return awardCostCenter;
    }

    public void setAwardCostCenter(String awardCostCenter) {
        this.awardCostCenter = awardCostCenter;
    }

    public String getAwardFund() {
        return awardFund;
    }

    public void setAwardFund(String awardFund) {
        this.awardFund = awardFund;
    }

    public String getAwardProgram() {
        return awardProgram;
    }

    public void setAwardProgram(String awardProgram) {
        this.awardProgram = awardProgram;
    }

    public String getAwardContractOwner() {
        return awardContractOwner;
    }

    public void setAwardContractOwner(String awardContractOwner) {
        this.awardContractOwner = awardContractOwner;
    }

    public String getAwardLifecycleStatus() {
        return awardLifecycleStatus;
    }

    public void setAwardLifecycleStatus(String awardLifecycleStatus) {
        this.awardLifecycleStatus = awardLifecycleStatus;
    }

    public String getSubAward() {
        return subAward;
    }

    public void setSubAward(String subAward) {
        this.subAward = subAward;
    }

    public String getPrimeSponsor() {
        return primeSponsor;
    }

    public void setPrimeSponsor(String primeSponsor) {
        this.primeSponsor = primeSponsor;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getBillToSponsor() {
        return billToSponsor;
    }

    public void setBillToSponsor(String billToSponsor) {
        this.billToSponsor = billToSponsor;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getLetterOfCredit() {
        return letterOfCredit;
    }

    public void setLetterOfCredit(String letterOfCredit) {
        this.letterOfCredit = letterOfCredit;
    }

    public String getLetterOfCreditDocumentId() {
        return letterOfCreditDocumentId;
    }

    public void setLetterOfCreditDocumentId(String letterOfCreditDocumentId) {
        this.letterOfCreditDocumentId = letterOfCreditDocumentId;
    }

    public String getAwardSequenceBillingActiveReference() {
        return awardSequenceBillingActiveReference;
    }

    public void setAwardSequenceBillingActiveReference(String awardSequenceBillingActiveReference) {
        this.awardSequenceBillingActiveReference = awardSequenceBillingActiveReference;
    }

    public String getAwardBillingSequenceNumberFormatSyntaxReference() {
        return awardBillingSequenceNumberFormatSyntaxReference;
    }

    public void setAwardBillingSequenceNumberFormatSyntaxReference(
            String awardBillingSequenceNumberFormatSyntaxReference) {
        this.awardBillingSequenceNumberFormatSyntaxReference = awardBillingSequenceNumberFormatSyntaxReference;
    }

    public String getCurrentAwardBillingSequenceNumberUsedReference() {
        return currentAwardBillingSequenceNumberUsedReference;
    }

    public void setCurrentAwardBillingSequenceNumberUsedReference(
            String currentAwardBillingSequenceNumberUsedReference) {
        this.currentAwardBillingSequenceNumberUsedReference = currentAwardBillingSequenceNumberUsedReference;
    }

    public String getAwardBillingSequenceGeneratorRule() {
        return awardBillingSequenceGeneratorRule;
    }

    public void setAwardBillingSequenceGeneratorRule(String awardBillingSequenceGeneratorRule) {
        this.awardBillingSequenceGeneratorRule = awardBillingSequenceGeneratorRule;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getZeroAmountAward() {
        return zeroAmountAward;
    }

    public void setZeroAmountAward(String zeroAmountAward) {
        this.zeroAmountAward = zeroAmountAward;
    }

    public String getSponsorDirectCostAmount() {
        return sponsorDirectCostAmount;
    }

    public void setSponsorDirectCostAmount(String sponsorDirectCostAmount) {
        this.sponsorDirectCostAmount = sponsorDirectCostAmount;
    }

    public String getSponsorFacilitiesAndAdministrationAmount() {
        return sponsorFacilitiesAndAdministrationAmount;
    }

    public void setSponsorFacilitiesAndAdministrationAmount(String sponsorFacilitiesAndAdministrationAmount) {
        this.sponsorFacilitiesAndAdministrationAmount = sponsorFacilitiesAndAdministrationAmount;
    }

    public String getCostShareTotalAmount() {
        return costShareTotalAmount;
    }

    public void setCostShareTotalAmount(String costShareTotalAmount) {
        this.costShareTotalAmount = costShareTotalAmount;
    }

    public String getAuthorizedAmount() {
        return authorizedAmount;
    }

    public void setAuthorizedAmount(String authorizedAmount) {
        this.authorizedAmount = authorizedAmount;
    }

    public String getBillingLimitOverride() {
        return billingLimitOverride;
    }

    public void setBillingLimitOverride(String billingLimitOverride) {
        this.billingLimitOverride = billingLimitOverride;
    }

    public String getCostShareRequiredBySponsor() {
        return costShareRequiredBySponsor;
    }

    public void setCostShareRequiredBySponsor(String costShareRequiredBySponsor) {
        this.costShareRequiredBySponsor = costShareRequiredBySponsor;
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

    public String getAwardSchedule() {
        return awardSchedule;
    }

    public void setAwardSchedule(String awardSchedule) {
        this.awardSchedule = awardSchedule;
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

    public String getProposalId() {
        return proposalId;
    }

    public void setProposalId(String proposalId) {
        this.proposalId = proposalId;
    }

    public String getProposalVersion() {
        return proposalVersion;
    }

    public void setProposalVersion(String proposalVersion) {
        this.proposalVersion = proposalVersion;
    }

    public String getOriginalProposal() {
        return originalProposal;
    }

    public void setOriginalProposal(String originalProposal) {
        this.originalProposal = originalProposal;
    }

    public String getAwardAmendmentReason() {
        return awardAmendmentReason;
    }

    public void setAwardAmendmentReason(String awardAmendmentReason) {
        this.awardAmendmentReason = awardAmendmentReason;
    }

    public String getAwardAmendmentEffectiveDate() {
        return awardAmendmentEffectiveDate;
    }

    public void setAwardAmendmentEffectiveDate(String awardAmendmentEffectiveDate) {
        this.awardAmendmentEffectiveDate = awardAmendmentEffectiveDate;
    }

    public String getAwardNotes() {
        return awardNotes;
    }

    public void setAwardNotes(String awardNotes) {
        this.awardNotes = awardNotes;
    }

    public String getBillingNotes() {
        return billingNotes;
    }

    public void setBillingNotes(String billingNotes) {
        this.billingNotes = billingNotes;
    }
}