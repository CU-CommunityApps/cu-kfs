package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurBatchReportHeaderValidationErrorItem {
    List<ValidationResult> headerValidationErrors;

    public ConcurBatchReportHeaderValidationErrorItem() {
        this.headerValidationErrors = new ArrayList<ValidationResult>();
    }

    public ConcurBatchReportHeaderValidationErrorItem(List<ValidationResult> headerValidationErrors) {
        this.headerValidationErrors = headerValidationErrors;
    }

    public List<ValidationResult> getHeaderValidationErrors() {
        return headerValidationErrors;
    }

    public void setHeaderValidationErrors(List<ValidationResult> headerValidationErrors) {
        this.headerValidationErrors = headerValidationErrors;
    }

    public void addHeaderValidationError(ValidationResult headerValidationError) {
        if (headerValidationErrors == null) {
            headerValidationErrors = new ArrayList<ValidationResult>();
        }
        this.headerValidationErrors.add(headerValidationError);
    }

}
