package edu.cornell.kfs.krad.antivirus.service.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.service.ScanResult.Status;
import edu.cornell.kfs.krad.antivirus.service.AntiVirusService;

public class DummyAntiVirusServiceImpl implements AntiVirusService {
    private static final Logger LOG = LogManager.getLogger();
    
    private String scanResult = Status.PASSED.toString();

    public DummyAntiVirusServiceImpl(String scanResult) {
        this.scanResult = scanResult;
        LOG.debug("DummyAntiVirusServiceImpl, created with virus scanResult of {}", scanResult);
    }

    @Override
    public ScanResult scan(byte[] in) throws IOException {
        return new DummyScanResult(scanResult);
    }

    @Override
    public ScanResult scan(InputStream in) {
        return new DummyScanResult(scanResult);
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

}
