package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseListItemDTO {
    @JsonProperty("Name")
    public String name;
    
    @JsonProperty("Total")
    public double total;
    
    @JsonProperty("CurrencyCode")
    public String currencyCode;
    
    @JsonProperty("Country")
    public String country;
    
    @JsonProperty("CountrySubdivision")
    public String countrySubdivision;
    
    @JsonProperty("CreateDate")
    public Date createDate;
    
    @JsonProperty("SubmitDate")
    public Date submitDate;
    
    @JsonProperty("ProcessingPaymentDate")
    public Object processingPaymentDate;
    
    @JsonProperty("PaidDate")
    public Object paidDate;
    
    @JsonProperty("ReceiptsReceived")
    public boolean receiptsReceived;
    
    @JsonProperty("UserDefinedDate")
    public Date userDefinedDate;
    
    @JsonProperty("LastComment")
    public String lastComment;
    
    @JsonProperty("OwnerLoginID")
    public String ownerLoginID;
    
    @JsonProperty("OwnerName")
    public String ownerName;
    
    @JsonProperty("ApproverLoginID")
    public String approverLoginID;
    
    @JsonProperty("ApproverName")
    public String approverName;
    
    @JsonProperty("ApprovalStatusName")
    public String approvalStatusName;
    
    @JsonProperty("ApprovalStatusCode")
    public String approvalStatusCode;
    
    @JsonProperty("PaymentStatusName")
    public String paymentStatusName;
    
    @JsonProperty("PaymentStatusCode")
    public String paymentStatusCode;
    
    @JsonProperty("LastModifiedDate")
    public Date lastModifiedDate;

    @JsonProperty("PersonalAmount")
    public double personalAmount;
    
    @JsonProperty("AmountDueEmployee")
    public double amountDueEmployee;
    
    @JsonProperty("AmountDueCompanyCard")
    public double amountDueCompanyCard;
    
    @JsonProperty("TotalClaimedAmount")
    public double totalClaimedAmount;
    
    @JsonProperty("TotalApprovedAmount")
    public double totalApprovedAmount;
    
    @JsonProperty("LedgerName")
    public String ledgerName;
    
    @JsonProperty("PolicyID")
    public String policyID;
    
    @JsonProperty("EverSentBack")
    public boolean everSentBack;
    
    @JsonProperty("HasException")
    public boolean hasException;
    
    @JsonProperty("WorkflowActionUrl")
    public String workflowActionUrl;
    
    @JsonProperty("OrgUnit1")
    public ConcurV3ExpenseReportItemDTO chart;
    
    @JsonProperty("OrgUnit2")
    public ConcurV3ExpenseReportItemDTO account;
    
    @JsonProperty("Custom1")
    public ConcurV3ExpenseReportItemDTO travlerType;
    
    @JsonProperty("Custom2")
    public ConcurV3ExpenseReportItemDTO reportTupe;
    
    @JsonProperty("Custom3")
    public ConcurV3ExpenseReportItemDTO businessPurpose;
    
    @JsonProperty("Custom5")
    public ConcurV3ExpenseReportItemDTO departmentOrgCode;
    
    @JsonProperty("Custom8")
    public ConcurV3ExpenseReportItemDTO ftcBsc;
    
    @JsonProperty("Custom9")
    public ConcurV3ExpenseReportItemDTO departmentName;
    
    @JsonProperty("Custom15")
    public ConcurV3ExpenseReportItemDTO userType;
    
    @JsonProperty("ID")
    public String iD;
    
    @JsonProperty("URI")
    public String uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountrySubdivision() {
        return countrySubdivision;
    }

    public void setCountrySubdivision(String countrySubdivision) {
        this.countrySubdivision = countrySubdivision;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public Object getProcessingPaymentDate() {
        return processingPaymentDate;
    }

    public void setProcessingPaymentDate(Object processingPaymentDate) {
        this.processingPaymentDate = processingPaymentDate;
    }

    public Object getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Object paidDate) {
        this.paidDate = paidDate;
    }

    public boolean isReceiptsReceived() {
        return receiptsReceived;
    }

    public void setReceiptsReceived(boolean receiptsReceived) {
        this.receiptsReceived = receiptsReceived;
    }

    public Date getUserDefinedDate() {
        return userDefinedDate;
    }

    public void setUserDefinedDate(Date userDefinedDate) {
        this.userDefinedDate = userDefinedDate;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public String getOwnerLoginID() {
        return ownerLoginID;
    }

    public void setOwnerLoginID(String ownerLoginID) {
        this.ownerLoginID = ownerLoginID;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getApproverLoginID() {
        return approverLoginID;
    }

    public void setApproverLoginID(String approverLoginID) {
        this.approverLoginID = approverLoginID;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApprovalStatusName() {
        return approvalStatusName;
    }

    public void setApprovalStatusName(String approvalStatusName) {
        this.approvalStatusName = approvalStatusName;
    }

    public String getApprovalStatusCode() {
        return approvalStatusCode;
    }

    public void setApprovalStatusCode(String approvalStatusCode) {
        this.approvalStatusCode = approvalStatusCode;
    }

    public String getPaymentStatusName() {
        return paymentStatusName;
    }

    public void setPaymentStatusName(String paymentStatusName) {
        this.paymentStatusName = paymentStatusName;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    public void setPaymentStatusCode(String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public double getPersonalAmount() {
        return personalAmount;
    }

    public void setPersonalAmount(double personalAmount) {
        this.personalAmount = personalAmount;
    }

    public double getAmountDueEmployee() {
        return amountDueEmployee;
    }

    public void setAmountDueEmployee(double amountDueEmployee) {
        this.amountDueEmployee = amountDueEmployee;
    }

    public double getAmountDueCompanyCard() {
        return amountDueCompanyCard;
    }

    public void setAmountDueCompanyCard(double amountDueCompanyCard) {
        this.amountDueCompanyCard = amountDueCompanyCard;
    }

    public double getTotalClaimedAmount() {
        return totalClaimedAmount;
    }

    public void setTotalClaimedAmount(double totalClaimedAmount) {
        this.totalClaimedAmount = totalClaimedAmount;
    }

    public double getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(double totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public void setLedgerName(String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public String getPolicyID() {
        return policyID;
    }

    public void setPolicyID(String policyID) {
        this.policyID = policyID;
    }

    public boolean isEverSentBack() {
        return everSentBack;
    }

    public void setEverSentBack(boolean everSentBack) {
        this.everSentBack = everSentBack;
    }

    public boolean isHasException() {
        return hasException;
    }

    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }

    public String getWorkflowActionUrl() {
        return workflowActionUrl;
    }

    public void setWorkflowActionUrl(String workflowActionUrl) {
        this.workflowActionUrl = workflowActionUrl;
    }

    public ConcurV3ExpenseReportItemDTO getChart() {
        return chart;
    }

    public void setChart(ConcurV3ExpenseReportItemDTO chart) {
        this.chart = chart;
    }

    public ConcurV3ExpenseReportItemDTO getAccount() {
        return account;
    }

    public void setAccount(ConcurV3ExpenseReportItemDTO account) {
        this.account = account;
    }

    public ConcurV3ExpenseReportItemDTO getTravlerType() {
        return travlerType;
    }

    public void setTravlerType(ConcurV3ExpenseReportItemDTO travlerType) {
        this.travlerType = travlerType;
    }

    public ConcurV3ExpenseReportItemDTO getReportTupe() {
        return reportTupe;
    }

    public void setReportTupe(ConcurV3ExpenseReportItemDTO reportTupe) {
        this.reportTupe = reportTupe;
    }

    public ConcurV3ExpenseReportItemDTO getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(ConcurV3ExpenseReportItemDTO businessPurpose) {
        this.businessPurpose = businessPurpose;
    }

    public ConcurV3ExpenseReportItemDTO getDepartmentOrgCode() {
        return departmentOrgCode;
    }

    public void setDepartmentOrgCode(ConcurV3ExpenseReportItemDTO departmentOrgCode) {
        this.departmentOrgCode = departmentOrgCode;
    }

    public ConcurV3ExpenseReportItemDTO getFtcBsc() {
        return ftcBsc;
    }

    public void setFtcBsc(ConcurV3ExpenseReportItemDTO ftcBsc) {
        this.ftcBsc = ftcBsc;
    }

    public ConcurV3ExpenseReportItemDTO getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(ConcurV3ExpenseReportItemDTO departmentName) {
        this.departmentName = departmentName;
    }

    public ConcurV3ExpenseReportItemDTO getUserType() {
        return userType;
    }

    public void setUserType(ConcurV3ExpenseReportItemDTO userType) {
        this.userType = userType;
    }

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}