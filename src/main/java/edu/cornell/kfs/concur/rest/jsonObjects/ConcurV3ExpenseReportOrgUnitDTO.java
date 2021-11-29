package edu.cornell.kfs.concur.rest.jsonObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurV3ExpenseReportOrgUnitDTO{
    @JsonProperty("Type") 
    public String type;
    @JsonProperty("Value") 
    public String value;
    @JsonProperty("Code") 
    public String code;
    @JsonProperty("ListItemID") 
    public String listItemID;
}