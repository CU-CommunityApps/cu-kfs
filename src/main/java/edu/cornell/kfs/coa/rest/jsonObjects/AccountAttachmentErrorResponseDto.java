package edu.cornell.kfs.coa.rest.jsonObjects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountAttachmentErrorResponseDto {

    private List<String> errors;

    public List<String> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }

}
