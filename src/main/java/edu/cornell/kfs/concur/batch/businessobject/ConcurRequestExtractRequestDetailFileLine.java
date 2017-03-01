package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurRequestExtractRequestDetailFileLine {
    private boolean lineValid;
    private Date batchDate;
    private String employeeId;
    private String lastName;
    private String firstName;
    private String middleInitial;
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
    
    public ConcurRequestExtractRequestDetailFileLine() {
        this.requestEntryDetails = new ArrayList<ConcurRequestExtractRequestEntryDetailFileLine>();
    }
    
    public boolean isLineValid() {
        return lineValid;
    }
    
    public void setLineValid(boolean lineValid) {
        this.lineValid = lineValid;
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
}
