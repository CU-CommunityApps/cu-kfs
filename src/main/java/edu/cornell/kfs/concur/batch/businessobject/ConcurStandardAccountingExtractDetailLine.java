package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurStandardAccountingExtractDetailLine {
    
    private String batchID; 
    private Date batchDate; 
    private String sequenceNumber; 
    private String employeeId; 
    private String employeeLastName; 
    private String employeeFirstName; 
    private String employeeMiddleInitial; 
    private String employeeGroupId; 
    private String reportId; 
    private String employeeStatus; 
    private String paymentCode; 
    private String journalAccountCode; 
    private Boolean journalAccountCodeOverridden;
    private String chartOfAccountsCode;
    private String accountNumber; 
    private String subAccountNumber; 
    private String subObjectCode; 
    private String orgRefId; 
    private String projectCode; 
    private String jounalDebitCredit;
    private KualiDecimal journalAmount;
    private String journalAmountString;
    private Date reportEndDate;
    private String policy;
    private String expenseType;
    private String cashAdvanceKey;
    private String reportEntryId;
    private Boolean reportEntryIsPersonalFlag;
    private String reportChartOfAccountsCode;
    private String reportAccountNumber;
    private String reportSubAccountNumber;
    private String reportSubObjectCode;
    private String reportProjectCode;
    private String reportOrgRefId;
    
    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeMiddleInitial() {
        return employeeMiddleInitial;
    }

    public void setEmployeeMiddleInitial(String employeeMiddleInitial) {
        this.employeeMiddleInitial = employeeMiddleInitial;
    }

    public String getEmployeeGroupId() {
        return employeeGroupId;
    }

    public void setEmployeeGroupId(String employeeGroupId) {
        this.employeeGroupId = employeeGroupId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public String getJournalAccountCode() {
        return journalAccountCode;
    }

    public void setJournalAccountCode(String journalAccountCode) {
        this.journalAccountCode = journalAccountCode;
    }

    public Boolean getJournalAccountCodeOverridden() {
        return journalAccountCodeOverridden;
    }

    public void setJournalAccountCodeOverridden(Boolean journalAccountCodeOverridden) {
        this.journalAccountCodeOverridden = journalAccountCodeOverridden;
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

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getJounalDebitCredit() {
        return jounalDebitCredit;
    }

    public void setJounalDebitCredit(String jounalDebitCredit) {
        this.jounalDebitCredit = jounalDebitCredit;
    }

    public KualiDecimal getJournalAmount() {
        return journalAmount;
    }

    public void setJournalAmount(KualiDecimal journalAmount) {
        this.journalAmount = journalAmount;
    }

    public String getJournalAmountString() {
        return journalAmountString;
    }

    public void setJournalAmountString(String journalAmountString) {
        this.journalAmountString = journalAmountString;
    }

    public Date getReportEndDate() {
        return reportEndDate;
    }

    public void setReportEndDate(Date reportEndDate) {
        this.reportEndDate = reportEndDate;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getCashAdvanceKey() {
        return cashAdvanceKey;
    }

    public void setCashAdvanceKey(String cashAdvanceKey) {
        this.cashAdvanceKey = cashAdvanceKey;
    }

    public String getReportEntryId() {
        return reportEntryId;
    }

    public void setReportEntryId(String reportEntryId) {
        this.reportEntryId = reportEntryId;
    }

    public Boolean getReportEntryIsPersonalFlag() {
        return reportEntryIsPersonalFlag;
    }

    public void setReportEntryIsPersonalFlag(Boolean reportEntryIsPersonalFlag) {
        this.reportEntryIsPersonalFlag = reportEntryIsPersonalFlag;
    }

    public String getReportChartOfAccountsCode() {
        return reportChartOfAccountsCode;
    }

    public void setReportChartOfAccountsCode(String reportChartOfAccountsCode) {
        this.reportChartOfAccountsCode = reportChartOfAccountsCode;
    }

    public String getReportAccountNumber() {
        return reportAccountNumber;
    }

    public void setReportAccountNumber(String reportAccountNumber) {
        this.reportAccountNumber = reportAccountNumber;
    }

    public String getReportSubAccountNumber() {
        return reportSubAccountNumber;
    }

    public void setReportSubAccountNumber(String reportSubAccountNumber) {
        this.reportSubAccountNumber = reportSubAccountNumber;
    }

    public String getReportSubObjectCode() {
        return reportSubObjectCode;
    }

    public void setReportSubObjectCode(String reportSubObjectCode) {
        this.reportSubObjectCode = reportSubObjectCode;
    }

    public String getReportProjectCode() {
        return reportProjectCode;
    }

    public void setReportProjectCode(String reportProjectCode) {
        this.reportProjectCode = reportProjectCode;
    }

    public String getReportOrgRefId() {
        return reportOrgRefId;
    }

    public void setReportOrgRefId(String reportOrgRefId) {
        this.reportOrgRefId = reportOrgRefId;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(" batchID: ").append(batchID).append(" batchDate: ").append(batchDate);
        sb.append(" sequenceNumber: ").append(sequenceNumber).append(" employeeId: ").append(employeeId);
        sb.append(" employeeLastName: ").append(employeeLastName).append(" employeeFirstName: ").append(employeeFirstName);
        sb.append(" employeeMiddleInitial: ").append(employeeMiddleInitial).append(" employeeGroupId: ").append(employeeGroupId);
        sb.append(" reportId: ").append(reportId).append(" reportEntryId: ").append(reportEntryId);
        sb.append(" reportEntryIsPersonalFlag: ").append(reportEntryIsPersonalFlag).append(" employeeStatus: ").append(employeeStatus);
        sb.append(" paymentCode: ").append(paymentCode).append(" journalAccountCode: ").append(journalAccountCode);
        sb.append(" journalAccountCodeOverridden ").append(journalAccountCodeOverridden);
        sb.append(" chartOfAccountsCode: ").append(chartOfAccountsCode).append(" accountNumber: ").append(accountNumber);
        sb.append(" subAccountNumber: ").append(subAccountNumber).append(" subObjectCode: ").append(subObjectCode);
        sb.append(" orgRefId: ").append(orgRefId).append(" projectCode: ").append(projectCode);
        sb.append(" jounalDebitCredit: ").append(jounalDebitCredit).append(" journalAmount: ").append(journalAmount);
        sb.append(" journalAmountString: ").append(journalAmountString).append(" reportEndDate: ").append(reportEndDate);
        sb.append(" policy: ").append(policy).append(" expenseType: ").append(expenseType);
        sb.append(" reportChartOfAccountsCode: ").append(reportChartOfAccountsCode).append(" reportAccountNumber: ").append(reportAccountNumber);
        sb.append(" reportSubAccountNumber: ").append(reportSubAccountNumber).append(" reportSubObjectCode: ").append(reportSubObjectCode);
        sb.append(" reportProjectCode: ").append(reportProjectCode).append(" reportOrgRefId: ").append(reportOrgRefId);
        sb.append(" cashAdvanceKey: ").append(cashAdvanceKey);
        return sb.toString();
    }

}
