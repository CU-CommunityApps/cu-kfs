package edu.cornell.kfs.pdp.batch;

import java.util.ArrayList;
import java.util.List;

public class PayeeACHAccountExtractReportData {
    
    private List<PayeeACHAccountExtractFileResult> achAccountExtractFileResults;
    private PayeeACHAccountExtractRetryResult achAccountExtractRetryResults;
    
    public PayeeACHAccountExtractReportData() {
        achAccountExtractFileResults = new ArrayList<PayeeACHAccountExtractFileResult>();
    }
    
    public void addAchAccountExtractFileResult(PayeeACHAccountExtractFileResult accountExtractFileResult) {
        achAccountExtractFileResults.add(accountExtractFileResult);
    }

    public List<PayeeACHAccountExtractFileResult> getAchAccountExtractFileResults() {
        return achAccountExtractFileResults;
    }

    public void setAchAccountExtractFileResults(List<PayeeACHAccountExtractFileResult> achAccountExtractFileResults) {
        this.achAccountExtractFileResults = achAccountExtractFileResults;
    }

    public PayeeACHAccountExtractRetryResult getAchAccountExtractRetryResults() {
        return achAccountExtractRetryResults;
    }

    public void setAchAccountExtractRetryResults(PayeeACHAccountExtractRetryResult achAccountExtractRetryResults) {
        this.achAccountExtractRetryResults = achAccountExtractRetryResults;
    }


}
