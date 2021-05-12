package edu.cornell.kfs.antivirus.service.impl;

import edu.cornell.kfs.antivirus.service.ScanResult;

public class DummyScanResult implements ScanResult {

    public String getResult() {
        return ScanResult.Status.PASSED.toString();
    }

    public void setResult(String result) {

    }

    public Status getStatus() {
        return ScanResult.Status.PASSED;
    }

    public void setStatus(Status status) {
    }

    public void setSignature(String signature) {
    }

    public String getSignature() {
        return null;
    }

}
