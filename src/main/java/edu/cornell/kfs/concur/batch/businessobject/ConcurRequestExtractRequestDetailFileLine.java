package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.ConcurRequestExtractPdpConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailLineValidationResult;

public class ConcurRequestExtractRequestDetailFileLine {
    private Date batchDate;
    private String requestEntryExpenseType;
    private String employeeId;
    private String lastName;
    private String firstName;
    private String middleInitial;
    private KualiDecimal totalApprovedAmount;
    private String employeeGroupId;
    private String payeeIdType;
    private String requestId;
    private KualiDecimal requestAmount;
    private String requestEntryDescription;
    private String expenseReportId;
    private String chart;
    private String accountNumber;
    private String subAccountNumber;
    private String subObjectCode;
    private String projectCode;
    private String orgRefId;
    private List<ConcurRequestExtractRequestEntryDetailFileLine> requestEntryDetails;
    private ConcurRequestExtractRequestDetailLineValidationResult validationResult;
    
    public ConcurRequestExtractRequestDetailFileLine() {
        this.requestEntryDetails = new ArrayList<ConcurRequestExtractRequestEntryDetailFileLine>();
        this.validationResult = new ConcurRequestExtractRequestDetailLineValidationResult();
    }

    public String getRequestEntryExpenseType() {
        return requestEntryExpenseType;
    }

    public void setRequestEntryExpenseType(String requestEntryExpenseType) {
        this.requestEntryExpenseType = requestEntryExpenseType;
    }

    public Date getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(Date batchDate) {
        this.batchDate = batchDate;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public KualiDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(KualiDecimal totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }

    public String getEmployeeGroupId() {
        return employeeGroupId;
    }

    public void setEmployeeGroupId(String employeeGroupId) {
        this.employeeGroupId = employeeGroupId;
    }

    public String getPayeeIdType() {
        return payeeIdType;
    }
    
    public void setPayeeIdType(String payeeIdType) {
        this.payeeIdType = payeeIdType;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public KualiDecimal getRequestAmount() {
        return requestAmount;
    }
    
    public void setRequestAmount(KualiDecimal requestAmount) {
        this.requestAmount = requestAmount;
    }
    
    public String getRequestEntryDescription() {
        return requestEntryDescription;
    }
    
    public void setRequestEntryDescription(String requestEntryDescription) {
        this.requestEntryDescription = requestEntryDescription;
    }
    
    public String getExpenseReportId() {
        return expenseReportId;
    }
    
    public void setExpenseReportId(String expenseReportId) {
        this.expenseReportId = expenseReportId;
    }
    
    public String getChart() {
        return chart;
    }
    
    public void setChart(String chart) {
        this.chart = chart;
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
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public String getOrgRefId() {
        return orgRefId;
    }
    
    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }
    
    public List<ConcurRequestExtractRequestEntryDetailFileLine> getRequestEntryDetails() {
        return requestEntryDetails;
    }
    
    public void setRequestEntryDetails(List<ConcurRequestExtractRequestEntryDetailFileLine> requestEntryDetails) {
        this.requestEntryDetails = requestEntryDetails;
    }

    public ConcurRequestExtractRequestDetailLineValidationResult getValidationResult() {
        return validationResult;
    }

    public void setDetailValidationResult(ConcurRequestExtractRequestDetailLineValidationResult validationResult) {
        this.validationResult = validationResult;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ConcurRequestExtractDetailFileLine:").append(KFSConstants.NEWLINE);
        sb.append("batchDate: ").append(batchDate).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("requestEntryExpenseType: ").append(requestEntryExpenseType).append(KFSConstants.NEWLINE);
        sb.append("employeeId: ").append(employeeId).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("lastName: ").append(lastName).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("firstName: ").append(firstName).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("middleInitial: ").append(middleInitial).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("employeeGroupId: ").append(this.employeeGroupId).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("payeeIdType: ").append(payeeIdType).append(KFSConstants.NEWLINE);
        sb.append("requestId: ").append(requestId).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("totalApprovedAmount: ").append(totalApprovedAmount).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("requestAmount: ").append(requestAmount).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("expenseReportId: ").append(expenseReportId).append(KFSConstants.NEWLINE);
        sb.append("requestEntryDescription: ").append(requestEntryDescription).append(KFSConstants.NEWLINE);
        sb.append("chart: ").append(chart).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("accountNumber: ").append(accountNumber).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("subAccountNumber: ").append(subAccountNumber).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("subObjectCode: ").append(subObjectCode).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("projectCode: ").append(projectCode).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("orgRefId: ").append(orgRefId).append(KFSConstants.NEWLINE);
        sb.append("requestEntryDetails:  ").append(KFSConstants.NEWLINE);
        for (ConcurRequestExtractRequestEntryDetailFileLine entryLines : requestEntryDetails) {
            sb.append(entryLines.toString()).append(KFSConstants.NEWLINE);
        }
        sb.append("cashAdvanceLine: ").append(validationResult.isCashAdvanceLine()).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("cashAdvanceUsedInExpenseReport: ").append(validationResult.isCashAdvanceUsedInExpenseReport()).append(ConcurRequestExtractPdpConstants.WHITESPACE);
        sb.append("validCashAdvanceLine: ").append(validationResult.isValidCashAdvanceLine()).append(KFSConstants.NEWLINE);
        sb.append("validationFailureMessages: ").append(KFSConstants.NEWLINE);
        sb.append(validationResult.getErrorMessagesAsOneFormattedString());
        return sb.toString();
    }

}
