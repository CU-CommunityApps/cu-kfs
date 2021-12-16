package edu.cornell.kfs.concur.rest.jsonObjects;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurRequestV4ListingDTO {
    @JsonProperty("data")
    public List<ConcurRequestV4ListItemDTO> listItems;

    @JsonProperty("operations")
    public List<ConcurRequestV4OperationDTO> operations;

    @JsonProperty("totalCount")
    public Integer totalCount;

    public List<ConcurRequestV4ListItemDTO> getListItems() {
        return listItems;
    }

    public void setListItems(List<ConcurRequestV4ListItemDTO> listItems) {
        this.listItems = listItems;
    }

    public List<ConcurRequestV4OperationDTO> getOperations() {
        return operations;
    }

    public void setOperations(List<ConcurRequestV4OperationDTO> operations) {
        this.operations = operations;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
