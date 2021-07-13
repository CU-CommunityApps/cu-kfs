package edu.cornell.kfs.krad.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.service.ScanResult;
import edu.cornell.kfs.krad.util.ClamAVUtils;

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
        status = determineStatusFromCurrentResult();
        if (Status.FAILED.equals(status)) {
            signature = ClamAVUtils.getSignatureFromScanResultMessage(result);
        } else {
            signature = KFSConstants.EMPTY_STRING;
        }
    }

    private Status determineStatusFromCurrentResult() {
        switch (StringUtils.defaultString(result)) {
            case ClamAVResponses.RESPONSE_OK :
                return Status.PASSED;
            
            case ClamAVResponses.RESPONSE_SIZE_EXCEEDED :
                return Status.ERROR;
            
            case ClamAVResponses.RESPONSE_ERROR_WRITING_FILE :
                return Status.ERROR;
            
            default :
                return StringUtils.endsWith(result, ClamAVResponses.FOUND_SUFFIX) ? Status.FAILED : Status.ERROR;
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
