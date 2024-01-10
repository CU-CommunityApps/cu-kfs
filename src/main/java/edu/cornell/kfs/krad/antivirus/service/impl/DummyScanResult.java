package edu.cornell.kfs.krad.antivirus.service.impl;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.util.DummyAVUtils;

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
        status = DummyAVUtils.determineScanStatusFromScanResult(result);
    }

}
