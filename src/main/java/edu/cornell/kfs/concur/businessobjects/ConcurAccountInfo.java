package edu.cornell.kfs.concur.businessobjects;

import org.kuali.kfs.sys.KFSConstants;

public class ConcurAccountInfo {
    protected String chart;
    protected String accountNumber;
    protected String subAccountNumber;
    protected String objectCode;
    protected String subObjectCode;
    protected String projectCode;
    protected String orgRefId;

    public ConcurAccountInfo(String chart, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode, String orgRefId) {
        this.setChart(chart);
        this.setAccountNumber(accountNumber);
        this.setSubAccountNumber(subAccountNumber);
        this.setObjectCode(objectCode);
        this.setSubObjectCode(subObjectCode);
        this.setProjectCode(projectCode);
        this.setOrgRefId(orgRefId);
    }

    public String getChart() {
        return chart;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Concur Account info: ");
        result.append(KFSConstants.NEWLINE);
        result.append("Chart: " + this.chart);
        result.append(KFSConstants.NEWLINE);
        result.append("Account number: " + this.accountNumber);
        result.append(KFSConstants.NEWLINE);
        result.append("Sub Account number: " + this.subAccountNumber);
        result.append(KFSConstants.NEWLINE);
        result.append("Object code: " + this.objectCode);
        result.append(KFSConstants.NEWLINE);
        result.append("Sub object code: " + this.subObjectCode);
        result.append(KFSConstants.NEWLINE);
        result.append("Project code: " + this.projectCode);
        result.append(KFSConstants.NEWLINE);
        result.append("Org Ref Id: " + this.orgRefId);
        result.append(KFSConstants.NEWLINE);

        return result.toString();
    }
}
