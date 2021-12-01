package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurRequestV4MainDesitnationDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("countrySubDivisionCode")
    private String countrySubDivisionCode;

    @JsonProperty("city")
    private String city;

    @JsonProperty("name")
    private String name;

    @JsonProperty("lnKey")
    private Integer lnKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountrySubDivisionCode() {
        return countrySubDivisionCode;
    }

    public void setCountrySubDivisionCode(String countrySubDivisionCode) {
        this.countrySubDivisionCode = countrySubDivisionCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLnKey() {
        return lnKey;
    }

    public void setLnKey(Integer lnKey) {
        this.lnKey = lnKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
