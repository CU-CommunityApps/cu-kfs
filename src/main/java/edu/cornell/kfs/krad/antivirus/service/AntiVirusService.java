package edu.cornell.kfs.krad.antivirus.service;

import java.io.IOException;
import java.io.InputStream;

public interface AntiVirusService {

    ScanResult scan(byte[] in) throws IOException;

    ScanResult scan(InputStream in);

}
