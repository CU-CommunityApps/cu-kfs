package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class ConcurRequestedCashAdvance extends PersistableBusinessObjectBase{
    protected int concurRequestedCashAdvanceId;
    protected String requestId;
    protected String employeeId;
    protected KualiDecimal paymentAmount;
    protected Date paymentDate;
    protected String sourceDocNbr;
    protected String fileName;
    
    public ConcurRequestedCashAdvance() {  
    }
    
    public ConcurRequestedCashAdvance(String requestId, String employeeId, KualiDecimal paymentAmount, Date paymentDate, String sourceDocNbr, String fileName) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.sourceDocNbr = sourceDocNbr;
        this.fileName = fileName;
    }

    public int getConcurRequestedCashAdvanceId() {
        return concurRequestedCashAdvanceId;
    }

    public void setConcurRequestedCashAdvanceId(int concurRequestedCashAdvanceId) {
        this.concurRequestedCashAdvanceId = concurRequestedCashAdvanceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public KualiDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(KualiDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getSourceDocNbr() {
        return sourceDocNbr;
    }

    public void setSourceDocNbr(String sourceDocNbr) {
        this.sourceDocNbr = sourceDocNbr;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
