package edu.cornell.kfs.fp.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class RecurringDisbursementVoucherDocumentRoutingReportItem {

    private String recurringDvDocumentNumber;
    private String spawnedDvDocumentNumber;
    private List<String> errors;

    public RecurringDisbursementVoucherDocumentRoutingReportItem() {
        errors = new ArrayList<>();
    }

    public RecurringDisbursementVoucherDocumentRoutingReportItem(String recurringDvDocumentNumber,
            String spawnedDvDocumentNumber) {
        this();
        this.recurringDvDocumentNumber = recurringDvDocumentNumber;
        this.spawnedDvDocumentNumber = spawnedDvDocumentNumber;
    }

    public RecurringDisbursementVoucherDocumentRoutingReportItem(String recurringDvDocumentNumber,
            String spawnedDvDocumentNumber, String error) {
        this(recurringDvDocumentNumber, spawnedDvDocumentNumber);
        addError(error);
    }

    public String getRecurringDvDocumentNumber() {
        return recurringDvDocumentNumber;
    }

    public void setRecurringDvDocumentNumber(String recurringDvDocumentNumber) {
        this.recurringDvDocumentNumber = recurringDvDocumentNumber;
    }

    public String getSpawnedDvDocumentNumber() {
        return spawnedDvDocumentNumber;
    }

    public void setSpawnedDvDocumentNumber(String spawnedDvDocumentNumber) {
        this.spawnedDvDocumentNumber = spawnedDvDocumentNumber;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addAllErrors(Collection<String> otherErrors) {
        errors.addAll(otherErrors);
    }

    public boolean hasErrors() {
        return CollectionUtils.isNotEmpty(errors);
    }

}
