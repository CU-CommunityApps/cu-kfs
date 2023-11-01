package edu.cornell.kfs.krad.antivirus.service.impl;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;

public class DummyScanResult implements ScanResult {
    
    private Status scanResultStatus;
    
    public DummyScanResult(Status scanResultStatus) {
        this.scanResultStatus = scanResultStatus;
    }

    @Override
    public String getResult() {
        return scanResultStatus.toString();
    }

    @Override
    public Status getStatus() {
        return scanResultStatus;
    }

    @Override
    public String getSignature() {
        return null;
    }

}
