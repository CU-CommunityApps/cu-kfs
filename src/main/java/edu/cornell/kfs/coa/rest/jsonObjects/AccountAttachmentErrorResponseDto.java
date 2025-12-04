package edu.cornell.kfs.coa.rest.jsonObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountAttachmentErrorResponseDto implements Serializable {

    private static final long serialVersionUID = 6607620766778933821L;

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

    public static AccountAttachmentErrorResponseDto of(final List<String> errors) {
        final AccountAttachmentErrorResponseDto dto = new AccountAttachmentErrorResponseDto();
        dto.setErrors(errors);
        return dto;
    }

}
