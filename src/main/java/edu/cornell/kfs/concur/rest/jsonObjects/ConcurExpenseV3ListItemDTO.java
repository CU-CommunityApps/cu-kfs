package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurExpenseV3ListItemDTO {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Total")
    private KualiDecimal total;

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
    private Date processingPaymentDate;

    @JsonProperty("PaidDate")
    private Date paidDate;

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
    private String approverLoginID;

    @JsonProperty("ApproverName")
    private String approverName;

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
    private KualiDecimal personalAmount;

    @JsonProperty("AmountDueEmployee")
    private KualiDecimal amountDueEmployee;

    @JsonProperty("AmountDueCompanyCard")
    private KualiDecimal amountDueCompanyCard;

    @JsonProperty("TotalClaimedAmount")
    private KualiDecimal totalClaimedAmount;

    @JsonProperty("TotalApprovedAmount")
    private KualiDecimal totalApprovedAmount;

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
    private ConcurExpenseV3ReportItemDTO travelerType;

    @JsonProperty("Custom2")
    private ConcurExpenseV3ReportItemDTO reportType;

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

    public KualiDecimal getTotal() {
        return total;
    }

    public void setTotal(KualiDecimal total) {
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

    public java.util.Date getProcessingPaymentDate() {
        return processingPaymentDate;
    }

    public void setProcessingPaymentDate(Date processingPaymentDate) {
        this.processingPaymentDate = processingPaymentDate;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
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

    public KualiDecimal getPersonalAmount() {
        return personalAmount;
    }

    public void setPersonalAmount(KualiDecimal personalAmount) {
        this.personalAmount = personalAmount;
    }

    public KualiDecimal getAmountDueEmployee() {
        return amountDueEmployee;
    }

    public void setAmountDueEmployee(KualiDecimal amountDueEmployee) {
        this.amountDueEmployee = amountDueEmployee;
    }

    public KualiDecimal getAmountDueCompanyCard() {
        return amountDueCompanyCard;
    }

    public void setAmountDueCompanyCard(KualiDecimal amountDueCompanyCard) {
        this.amountDueCompanyCard = amountDueCompanyCard;
    }

    public KualiDecimal getTotalClaimedAmount() {
        return totalClaimedAmount;
    }

    public void setTotalClaimedAmount(KualiDecimal totalClaimedAmount) {
        this.totalClaimedAmount = totalClaimedAmount;
    }

    public KualiDecimal getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(KualiDecimal totalApprovedAmount) {
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

    public ConcurExpenseV3ReportItemDTO getTravelerType() {
        return travelerType;
    }

    public void setTravelerType(ConcurExpenseV3ReportItemDTO travelerType) {
        this.travelerType = travelerType;
    }

    public ConcurExpenseV3ReportItemDTO getReportType() {
        return reportType;
    }

    public void setReportType(ConcurExpenseV3ReportItemDTO reportType) {
        this.reportType = reportType;
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