package edu.cornell.kfs.fp.businessobject;

import java.io.Serializable;
import java.sql.Date;

public class RecurringDisbursementVoucherPDPStatus implements Serializable, Comparable<RecurringDisbursementVoucherPDPStatus> {
    
    private static final long serialVersionUID = 241060669050768331L;
    
    private String documentNumber;
    private String pdpStatus;
    private Date extractDate;
    private Date cancelDate;
    private Date paidDate;
    private Date dueDate;
    private String paymentDetailDocumentType;
    private String dvStatus;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPdpStatus() {
        return pdpStatus;
    }

    public void setPdpStatus(String pdpStatus) {
        this.pdpStatus = pdpStatus;
    }

    public Date getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(Date extractDate) {
        this.extractDate = extractDate;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentDetailDocumentType() {
        return paymentDetailDocumentType;
    }

    public void setPaymentDetailDocumentType(String paymentDetailDocumentType) {
        this.paymentDetailDocumentType = paymentDetailDocumentType;
    }

    public String getDvStatus() {
        return dvStatus;
    }

    public void setDvStatus(String dvStatus) {
        this.dvStatus = dvStatus;
    }

    @Override
    public int compareTo(RecurringDisbursementVoucherPDPStatus o) {
        if (documentNumber == null || o.getDocumentNumber() == null) {
            return 0;
        } else {
            return documentNumber.compareTo(o.getDocumentNumber());
        }
    }

}
