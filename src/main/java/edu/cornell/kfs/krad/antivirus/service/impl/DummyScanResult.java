package edu.cornell.kfs.krad.antivirus.service.impl;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;

public class DummyScanResult implements ScanResult {
    
    private static final boolean PASS_ANTIVIRUS = true;

    @Override
    public String getResult() {
        return PASS_ANTIVIRUS ? ScanResult.Status.PASSED.toString() : ScanResult.Status.FAILED.toString();
    }

    @Override
    public Status getStatus() {
        return PASS_ANTIVIRUS ? ScanResult.Status.PASSED : ScanResult.Status.FAILED;
    }

    @Override
    public String getSignature() {
        return null;
    }

}
