package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiAwardSpecialConditionDataBo extends TransientBusinessObjectBase {

    // Submit Award File Special Conditions for every award line
    private String proposalNumberUsedForDataRow;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String awardLineDataRowId;

    private String specialConditionDataRowId; // template field name duplicated as rowId
    private String specialConditionDataDelete;
    private String specialCondition;
    private String specialConditionReferenceId;
    private String specialConditionType;
    private String specialConditionComment;

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

}
