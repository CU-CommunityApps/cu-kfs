package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurRequestV4ListItemDTO {
    @JsonProperty("href")
    private String href;

    @JsonProperty("id")
    private String id;

    @JsonProperty("approvalStatus")
    private ConcurRequestV4StatusDTO approvalStatus;

    @JsonProperty("approved")
    private Boolean approved;

    @JsonProperty("approver")
    private ConcurRequestV4PersonDTO approver;

    @JsonProperty("businessPurpose")
    private String businessPurpose;

    @JsonProperty("canceledPostApproval")
    private Boolean canceledPostApproval;

    @JsonProperty("closed")
    private Boolean closed;

    @JsonProperty("creationDate")
    private String creationDate;

    @JsonProperty("endDate")
    private String endDate;

    @JsonProperty("everSentBack")
    private Boolean everSentBack;

    @JsonProperty("highestExceptionLevel")
    private String highestExceptionLevel;

    @JsonProperty("isUserReviewed")
    private Boolean isUserReviewed;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner")
    public ConcurRequestV4PersonDTO owner;

    @JsonProperty("pendingApproval")
    private Boolean pendingApproval;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("submitDate")
    private String submitDate;

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

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
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

    public Boolean getCanceledPostApproval() {
        return canceledPostApproval;
    }

    public void setCanceledPostApproval(Boolean canceledPostApproval) {
        this.canceledPostApproval = canceledPostApproval;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getEverSentBack() {
        return everSentBack;
    }

    public void setEverSentBack(Boolean everSentBack) {
        this.everSentBack = everSentBack;
    }

    public String getHighestExceptionLevel() {
        return highestExceptionLevel;
    }

    public void setHighestExceptionLevel(String highestExceptionLevel) {
        this.highestExceptionLevel = highestExceptionLevel;
    }

    public Boolean getIsUserReviewed() {
        return isUserReviewed;
    }

    public void setIsUserReviewed(Boolean isUserReviewed) {
        this.isUserReviewed = isUserReviewed;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
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
