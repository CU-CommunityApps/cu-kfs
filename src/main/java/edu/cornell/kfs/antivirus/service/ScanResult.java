package edu.cornell.kfs.antivirus.service;

public interface ScanResult {
    enum Status { PASSED, FAILED, ERROR }

    String getResult();

    void setResult(String result);

    Status getStatus();

    void setStatus(Status status);

    void setSignature(String signature);

    String getSignature();

}
