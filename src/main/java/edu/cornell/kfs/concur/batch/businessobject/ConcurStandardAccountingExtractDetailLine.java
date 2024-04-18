package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;

import org.kuali.kfs.sys.KFSConstants;

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
    private String journalDebitCredit;
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
    private String journalPayerPaymentTypeName;
    private String journalPayeePaymentTypeName;
    private String cashAdvancePaymentCodeName;
    private KualiDecimal cashAdvanceAmount;
    private String cashAdvanceTransactionType;
    private String employeeChart;
    private String employeeAccountNumber;
    private String cashAdvanceName;
    private String kfsAssembledRequestId;
    private ConcurSaeRequestedCashAdvanceDetailLineValidationResult validationResult;
    
    public ConcurStandardAccountingExtractDetailLine() {
        this.validationResult = new ConcurSaeRequestedCashAdvanceDetailLineValidationResult();
    }
    
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

    public String getJournalDebitCredit() {
        return journalDebitCredit;
    }

    public void setJournalDebitCredit(String journalDebitCredit) {
        this.journalDebitCredit = journalDebitCredit;
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
        setKfsAssembledRequestId(cashAdvanceKey);
    }
    
    public String getKfsAssembledRequestId() {
        return kfsAssembledRequestId;
    }

    /**
     * Setter visibility purposely made private to ensure value is properly constructed by this business object.
     * @param kfsAssembledRequestId
     */
    private void setKfsAssembledRequestId(String kfsAssembledRequestId) {
        this.kfsAssembledRequestId = new String(ConcurConstants.SAE_REQUEST_ID_PREFIX + kfsAssembledRequestId);
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

    public String getJournalPayerPaymentTypeName() {
        return journalPayerPaymentTypeName;
    }

    public void setJournalPayerPaymentTypeName(String journalPayerPaymentTypeName) {
        this.journalPayerPaymentTypeName = journalPayerPaymentTypeName;
    }

    public String getJournalPayeePaymentTypeName() {
        return journalPayeePaymentTypeName;
    }

    public void setJournalPayeePaymentTypeName(String journalPayeePaymentTypeName) {
        this.journalPayeePaymentTypeName = journalPayeePaymentTypeName;
    }

    public String getCashAdvancePaymentCodeName() {
        return cashAdvancePaymentCodeName;
    }

    public void setCashAdvancePaymentCodeName(String cashAdvancePaymentCodeName) {
        this.cashAdvancePaymentCodeName = cashAdvancePaymentCodeName;
    }

    public KualiDecimal getCashAdvanceAmount() {
        return cashAdvanceAmount;
    }

    public void setCashAdvanceAmount(KualiDecimal cashAdvanceAmount) {
        this.cashAdvanceAmount = cashAdvanceAmount;
    }

    public String getCashAdvanceTransactionType() {
        return cashAdvanceTransactionType;
    }

    public void setCashAdvanceTransactionType(String cashAdvanceTransactionType) {
        this.cashAdvanceTransactionType = cashAdvanceTransactionType;
    }

    public ConcurSaeRequestedCashAdvanceDetailLineValidationResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ConcurSaeRequestedCashAdvanceDetailLineValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public String getEmployeeChart() {
        return employeeChart;
    }

    public void setEmployeeChart(String employeeChart) {
        this.employeeChart = employeeChart;
    }

    public String getEmployeeAccountNumber() {
        return employeeAccountNumber;
    }

    public void setEmployeeAccountNumber(String employeeAccountNumber) {
        this.employeeAccountNumber = employeeAccountNumber;
    }

    public String getCashAdvanceName() {
        return cashAdvanceName;
    }

    public void setCashAdvanceName(String cashAdvanceName) {
        this.cashAdvanceName = cashAdvanceName;
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
        sb.append(" journalDebitCredit: ").append(journalDebitCredit).append(" journalAmount: ").append(journalAmount);
        sb.append(" journalAmountString: ").append(journalAmountString).append(" reportEndDate: ").append(reportEndDate);
        sb.append(" policy: ").append(policy).append(" expenseType: ").append(expenseType);
        sb.append(" reportChartOfAccountsCode: ").append(reportChartOfAccountsCode).append(" reportAccountNumber: ").append(reportAccountNumber);
        sb.append(" reportSubAccountNumber: ").append(reportSubAccountNumber).append(" reportSubObjectCode: ").append(reportSubObjectCode);
        sb.append(" reportProjectCode: ").append(reportProjectCode).append(" reportOrgRefId: ").append(reportOrgRefId);
        sb.append(" cashAdvanceKey: ").append(cashAdvanceKey).append(" cashAdvancePaymentCodeName: ").append(cashAdvancePaymentCodeName);
        sb.append(" journalPayerPaymentTypeName: ").append(journalPayerPaymentTypeName);
        sb.append(" journalPayeePaymentTypeName: ").append(journalPayeePaymentTypeName);
        sb.append(" cashAdvanceTransactionType: ").append(cashAdvanceTransactionType);
        sb.append(" cashAdvanceAmount: ").append(cashAdvanceAmount);
        sb.append(" employeeChart: ").append(employeeChart);
        sb.append(" employeeAccountNumber: ").append(employeeAccountNumber);
        sb.append(" cashAdvanceName: ").append(cashAdvanceName);
        sb.append(" kfsAssembledRequestId: ").append(kfsAssembledRequestId);
        sb.append(" cashAdvanceLine: ").append(validationResult.isCashAdvanceLine());
        sb.append(" cashAdvanceAdministratorApprovedCashAdvance: ").append(validationResult.isCashAdvanceAdministratorApprovedCashAdvance());
        sb.append(" cashAdvanceUsedInExpenseReport: ").append(validationResult.isCashAdvanceUsedInExpenseReport());
        sb.append(" validCashAdvanceLine: ").append(validationResult.isValidCashAdvanceLine()).append(KFSConstants.NEWLINE);
        sb.append(" validationFailureMessages: ").append(KFSConstants.NEWLINE);
        sb.append(validationResult.getErrorMessagesAsOneFormattedString());
        return sb.toString();
    }

}
