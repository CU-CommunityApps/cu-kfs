package edu.cornell.kfs.concur.rest.jsonObjects;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpenseListingDTO{
    @JsonProperty("Items") 
    public List<ExpenseListItemDTO> items;
    
    @JsonProperty("NextPage") 
    public String nextPage;
    
    public List<ExpenseListItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ExpenseListItemDTO> items) {
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
