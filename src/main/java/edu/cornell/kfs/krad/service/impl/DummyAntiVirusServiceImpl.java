package edu.cornell.kfs.krad.service.impl;

import java.io.IOException;
import java.io.InputStream;

import edu.cornell.kfs.krad.service.AntiVirusService;
import edu.cornell.kfs.krad.service.ScanResult;

public class DummyAntiVirusServiceImpl implements AntiVirusService {

    @Override
    public ScanResult scan(byte[] in) throws IOException {
        return new DummyScanResult();
    }

    @Override
    public ScanResult scan(InputStream in) {
        return new DummyScanResult();
    }

}
