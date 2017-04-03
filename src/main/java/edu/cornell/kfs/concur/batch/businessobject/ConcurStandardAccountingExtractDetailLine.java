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
    private String employeeMiddleInitital; 
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

    public String getEmployeeMiddleInitital() {
        return employeeMiddleInitital;
    }

    public void setEmployeeMiddleInitital(String employeeMiddleInitital) {
        this.employeeMiddleInitital = employeeMiddleInitital;
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

    public String getDebugInformation() {
        StringBuilder sb = new StringBuilder(" batchID: ").append(batchID).append(" batchDate: ").append(batchDate);
        sb.append(" sequenceNumber: ").append(sequenceNumber).append(" employeeId: ").append(employeeId);
        sb.append(" employeeLastName: ").append(employeeLastName).append(" employeeFirstName: ").append(employeeFirstName);
        sb.append(" employeeMiddleInitital: ").append(employeeMiddleInitital).append(" employeeGroupId: ").append(employeeGroupId);
        sb.append(" reportId: ").append(reportId).append(" employeeStatus: ").append(employeeStatus);
        sb.append(" paymentCode: ").append(paymentCode).append(" journalAccountCode: ").append(journalAccountCode);
        sb.append(" journalAccountCodeOverridden ").append(journalAccountCodeOverridden);
        sb.append(" chartOfAccountsCode: ").append(chartOfAccountsCode).append(" accountNumber: ").append(accountNumber);
        sb.append(" subAccountNumber: ").append(subAccountNumber).append(" subObjectCode: ").append(subObjectCode);
        sb.append(" orgRefId: ").append(orgRefId).append(" projectCode: ").append(projectCode);
        sb.append(" jounalDebitCredit: ").append(jounalDebitCredit).append(" journalAmount: ").append(journalAmount);
        sb.append(" reportEndDate: ").append(reportEndDate).append(" policy: ").append(policy);
        sb.append(" expenseType: ").append(expenseType);
        return sb.toString();
    }

}
