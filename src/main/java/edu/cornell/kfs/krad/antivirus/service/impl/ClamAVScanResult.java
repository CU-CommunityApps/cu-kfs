package edu.cornell.kfs.krad.antivirus.service.impl;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.antivirus.service.ScanResult;
import edu.cornell.kfs.krad.antivirus.util.ClamAVUtils;

public class ClamAVScanResult implements ScanResult {

    private String result = KFSConstants.EMPTY_STRING;
    private Status status = Status.FAILED;
    private String signature = KFSConstants.EMPTY_STRING;
    private Exception exception = null;

    public ClamAVScanResult(String result) {
        this.result = result;
        refreshStatusAndSignatureFromCurrentResult();
    }

    public ClamAVScanResult(Exception ex) {
        this.exception = ex;
        this.status = Status.ERROR;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        refreshStatusAndSignatureFromCurrentResult();
    }

    private void refreshStatusAndSignatureFromCurrentResult() {
        status = ClamAVUtils.determineScanStatusFromScanResultMessage(result);
        if (Status.FAILED.equals(status)) {
            signature = ClamAVUtils.extractSignatureFromScanResultMessage(result);
        } else {
            signature = KFSConstants.EMPTY_STRING;
        }
    }

    @Override
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
