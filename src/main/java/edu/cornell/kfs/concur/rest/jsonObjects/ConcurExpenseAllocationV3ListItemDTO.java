package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurExpenseAllocationV3ListItemDTO {
    
    @JsonProperty("ID")
    private String id;
    
    @JsonProperty("EntryID")
    private String entryId;
    
    @JsonProperty("URI")
    private String uri;
    
    @JsonProperty("Percentage")
    private KualiDecimal percentage;
    
    @JsonProperty("IsPercentEdited")
    private boolean percentEdited;
    
    @JsonProperty("IsHidden")
    private boolean hidden;
    
    @JsonProperty("AccountCode1")
    private String objectCode;
    
    @JsonProperty("Custom1")
    private ConcurExpenseAllocationV3ListItemDetailDTO chart;
    
    @JsonProperty("Custom2")
    private ConcurExpenseAllocationV3ListItemDetailDTO account;
    
    @JsonProperty("Custom3")
    private ConcurExpenseAllocationV3ListItemDetailDTO subAccount;
    
    @JsonProperty("Custom4")
    private ConcurExpenseAllocationV3ListItemDetailDTO subObject;
    
    @JsonProperty("Custom5")
    private ConcurExpenseAllocationV3ListItemDetailDTO projectCode;
    
    @JsonProperty("Custom6")
    private ConcurExpenseAllocationV3ListItemDetailDTO orgRefId;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public KualiDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(KualiDecimal percentage) {
        this.percentage = percentage;
    }

    public boolean isPercentEdited() {
        return percentEdited;
    }

    public void setPercentEdited(boolean percentEdited) {
        this.percentEdited = percentEdited;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getChart() {
        return chart;
    }

    public void setChart(ConcurExpenseAllocationV3ListItemDetailDTO chart) {
        this.chart = chart;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getAccount() {
        return account;
    }

    public void setAccount(ConcurExpenseAllocationV3ListItemDetailDTO account) {
        this.account = account;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(ConcurExpenseAllocationV3ListItemDetailDTO subAccount) {
        this.subAccount = subAccount;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getSubObject() {
        return subObject;
    }

    public void setSubObject(ConcurExpenseAllocationV3ListItemDetailDTO subObject) {
        this.subObject = subObject;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(ConcurExpenseAllocationV3ListItemDetailDTO projectCode) {
        this.projectCode = projectCode;
    }

    public ConcurExpenseAllocationV3ListItemDetailDTO getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(ConcurExpenseAllocationV3ListItemDetailDTO orgRefId) {
        this.orgRefId = orgRefId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
