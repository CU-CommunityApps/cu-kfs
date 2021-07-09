package edu.cornell.kfs.krad.service.impl;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.service.ScanResult;

public class ClamAVScanResult implements ScanResult {

    private String result = "";
    private Status status = Status.FAILED;
    private String signature = "";
    private Exception exception = null;

    public ClamAVScanResult(String result) {
        setResult(result);
    }

    public ClamAVScanResult(Exception ex) {
        setException(ex);
        setStatus(Status.ERROR);
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

        if (StringUtils.isBlank(result)) {
            setStatus(Status.ERROR);
        } else if (ClamAVResponses.RESPONSE_OK.equals(result)) {
            setStatus(Status.PASSED);
        } else if (result.endsWith(ClamAVResponses.FOUND_SUFFIX)) {
            setSignature(result.substring(ClamAVResponses.STREAM_PREFIX.length(),
                    result.lastIndexOf(ClamAVResponses.FOUND_SUFFIX) - 1 ));
        } else if (ClamAVResponses.RESPONSE_SIZE_EXCEEDED.equals(result)) {
            setStatus(Status.ERROR);
        } else if (ClamAVResponses.RESPONSE_ERROR_WRITING_FILE.equals(result)) {
            setStatus(Status.ERROR);                           
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
