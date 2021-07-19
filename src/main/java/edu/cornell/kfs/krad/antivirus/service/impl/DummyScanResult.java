package edu.cornell.kfs.krad.antivirus.service.impl;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;

public class DummyScanResult implements ScanResult {

    @Override
    public String getResult() {
        return ScanResult.Status.PASSED.toString();
    }

    @Override
    public Status getStatus() {
        return ScanResult.Status.PASSED;
    }

    @Override
    public String getSignature() {
        return null;
    }

}
