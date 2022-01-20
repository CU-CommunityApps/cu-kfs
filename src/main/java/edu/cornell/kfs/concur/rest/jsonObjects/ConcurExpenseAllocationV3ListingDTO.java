package edu.cornell.kfs.concur.rest.jsonObjects;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurExpenseAllocationV3ListingDTO {
    
    @JsonProperty("Items")
    private List<ConcurExpenseAllocationV3ListItemDTO> items;
    
    @JsonProperty("NextPage")
    private String nextPage;
    
    public List<ConcurExpenseAllocationV3ListItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ConcurExpenseAllocationV3ListItemDTO> items) {
        this.items = items;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
