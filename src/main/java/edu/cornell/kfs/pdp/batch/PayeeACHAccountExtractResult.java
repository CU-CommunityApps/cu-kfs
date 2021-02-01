package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.List;

public class PayeeACHAccountExtractResult {
    
    private List<String> errors;
    private int numberOfRowsProcessedSuccessfully;
    private int numberOfRowsWithFailures;
    
    PayeeACHAccountExtractResult(){
        errors = new ArrayList<String>();
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

}
