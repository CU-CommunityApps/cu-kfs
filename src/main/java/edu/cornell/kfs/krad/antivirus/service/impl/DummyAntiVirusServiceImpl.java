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
    
    private String dummyResult = Status.PASSED.toString();

    public DummyAntiVirusServiceImpl(String dummyResult) {
        this.dummyResult = dummyResult;
        LOG.debug("DummyAntiVirusServiceImpl, created with virus dummyResult of {}", dummyResult);
    }

    @Override
    public ScanResult scan(byte[] in) throws IOException {
        return new DummyScanResult(dummyResult);
    }

    @Override
    public ScanResult scan(InputStream in) {
        return new DummyScanResult(dummyResult);
    }

    public String getDummyResult() {
        return dummyResult;
    }

    public void setDummyResult(String dummyResult) {
        this.dummyResult = dummyResult;
    }

}
