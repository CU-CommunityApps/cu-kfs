package edu.cornell.kfs.concur.rest.jsonObjects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * NOTE: This DTO has been limited to just the common field(s) for both Expense V4 and Request V4 workflow actions.
 * If further separation is needed between the two in the future, then please refactor this class accordingly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConcurV4WorkflowDTO {

    @JsonProperty("comment")
    private String comment;

    public ConcurV4WorkflowDTO() {
    }

    public ConcurV4WorkflowDTO(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
