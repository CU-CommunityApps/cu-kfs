package edu.cornell.kfs.fp.xmlObjects;

import java.io.Serializable;

import javax.annotation.Generated;

import org.kuali.kfs.krad.util.KRADConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "aws_account",
    "kfs_account",
    "cost_center",
    "business_purpose",
    "cost"
})
public class AmazonAccountDetail implements Serializable {

    private static final long serialVersionUID = 2085751046553129803L;
    @JsonProperty("aws_account")
    private String awsAccount;
    @JsonProperty("kfs_account")
    private String kfsAccount;
    @JsonProperty("cost_center")
    private String costCenter;
    @JsonProperty("business_purpose")
    private String businessPurpose;
    @JsonProperty("cost")
    private String cost;
    
    public String getAwsAccount() {
        return awsAccount;
    }
    public void setAwsAccount(String awsAccount) {
        this.awsAccount = awsAccount;
    }
    public String getKfsAccount() {
        return kfsAccount;
    }
    public void setKfsAccount(String kfsAccount) {
        this.kfsAccount = kfsAccount;
    }
    public String getCostCenter() {
        return costCenter;
    }
    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }
    public String getBusinessPurpose() {
        return businessPurpose;
    }
    public void setBusinessPurpose(String businessPurpose) {
        this.businessPurpose = businessPurpose;
    }
    public String getCost() {
        return cost;
    }
    public void setCost(String cost) {
        this.cost = cost;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("AWS Account: ").append(KRADConstants.SINGLE_QUOTE).append(getAwsAccount()).append(KRADConstants.SINGLE_QUOTE);
        sb.append(" KFS Account: ").append(KRADConstants.SINGLE_QUOTE).append(getKfsAccount()).append(KRADConstants.SINGLE_QUOTE);
        sb.append(" Cost Center: ").append(KRADConstants.SINGLE_QUOTE).append(getCostCenter()).append(KRADConstants.SINGLE_QUOTE);
        sb.append(" Business Purpose: ").append(KRADConstants.SINGLE_QUOTE).append(getBusinessPurpose()).append(KRADConstants.SINGLE_QUOTE);
        sb.append(" Cost: ").append(KRADConstants.SINGLE_QUOTE).append(getCost()).append(KRADConstants.SINGLE_QUOTE);
        return sb.toString();
    }
    
}