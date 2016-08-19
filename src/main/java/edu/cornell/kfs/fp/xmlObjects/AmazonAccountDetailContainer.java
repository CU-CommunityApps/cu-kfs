package edu.cornell.kfs.fp.xmlObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
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
