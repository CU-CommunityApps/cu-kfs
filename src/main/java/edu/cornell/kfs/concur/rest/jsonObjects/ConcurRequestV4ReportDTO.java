package edu.cornell.kfs.concur.rest.jsonObjects;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurRequestV4ReportDTO {

    @JsonProperty("href")
    private String href;

    @JsonProperty("id")
    private String id;

    @JsonProperty("approvalStatus")
    private ConcurRequestV4StatusDTO approvalStatus;

    @JsonProperty("approved")
    private boolean approved;

    @JsonProperty("approver")
    private ConcurRequestV4PersonDTO approver;

    @JsonProperty("businessPurpose")
    private String businessPurpose;

    @JsonProperty("canceledPostApproval")
    private boolean canceledPostApproval;

    @JsonProperty("closed")
    private boolean closed;

    @JsonProperty("creationDate")
    private Date creationDate;

    @JsonProperty("custom1")
    private ConcurRequestV4CustomItemDTO chart;

    @JsonProperty("custom2")
    private ConcurRequestV4CustomItemDTO Account;

    @JsonProperty("endDate")
    private Date endDate;

    @JsonProperty("everSentBack")
    private boolean everSentBack;

    @JsonProperty("isUserReviewed")
    private boolean isUserReviewed;

    @JsonProperty("lastModified")
    private String lastModified;

    @JsonProperty("mainDestination")
    private ConcurRequestV4MainDestinationDTO mainDestination;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner")
    private ConcurRequestV4PersonDTO owner;

    @JsonProperty("pendingApproval")
    private Boolean pendingApproval;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("startDate")
    private Date startDate;

    @JsonProperty("submitDate")
    private Date submitDate;

    @JsonProperty("totalApprovedAmount")
    private ConcurRequestV4AmountDTO totalApprovedAmount;

    @JsonProperty("totalPostedAmount")
    private ConcurRequestV4AmountDTO totalPostedAmount;

    @JsonProperty("totalRemainingAmount")
    private ConcurRequestV4AmountDTO totalRemainingAmount;

    @JsonProperty("type")
    private ConcurRequestV4TypeDTO type;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConcurRequestV4StatusDTO getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ConcurRequestV4StatusDTO approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public ConcurRequestV4PersonDTO getApprover() {
        return approver;
    }

    public void setApprover(ConcurRequestV4PersonDTO approver) {
        this.approver = approver;
    }

    public String getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(String businessPurpose) {
        this.businessPurpose = businessPurpose;
    }

    public boolean isCanceledPostApproval() {
        return canceledPostApproval;
    }

    public void setCanceledPostApproval(boolean canceledPostApproval) {
        this.canceledPostApproval = canceledPostApproval;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ConcurRequestV4CustomItemDTO getChart() {
        return chart;
    }

    public void setChart(ConcurRequestV4CustomItemDTO chart) {
        this.chart = chart;
    }

    public ConcurRequestV4CustomItemDTO getAccount() {
        return Account;
    }

    public void setAccount(ConcurRequestV4CustomItemDTO account) {
        Account = account;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isEverSentBack() {
        return everSentBack;
    }

    public void setEverSentBack(boolean everSentBack) {
        this.everSentBack = everSentBack;
    }

    public boolean isUserReviewed() {
        return isUserReviewed;
    }

    public void setUserReviewed(boolean isUserReviewed) {
        this.isUserReviewed = isUserReviewed;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public ConcurRequestV4MainDestinationDTO getMainDestination() {
        return mainDestination;
    }

    public void setMainDestination(ConcurRequestV4MainDestinationDTO mainDestination) {
        this.mainDestination = mainDestination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConcurRequestV4PersonDTO getOwner() {
        return owner;
    }

    public void setOwner(ConcurRequestV4PersonDTO owner) {
        this.owner = owner;
    }

    public Boolean getPendingApproval() {
        return pendingApproval;
    }

    public void setPendingApproval(Boolean pendingApproval) {
        this.pendingApproval = pendingApproval;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }

    public ConcurRequestV4AmountDTO getTotalApprovedAmount() {
        return totalApprovedAmount;
    }

    public void setTotalApprovedAmount(ConcurRequestV4AmountDTO totalApprovedAmount) {
        this.totalApprovedAmount = totalApprovedAmount;
    }

    public ConcurRequestV4AmountDTO getTotalPostedAmount() {
        return totalPostedAmount;
    }

    public void setTotalPostedAmount(ConcurRequestV4AmountDTO totalPostedAmount) {
        this.totalPostedAmount = totalPostedAmount;
    }

    public ConcurRequestV4AmountDTO getTotalRemainingAmount() {
        return totalRemainingAmount;
    }

    public void setTotalRemainingAmount(ConcurRequestV4AmountDTO totalRemainingAmount) {
        this.totalRemainingAmount = totalRemainingAmount;
    }

    public ConcurRequestV4TypeDTO getType() {
        return type;
    }

    public void setType(ConcurRequestV4TypeDTO type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
