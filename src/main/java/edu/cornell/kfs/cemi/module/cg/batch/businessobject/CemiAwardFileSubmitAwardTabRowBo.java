package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiAwardFileSubmitAwardTabRowBo extends CemiIndexedBusinessObjectBase {

    // Attributes to make the data extract spreadsheet row database searchable and
    // identifiable by data extraction run date and legacy data source.
    // Abstract class attributes that are controlled by the CemiOrmDataBuilderBase class
    // and not the business object factory:
    //    private String jobRunDateString;
    //    private Long jobRunRowIndex;
    private String proposalNumberUsedForDataRow; 
    
    // Attributes representing a single data extract spreadsheet row
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
    private String awardCostCenter;               // template field name duplicated as costCenter
    private String awardFund;                     // template field name duplicated as fund
    private String awardProgram;                  // template field name duplicated as program
    private String awardContractOwner;
    private String awardLifecycleStatus;          // template field name duplicated as awardLifeCycleStatus
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
            
    private String awardLineDataRowId;                        // template field name duplicated as rowId
    private String receivableContractLine;
    private String receivableContractLineReferenceId;
    private String lineNumber;
    private String intercompanyAffiliate;
    private String revenueCategory;
    private String awardLineDataAwardLifecycleStatus;         // template field name duplicated as awardLifeCycleStatus
    private String lineType;
    private String spendRestriction;
    private String lineItemDescriptionOverride;
    private String deferredRevenue;
    private String lineStatus;
    private String awardLineDocumentStatus;
    private String primaryGrant;
    private String lineCfdaNumber;
    private String grantId;
    private String lineAmount;
    private String rateAgreement;
    private String costRateType;
    private String exception;
    private String revenueAllocationProfile;
    private String delete;
    private String basisLimit;
    private String basisLimitId;
    private String basisLimitName;
    private String basisLimitAmount;
    private String awardLineStartDate;
    private String awardLineEndDate;
    private String awardLineDescription;
    private String lineInvoiceMemoOverride;
    private String awardLineDataCostCenter;                   // template field name duplicated as costCenter
    private String awardLineDataFund;                         // template field name duplicated as fund
    private String awardLineDataProgram;                      // template field name duplicated as program
    private String awardLineSalaryCap;
    private String awardLineSalaryCapOverride;
    private String subrecipient;
    private String lineFederalAwardIdNumber;
    private String lineBillingNotes;
    private String revenueRecognitionLineNotes;
            
    private String specialConditionDataRowId;                 // template field name duplicated as rowId
    private String specialConditionDataDelete;
    private String specialCondition;
    private String specialConditionReferenceId;
    private String specialConditionType;
    private String specialConditionComment;
            
    private String awardBudgetDataRowId;                      // template field name duplicated as rowId
    private String defaultBudgetStructure;
    private String defaultBudgetType;
    private String defaultBalancedAmendment;
            
    private String nsfCodeAllocationDataRowId;                // template field name duplicated as rowId
    private String nsfCodeAllocationDataDelete;
    private String nsfCodeAllocation;
    private String nsfCodeAllocationId;
    private String nsfCodeAllocationPercentage;
    private String nsfCode;
    
    
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
    
    public void setAwardBillingSequenceNumberFormatSyntaxReference(String awardBillingSequenceNumberFormatSyntaxReference) {
        this.awardBillingSequenceNumberFormatSyntaxReference = awardBillingSequenceNumberFormatSyntaxReference;
    }
    
    public String getCurrentAwardBillingSequenceNumberUsedReference() {
        return currentAwardBillingSequenceNumberUsedReference;
    }
    
    public void setCurrentAwardBillingSequenceNumberUsedReference(String currentAwardBillingSequenceNumberUsedReference) {
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
    
    public String getAwardLineDataRowId() {
        return awardLineDataRowId;
    }
    
    public void setAwardLineDataRowId(String awardLineDataRowId) {
        this.awardLineDataRowId = awardLineDataRowId;
    }
    
    public String getReceivableContractLine() {
        return receivableContractLine;
    }
    
    public void setReceivableContractLine(String receivableContractLine) {
        this.receivableContractLine = receivableContractLine;
    }
    
    public String getReceivableContractLineReferenceId() {
        return receivableContractLineReferenceId;
    }
    
    public void setReceivableContractLineReferenceId(String receivableContractLineReferenceId) {
        this.receivableContractLineReferenceId = receivableContractLineReferenceId;
    }
    
    public String getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getIntercompanyAffiliate() {
        return intercompanyAffiliate;
    }
    
    public void setIntercompanyAffiliate(String intercompanyAffiliate) {
        this.intercompanyAffiliate = intercompanyAffiliate;
    }
    
    public String getRevenueCategory() {
        return revenueCategory;
    }
    
    public void setRevenueCategory(String revenueCategory) {
        this.revenueCategory = revenueCategory;
    }
    
    public String getAwardLineDataAwardLifecycleStatus() {
        return awardLineDataAwardLifecycleStatus;
    }
    
    public void setAwardLineDataAwardLifecycleStatus(String awardLineDataAwardLifecycleStatus) {
        this.awardLineDataAwardLifecycleStatus = awardLineDataAwardLifecycleStatus;
    }
    
    public String getLineType() {
        return lineType;
    }
    
    public void setLineType(String lineType) {
        this.lineType = lineType;
    }
    
    public String getSpendRestriction() {
        return spendRestriction;
    }
    
    public void setSpendRestriction(String spendRestriction) {
        this.spendRestriction = spendRestriction;
    }
    
    public String getLineItemDescriptionOverride() {
        return lineItemDescriptionOverride;
    }
    
    public void setLineItemDescriptionOverride(String lineItemDescriptionOverride) {
        this.lineItemDescriptionOverride = lineItemDescriptionOverride;
    }
    
    public String getDeferredRevenue() {
        return deferredRevenue;
    }
    
    public void setDeferredRevenue(String deferredRevenue) {
        this.deferredRevenue = deferredRevenue;
    }
    
    public String getLineStatus() {
        return lineStatus;
    }
    
    public void setLineStatus(String lineStatus) {
        this.lineStatus = lineStatus;
    }
    
    public String getAwardLineDocumentStatus() {
        return awardLineDocumentStatus;
    }
    
    public void setAwardLineDocumentStatus(String awardLineDocumentStatus) {
        this.awardLineDocumentStatus = awardLineDocumentStatus;
    }
    
    public String getPrimaryGrant() {
        return primaryGrant;
    }
    
    public void setPrimaryGrant(String primaryGrant) {
        this.primaryGrant = primaryGrant;
    }
    
    public String getLineCfdaNumber() {
        return lineCfdaNumber;
    }
    
    public void setLineCfdaNumber(String lineCfdaNumber) {
        this.lineCfdaNumber = lineCfdaNumber;
    }
    
    public String getGrantId() {
        return grantId;
    }
    
    public void setGrantId(String grantId) {
        this.grantId = grantId;
    }
    
    public String getLineAmount() {
        return lineAmount;
    }
    
    public void setLineAmount(String lineAmount) {
        this.lineAmount = lineAmount;
    }
    
    public String getRateAgreement() {
        return rateAgreement;
    }
    
    public void setRateAgreement(String rateAgreement) {
        this.rateAgreement = rateAgreement;
    }
    
    public String getCostRateType() {
        return costRateType;
    }
    
    public void setCostRateType(String costRateType) {
        this.costRateType = costRateType;
    }
    
    public String getException() {
        return exception;
    }
    
    public void setException(String exception) {
        this.exception = exception;
    }
    
    public String getRevenueAllocationProfile() {
        return revenueAllocationProfile;
    }
    
    public void setRevenueAllocationProfile(String revenueAllocationProfile) {
        this.revenueAllocationProfile = revenueAllocationProfile;
    }
    
    public String getDelete() {
        return delete;
    }
    
    public void setDelete(String delete) {
        this.delete = delete;
    }
    
    public String getBasisLimit() {
        return basisLimit;
    }
    
    public void setBasisLimit(String basisLimit) {
        this.basisLimit = basisLimit;
    }
    
    public String getBasisLimitId() {
        return basisLimitId;
    }
    
    public void setBasisLimitId(String basisLimitId) {
        this.basisLimitId = basisLimitId;
    }
    
    public String getBasisLimitName() {
        return basisLimitName;
    }
    
    public void setBasisLimitName(String basisLimitName) {
        this.basisLimitName = basisLimitName;
    }
    
    public String getBasisLimitAmount() {
        return basisLimitAmount;
    }
    
    public void setBasisLimitAmount(String basisLimitAmount) {
        this.basisLimitAmount = basisLimitAmount;
    }
    
    public String getAwardLineStartDate() {
        return awardLineStartDate;
    }
    
    public void setAwardLineStartDate(String awardLineStartDate) {
        this.awardLineStartDate = awardLineStartDate;
    }
    
    public String getAwardLineEndDate() {
        return awardLineEndDate;
    }
    
    public void setAwardLineEndDate(String awardLineEndDate) {
        this.awardLineEndDate = awardLineEndDate;
    }
    
    public String getAwardLineDescription() {
        return awardLineDescription;
    }
    
    public void setAwardLineDescription(String awardLineDescription) {
        this.awardLineDescription = awardLineDescription;
    }
    
    public String getLineInvoiceMemoOverride() {
        return lineInvoiceMemoOverride;
    }
    
    public void setLineInvoiceMemoOverride(String lineInvoiceMemoOverride) {
        this.lineInvoiceMemoOverride = lineInvoiceMemoOverride;
    }
    
    public String getAwardLineDataCostCenter() {
        return awardLineDataCostCenter;
    }
    
    public void setAwardLineDataCostCenter(String awardLineDataCostCenter) {
        this.awardLineDataCostCenter = awardLineDataCostCenter;
    }
    
    public String getAwardLineDataFund() {
        return awardLineDataFund;
    }
    
    public void setAwardLineDataFund(String awardLineDataFund) {
        this.awardLineDataFund = awardLineDataFund;
    }
    
    public String getAwardLineDataProgram() {
        return awardLineDataProgram;
    }
    
    public void setAwardLineDataProgram(String awardLineDataProgram) {
        this.awardLineDataProgram = awardLineDataProgram;
    }
    
    public String getAwardLineSalaryCap() {
        return awardLineSalaryCap;
    }
    
    public void setAwardLineSalaryCap(String awardLineSalaryCap) {
        this.awardLineSalaryCap = awardLineSalaryCap;
    }
    
    public String getAwardLineSalaryCapOverride() {
        return awardLineSalaryCapOverride;
    }
    
    public void setAwardLineSalaryCapOverride(String awardLineSalaryCapOverride) {
        this.awardLineSalaryCapOverride = awardLineSalaryCapOverride;
    }
    
    public String getSubrecipient() {
        return subrecipient;
    }
    
    public void setSubrecipient(String subrecipient) {
        this.subrecipient = subrecipient;
    }
    
    public String getLineFederalAwardIdNumber() {
        return lineFederalAwardIdNumber;
    }
    
    public void setLineFederalAwardIdNumber(String lineFederalAwardIdNumber) {
        this.lineFederalAwardIdNumber = lineFederalAwardIdNumber;
    }
    
    public String getLineBillingNotes() {
        return lineBillingNotes;
    }
    
    public void setLineBillingNotes(String lineBillingNotes) {
        this.lineBillingNotes = lineBillingNotes;
    }
    
    public String getRevenueRecognitionLineNotes() {
        return revenueRecognitionLineNotes;
    }
    
    public void setRevenueRecognitionLineNotes(String revenueRecognitionLineNotes) {
        this.revenueRecognitionLineNotes = revenueRecognitionLineNotes;
    }
    
    public String getSpecialConditionDataRowId() {
        return specialConditionDataRowId;
    }
    
    public void setSpecialConditionDataRowId(String specialConditionDataRowId) {
        this.specialConditionDataRowId = specialConditionDataRowId;
    }
    
    public String getSpecialConditionDataDelete() {
        return specialConditionDataDelete;
    }
    
    public void setSpecialConditionDataDelete(String specialConditionDataDelete) {
        this.specialConditionDataDelete = specialConditionDataDelete;
    }
    
    public String getSpecialCondition() {
        return specialCondition;
    }
    
    public void setSpecialCondition(String specialCondition) {
        this.specialCondition = specialCondition;
    }
    
    public String getSpecialConditionReferenceId() {
        return specialConditionReferenceId;
    }
    
    public void setSpecialConditionReferenceId(String specialConditionReferenceId) {
        this.specialConditionReferenceId = specialConditionReferenceId;
    }
    
    public String getSpecialConditionType() {
        return specialConditionType;
    }
    
    public void setSpecialConditionType(String specialConditionType) {
        this.specialConditionType = specialConditionType;
    }
    
    public String getSpecialConditionComment() {
        return specialConditionComment;
    }
    
    public void setSpecialConditionComment(String specialConditionComment) {
        this.specialConditionComment = specialConditionComment;
    }
    
    public String getAwardBudgetDataRowId() {
        return awardBudgetDataRowId;
    }
    
    public void setAwardBudgetDataRowId(String awardBudgetDataRowId) {
        this.awardBudgetDataRowId = awardBudgetDataRowId;
    }
    
    public String getDefaultBudgetStructure() {
        return defaultBudgetStructure;
    }
    
    public void setDefaultBudgetStructure(String defaultBudgetStructure) {
        this.defaultBudgetStructure = defaultBudgetStructure;
    }
    
    public String getDefaultBudgetType() {
        return defaultBudgetType;
    }
    
    public void setDefaultBudgetType(String defaultBudgetType) {
        this.defaultBudgetType = defaultBudgetType;
    }
    
    public String getDefaultBalancedAmendment() {
        return defaultBalancedAmendment;
    }
    
    public void setDefaultBalancedAmendment(String defaultBalancedAmendment) {
        this.defaultBalancedAmendment = defaultBalancedAmendment;
    }
    
    public String getNsfCodeAllocationDataRowId() {
        return nsfCodeAllocationDataRowId;
    }
    
    public void setNsfCodeAllocationDataRowId(String nsfCodeAllocationDataRowId) {
        this.nsfCodeAllocationDataRowId = nsfCodeAllocationDataRowId;
    }
    
    public String getNsfCodeAllocationDataDelete() {
        return nsfCodeAllocationDataDelete;
    }
    
    public void setNsfCodeAllocationDataDelete(String nsfCodeAllocationDataDelete) {
        this.nsfCodeAllocationDataDelete = nsfCodeAllocationDataDelete;
    }
    
    public String getNsfCodeAllocation() {
        return nsfCodeAllocation;
    }
    
    public void setNsfCodeAllocation(String nsfCodeAllocation) {
        this.nsfCodeAllocation = nsfCodeAllocation;
    }
    
    public String getNsfCodeAllocationId() {
        return nsfCodeAllocationId;
    }
    
    public void setNsfCodeAllocationId(String nsfCodeAllocationId) {
        this.nsfCodeAllocationId = nsfCodeAllocationId;
    }
    
    public String getNsfCodeAllocationPercentage() {
        return nsfCodeAllocationPercentage;
    }
    
    public void setNsfCodeAllocationPercentage(String nsfCodeAllocationPercentage) {
        this.nsfCodeAllocationPercentage = nsfCodeAllocationPercentage;
    }
    
    public String getNsfCode() {
        return nsfCode;
    }
    
    public void setNsfCode(String nsfCode) {
        this.nsfCode = nsfCode;
    }

}
