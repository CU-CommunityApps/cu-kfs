package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiAwardLineDataBo extends TransientBusinessObjectBase {
    
    // Submit Award File single Award Line Data element
    // legacy keys
    private String proposalNumberUsedForDataRow;
    private String chartOfAccountsCode;
    private String accountNumber;

    // Award extract data elements
    private String awardLineDataRowId; // template field name duplicated as rowId
    private String receivableContractLine;
    private String receivableContractLineReferenceId;
    private String lineNumber;
    private String intercompanyAffiliate;
    private String revenueCategory;
    private String awardLineDataAwardLifecycleStatus; // template field name duplicated as awardLifeCycleStatus
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
    private String awardLineDataCostCenter; // template field name duplicated as costCenter
    private String awardLineDataFund; // template field name duplicated as fund
    private String awardLineDataProgram; // template field name duplicated as program
    private String awardLineSalaryCap;
    private String awardLineSalaryCapOverride;
    private String subrecipient;
    private String lineFederalAwardIdNumber;
    private String lineBillingNotes;
    private String revenueRecognitionLineNotes;

    public String getProposalNumberUsedForDataRow() {
        return proposalNumberUsedForDataRow;
    }

    public void setProposalNumberUsedForDataRow(String proposalNumberUsedForDataRow) {
        this.proposalNumberUsedForDataRow = proposalNumberUsedForDataRow;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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

}
