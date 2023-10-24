package edu.cornell.kfs.krad.antivirus.service.impl;

import java.io.IOException;
import java.io.InputStream;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.service.ScanResult.Status;
import edu.cornell.kfs.krad.antivirus.service.AntiVirusService;

public class DummyAntiVirusServiceImpl implements AntiVirusService {
    
    private Status scanResultStatus;
    
    public DummyAntiVirusServiceImpl() {
        this(ScanResult.Status.PASSED);
    }
    
    public DummyAntiVirusServiceImpl(Status scanResultStatus) {
        this.scanResultStatus = scanResultStatus;
    }

    @Override
    public ScanResult scan(byte[] in) throws IOException {
        return new DummyScanResult(scanResultStatus);
    }

    @Override
    public ScanResult scan(InputStream in) {
        return new DummyScanResult(scanResultStatus);
    }

    public Status getScanResultStatus() {
        return scanResultStatus;
    }

    public void setScanResultStatus(Status scanResultStatus) {
        this.scanResultStatus = scanResultStatus;
    }

}
