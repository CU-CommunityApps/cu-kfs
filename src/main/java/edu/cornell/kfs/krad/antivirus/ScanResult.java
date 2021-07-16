package edu.cornell.kfs.krad.antivirus;

public interface ScanResult {
    enum Status { PASSED, FAILED, ERROR }

    String getResult();

    Status getStatus();

    String getSignature();
}
