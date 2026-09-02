package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiAwardBudgetDataBo extends TransientBusinessObjectBase {

    // Submit Award File Budget data for every award line
    private String proposalNumberUsedForDataRow;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String awardLineDataRowId;
    private String specialConditionDataRowId;
    
    private String awardBudgetDataRowId; // template field name duplicated as rowId
    private String defaultBudgetStructure;
    private String defaultBudgetType;
    private String defaultBalancedAmendment;

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
    
}
