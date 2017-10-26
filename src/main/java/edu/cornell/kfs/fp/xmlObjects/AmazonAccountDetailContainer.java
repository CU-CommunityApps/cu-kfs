package edu.cornell.kfs.fp.xmlObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "account_detail"
})
public class AmazonAccountDetailContainer implements Serializable {

    private static final long serialVersionUID = -934452176115246084L;
    
    @JsonProperty("account_detail")
    private List<AmazonAccountDetail> accountDetail = new ArrayList<AmazonAccountDetail>();

    public List<AmazonAccountDetail> getAccountDetail() {
        return accountDetail;
    }

    public void setAccountDetail(List<AmazonAccountDetail> accountDetail) {
        this.accountDetail = accountDetail;
    }

}
