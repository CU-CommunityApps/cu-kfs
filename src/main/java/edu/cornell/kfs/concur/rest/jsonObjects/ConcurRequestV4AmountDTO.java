package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurRequestV4AmountDTO {
    @JsonProperty("value")
    private KualiDecimal value;

    @JsonProperty("currency")
    private String currency;

    public KualiDecimal getValue() {
        return value;
    }

    public void setValue(KualiDecimal value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
