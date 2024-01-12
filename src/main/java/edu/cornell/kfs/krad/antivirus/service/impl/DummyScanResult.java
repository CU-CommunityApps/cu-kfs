package edu.cornell.kfs.krad.antivirus.service.impl;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;

public class DummyScanResult implements ScanResult {

    private String result = Status.PASSED.toString();
    private Status status = Status.PASSED;

    public DummyScanResult(String result) {
        this.result = result;
        refreshStatusFromCurrentResult();
    }

    @Override
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        refreshStatusFromCurrentResult();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String getSignature() {
        return null;
    }

    private void refreshStatusFromCurrentResult() {
        status = determineScanStatusFromScanResult(result);
    }

    private static Status determineScanStatusFromScanResult(String result) {
        switch (StringUtils.defaultString(result)) {
            case "PASSED" :
                return Status.PASSED;
            case "FAILED" :
                return Status.FAILED;
            case "ERROR" :
                return Status.ERROR;
            default :
                //Gracefully handle when null, empty string, or any unrecognized Status is passed to this method.
                return Status.ERROR;
        }
    }

}
