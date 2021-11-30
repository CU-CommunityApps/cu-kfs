package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurExpenseV3ReportDTO {
    @JsonProperty("Name")
    private String name;
    
    @JsonProperty("Total")
    private double total;
    
    @JsonProperty("CurrencyCode")
    private String currencyCode;
    
    @JsonProperty("Country")
    private String country;
    
    @JsonProperty("CountrySubdivision")
    private String countrySubdivision;
    
    @JsonProperty("CreateDate")
    private Date createDate;
    
    @JsonProperty("SubmitDate")
    private Date submitDate;
    
    @JsonProperty("ProcessingPaymentDate")
    private Object processingPaymentDate;
    
    @JsonProperty("PaidDate")
    private Object paidDate;
    
    @JsonProperty("ReceiptsReceived")
    private boolean receiptsReceived;
    
    @JsonProperty("UserDefinedDate")
    private Date userDefinedDate;
    
    @JsonProperty("LastComment")
    private String lastComment;
    
    @JsonProperty("OwnerLoginID")
    private String ownerLoginID;
    
    @JsonProperty("OwnerName")
    private String ownerName;
    
    @JsonProperty("ApproverLoginID")
    private Object approverLoginID;
    
    @JsonProperty("ApproverName")
    private Object approverName;
    
    @JsonProperty("ApprovalStatusName")
    private String approvalStatusName;
    
    @JsonProperty("ApprovalStatusCode")
    private String approvalStatusCode;
    
    @JsonProperty("PaymentStatusName")
    private String paymentStatusName;
    
    @JsonProperty("PaymentStatusCode")
    private String paymentStatusCode;
    
    @JsonProperty("LastModifiedDate")
    private Date lastModifiedDate;
    
    @JsonProperty("PersonalAmount")
    private double personalAmount;
    
    @JsonProperty("AmountDueEmployee")
    private double amountDueEmployee;
    
    @JsonProperty("AmountDueCompanyCard")
    private double amountDueCompanyCard;
    
    @JsonProperty("TotalClaimedAmount")
    private double totalClaimedAmount;
    
    @JsonProperty("TotalApprovedAmount")
    private double totalApprovedAmount;
    
    @JsonProperty("LedgerName")
    private String ledgerName;
    
    @JsonProperty("PolicyID")
    private String policyID;
    
    @JsonProperty("EverSentBack")
    private boolean everSentBack;
    
    @JsonProperty("HasException")
    private boolean hasException;
    
    @JsonProperty("WorkflowActionUrl")
    private String workflowActionUrl;
    
    @JsonProperty("OrgUnit1")
    private ConcurExpenseV3ReportItemDTO chart;
    
    @JsonProperty("OrgUnit2")
    private ConcurExpenseV3ReportItemDTO account;
    
    @JsonProperty("Custom1")
    private ConcurExpenseV3ReportItemDTO travlerType;
    
    @JsonProperty("Custom2")
    private ConcurExpenseV3ReportItemDTO reportTupe;
    
    @JsonProperty("Custom3")
    private ConcurExpenseV3ReportItemDTO businessPurpose;
    
    @JsonProperty("Custom5")
    private ConcurExpenseV3ReportItemDTO departmentOrgCode;
    
    @JsonProperty("Custom8")
    private ConcurExpenseV3ReportItemDTO ftcBsc;
    
    @JsonProperty("Custom9")
    private ConcurExpenseV3ReportItemDTO departmentName;
    
    @JsonProperty("Custom15")
    private ConcurExpenseV3ReportItemDTO userType;
    
    @JsonProperty("ID")
    private String id;
    
    @JsonProperty("URI")
    private String uri;
    
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

    public Object getApproverLoginID() {
        return approverLoginID;
    }

    public void setApproverLoginID(Object approverLoginID) {
        this.approverLoginID = approverLoginID;
    }

    public Object getApproverName() {
        return approverName;
    }

    public void setApproverName(Object approverName) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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