package edu.cornell.kfs.pdp.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class StaleCheckBatchRow extends TransientBusinessObjectBase {
    private static final long serialVersionUID = -2282621440553926073L;
    private static final String DATA_DELIMITER = ",";

    private String checkIssuedDate;
    private String bankCode;
    private String checkStatus;
    private String checkNumber;
    private String checkTotalAmount;

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

    public String getLogData() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCheckNumber()).append(DATA_DELIMITER).append(getCheckTotalAmount());
        return sb.toString();
    }

}
