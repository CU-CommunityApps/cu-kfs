package edu.cornell.kfs.antivirus.service.impl;

import java.io.IOException;
import java.io.InputStream;

import edu.cornell.kfs.antivirus.service.AntiVirusService;
import edu.cornell.kfs.antivirus.service.ScanResult;

public class DummyAntiVirusServiceImpl implements AntiVirusService {

    public ScanResult scan(byte[] in) throws IOException {
        return new DummyScanResult();
    }

    public ScanResult scan(InputStream in) {
        return new DummyScanResult();
    }

}
