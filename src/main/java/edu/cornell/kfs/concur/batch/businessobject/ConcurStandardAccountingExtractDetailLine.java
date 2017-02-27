package edu.cornell.kfs.concur.batch.businessobject;

public class ConcurStandardAccountingExtractDetailLine {
    
    private String detailType; //1
    private String batchID; //2
    private String batchDate; //3
    private String sequenceNumber; //4
    private String employeeId; //5
    private String employeeLastName; //6
    private String employeeFirstName; //7
    private String employeeMiddleInitital; //8
    private String employeeGroupId; //9
    private String reportId; //19
    private String reportCustom1; //41
    private String paymentCode; //127
    private String journalAccountCode; //167
    private String allocationCustom1; //191
    private String allocationCustom2; //192
    private String allocationCustom3; //193
    private String allocationCustom4; //194
    private String allocationCustom5; //195
    private String allocationCustom6; //196
    
    public String getDetailType() {
        return detailType;
    }
    public void setDetailType(String detailType) {
        this.detailType = detailType;
    }
    public String getBatchID() {
        return batchID;
    }
    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }
    public String getBatchDate() {
        return batchDate;
    }
    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    public String getEmployeeLastName() {
        return employeeLastName;
    }
    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }
    public String getEmployeeFirstName() {
        return employeeFirstName;
    }
    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }
    public String getEmployeeMiddleInitital() {
        return employeeMiddleInitital;
    }
    public void setEmployeeMiddleInitital(String employeeMiddleInitital) {
        this.employeeMiddleInitital = employeeMiddleInitital;
    }
    public String getEmployeeGroupId() {
        return employeeGroupId;
    }
    public void setEmployeeGroupId(String employeeGroupId) {
        this.employeeGroupId = employeeGroupId;
    }
    public String getReportId() {
        return reportId;
    }
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    public String getReportCustom1() {
        return reportCustom1;
    }
    public void setReportCustom1(String reportCustom1) {
        this.reportCustom1 = reportCustom1;
    }
    public String getPaymentCode() {
        return paymentCode;
    }
    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }
    public String getJournalAccountCode() {
        return journalAccountCode;
    }
    public void setJournalAccountCode(String journalAccountCode) {
        this.journalAccountCode = journalAccountCode;
    }
    public String getAllocationCustom1() {
        return allocationCustom1;
    }
    public void setAllocationCustom1(String allocationCustom1) {
        this.allocationCustom1 = allocationCustom1;
    }
    public String getAllocationCustom2() {
        return allocationCustom2;
    }
    public void setAllocationCustom2(String allocationCustom2) {
        this.allocationCustom2 = allocationCustom2;
    }
    public String getAllocationCustom3() {
        return allocationCustom3;
    }
    public void setAllocationCustom3(String allocationCustom3) {
        this.allocationCustom3 = allocationCustom3;
    }
    public String getAllocationCustom4() {
        return allocationCustom4;
    }
    public void setAllocationCustom4(String allocationCustom4) {
        this.allocationCustom4 = allocationCustom4;
    }
    public String getAllocationCustom5() {
        return allocationCustom5;
    }
    public void setAllocationCustom5(String allocationCustom5) {
        this.allocationCustom5 = allocationCustom5;
    }
    public String getAllocationCustom6() {
        return allocationCustom6;
    }
    public void setAllocationCustom6(String allocationCustom6) {
        this.allocationCustom6 = allocationCustom6;
    }
    
    public String getDebugInformation() {
        StringBuilder sb = new StringBuilder("detailType: ").append(detailType);
        sb.append(" batchID: ").append(batchID).append(" batchDate: ").append(batchDate);
        sb.append(" sequenceNumber: ").append(sequenceNumber).append(" employeeId: ").append(employeeId);
        sb.append(" employeeLastName: ").append(employeeLastName).append(" employeeFirstName: ").append(employeeFirstName);
        sb.append(" employeeMiddleInitital: ").append(employeeMiddleInitital).append(" employeeGroupId: ").append(employeeGroupId);
        sb.append(" reportId: ").append(reportId).append(" reportCustom1: ").append(reportCustom1);
        sb.append(" paymentCode: ").append(paymentCode).append(" journalAccountCode: ").append(journalAccountCode);
        sb.append(" allocationCustom1: ").append(allocationCustom1).append(" allocationCustom2: ").append(allocationCustom2);
        sb.append(" allocationCustom3: ").append(allocationCustom3).append(" allocationCustom4: ").append(allocationCustom4);
        sb.append(" allocationCustom5: ").append(allocationCustom5).append(" allocationCustom6: ").append(allocationCustom6);
        return sb.toString();
    }

}
