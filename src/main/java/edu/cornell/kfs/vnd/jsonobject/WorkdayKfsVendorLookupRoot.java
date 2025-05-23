package edu.cornell.kfs.vnd.jsonobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkdayKfsVendorLookupRoot {

    @JsonProperty("Report_Entry")
    private List<WorkdayKfsVendorLookupResult> results;
    
    public WorkdayKfsVendorLookupRoot() {
        results = new ArrayList<WorkdayKfsVendorLookupResult>();
    }

    public List<WorkdayKfsVendorLookupResult> getResults() {
        if (results == null) {
            results = new ArrayList<WorkdayKfsVendorLookupResult>();
        }
        return results;
    }

    public void setResults(List<WorkdayKfsVendorLookupResult> results) {
        this.results = results;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
