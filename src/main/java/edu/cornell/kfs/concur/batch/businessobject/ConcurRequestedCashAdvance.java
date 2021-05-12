package edu.cornell.kfs.concur.batch.businessobject;

import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class ConcurRequestedCashAdvance extends PersistableBusinessObjectBase{
    protected int concurRequestedCashAdvanceId;
    protected String requestId;
    protected String employeeId;
    protected KualiDecimal paymentAmount;
    protected Date paymentDate;
    protected String sourceDocNbr;
    protected String cashAdvanceKey;
    protected String chart;
    protected String accountNumber;
    protected String subAccountNumber;
    protected String objectCode;
    protected String subObjectCode;
    protected String projectCode;
    protected String orgRefId;
    protected String fileName;
    
    public ConcurRequestedCashAdvance() {  
    }
    
    public ConcurRequestedCashAdvance(String requestId, String employeeId, KualiDecimal paymentAmount, Date paymentDate, String sourceDocNbr,
                                      String cashAdvanceKey, String chart, String accountNumber, String subAccountNumber, String objectCode,
                                      String subObjectCode, String projectCode, String orgRefId, String fileName) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.sourceDocNbr = sourceDocNbr;
        this.cashAdvanceKey = cashAdvanceKey;
        this.chart = chart;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
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

    public String getCashAdvanceKey() {
        return cashAdvanceKey;
    }

    public void setCashAdvanceKey(String cashAdvanceKey) {
        this.cashAdvanceKey = cashAdvanceKey;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }

}
