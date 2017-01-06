package edu.cornell.kfs.concur.businessobjects;

public class ConcurAccountInfo {
    protected String chart;
    protected String accountNumber;
    protected String subAccountNumber;
    protected String objectCode;
    protected String subObjectCode;
    protected String projectCode;
    protected String orgRefId;
    
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
}
