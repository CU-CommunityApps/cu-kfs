package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurExpenseV3ListItemDTO {
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
    public ConcurExpenseV3ReportItemDTO chart;

    @JsonProperty("OrgUnit2")
    public ConcurExpenseV3ReportItemDTO account;

    @JsonProperty("Custom1")
    public ConcurExpenseV3ReportItemDTO travlerType;

    @JsonProperty("Custom2")
    public ConcurExpenseV3ReportItemDTO reportTupe;

    @JsonProperty("Custom3")
    public ConcurExpenseV3ReportItemDTO businessPurpose;

    @JsonProperty("Custom5")
    public ConcurExpenseV3ReportItemDTO departmentOrgCode;

    @JsonProperty("Custom8")
    public ConcurExpenseV3ReportItemDTO ftcBsc;

    @JsonProperty("Custom9")
    public ConcurExpenseV3ReportItemDTO departmentName;

    @JsonProperty("Custom15")
    public ConcurExpenseV3ReportItemDTO userType;

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

    public ConcurExpenseV3ReportItemDTO getChart() {
        return chart;
    }

    public void setChart(ConcurExpenseV3ReportItemDTO chart) {
        this.chart = chart;
    }

    public ConcurExpenseV3ReportItemDTO getAccount() {
        return account;
    }

    public void setAccount(ConcurExpenseV3ReportItemDTO account) {
        this.account = account;
    }

    public ConcurExpenseV3ReportItemDTO getTravlerType() {
        return travlerType;
    }

    public void setTravlerType(ConcurExpenseV3ReportItemDTO travlerType) {
        this.travlerType = travlerType;
    }

    public ConcurExpenseV3ReportItemDTO getReportTupe() {
        return reportTupe;
    }

    public void setReportTupe(ConcurExpenseV3ReportItemDTO reportTupe) {
        this.reportTupe = reportTupe;
    }

    public ConcurExpenseV3ReportItemDTO getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(ConcurExpenseV3ReportItemDTO businessPurpose) {
        this.businessPurpose = businessPurpose;
    }

    public ConcurExpenseV3ReportItemDTO getDepartmentOrgCode() {
        return departmentOrgCode;
    }

    public void setDepartmentOrgCode(ConcurExpenseV3ReportItemDTO departmentOrgCode) {
        this.departmentOrgCode = departmentOrgCode;
    }

    public ConcurExpenseV3ReportItemDTO getFtcBsc() {
        return ftcBsc;
    }

    public void setFtcBsc(ConcurExpenseV3ReportItemDTO ftcBsc) {
        this.ftcBsc = ftcBsc;
    }

    public ConcurExpenseV3ReportItemDTO getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(ConcurExpenseV3ReportItemDTO departmentName) {
        this.departmentName = departmentName;
    }

    public ConcurExpenseV3ReportItemDTO getUserType() {
        return userType;
    }

    public void setUserType(ConcurExpenseV3ReportItemDTO userType) {
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