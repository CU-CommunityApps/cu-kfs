package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountExtractResult {
    
    private List<String> errors;
    private Map<PayeeACHAccountExtractDetail, List<String>> errorEntries;
    private List<PayeeACHAccountExtractDetail> successEntries;
    private int numberOfRowsProcessedSuccessfully;
    private int numberOfRowsWithFailures;
    
    PayeeACHAccountExtractResult(){
        errors = new ArrayList<String>();
        errorEntries = new HashMap<PayeeACHAccountExtractDetail, List<String>>();
        successEntries = new ArrayList<PayeeACHAccountExtractDetail>();
    }  
    
    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public int getNumberOfRowsProcessedSuccessfully() {
        return numberOfRowsProcessedSuccessfully;
    }

    public void setNumberOfRowsProcessedSuccessfully(int numberOfRowsProcessedSuccessfully) {
        this.numberOfRowsProcessedSuccessfully = numberOfRowsProcessedSuccessfully;
    }

    public int getNumberOfRowsWithFailures() {
        return numberOfRowsWithFailures;
    }

    public void setNumberOfRowsWithFailures(int numberOfRowsWithFailures) {
        this.numberOfRowsWithFailures = numberOfRowsWithFailures;
    }
    
    public void addSuccessRow() {
        this.numberOfRowsProcessedSuccessfully++;
    }
    
    public void addFailedRow() {
        this.numberOfRowsWithFailures++;
    }

    public Map<PayeeACHAccountExtractDetail, List<String>> getErrorEntries() {
        return errorEntries;
    }

    public void setErrorEntries(Map<PayeeACHAccountExtractDetail, List<String>> entryErrors) {
        this.errorEntries = entryErrors;
    }

    public List<PayeeACHAccountExtractDetail> getSuccessEntries() {
        return successEntries;
    }

    public void setSuccessEntries(List<PayeeACHAccountExtractDetail> successEntries) {
        this.successEntries = successEntries;
    }

}
