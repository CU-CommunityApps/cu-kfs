package edu.cornell.kfs.pdp.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class StaleCheckExtractDetail extends TransientBusinessObjectBase {
    private static final long serialVersionUID = -2282621440553926073L;
    private static final String DATA_DELIMITER = ",";

    private String checkIssuedDate;
    private String bankCode;
    private String checkStatus;
    private String checkNumber;
    private String checkTotalAmount;
    private String filename;
    private String lineNumber;

    public String getCheckIssuedDate() {
        return checkIssuedDate;
    }

    public void setCheckIssuedDate(String checkIssuedDate) {
        this.checkIssuedDate = checkIssuedDate;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getCheckTotalAmount() {
        return checkTotalAmount;
    }

    public void setCheckTotalAmount(String checkTotalAmount) {
        this.checkTotalAmount = checkTotalAmount;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getLogData() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCheckIssuedDate()).append(DATA_DELIMITER).append(getCheckTotalAmount()).append(DATA_DELIMITER);
        return sb.toString();
    }



}
