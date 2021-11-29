package edu.cornell.kfs.concur.rest.jsonObjects;

import java.sql.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurV3ExpenseReportDTO {
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
    private ConcurV3ExpenseReportItemDTO chart;
    
    @JsonProperty("OrgUnit2")
    private ConcurV3ExpenseReportItemDTO account;
    
    @JsonProperty("Custom1")
    private ConcurV3ExpenseReportItemDTO travlerType;
    
    @JsonProperty("Custom2")
    private ConcurV3ExpenseReportItemDTO reportTupe;
    
    @JsonProperty("Custom3")
    private ConcurV3ExpenseReportItemDTO businessPurpose;
    
    @JsonProperty("Custom5")
    private ConcurV3ExpenseReportItemDTO departmentOrgCode;
    
    @JsonProperty("Custom8")
    private ConcurV3ExpenseReportItemDTO ftcBsc;
    
    @JsonProperty("Custom9")
    private ConcurV3ExpenseReportItemDTO departmentName;
    
    @JsonProperty("Custom15")
    private ConcurV3ExpenseReportItemDTO userType;
    
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