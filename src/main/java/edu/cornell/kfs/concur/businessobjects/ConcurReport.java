package edu.cornell.kfs.concur.businessobjects;

import java.util.List;

public class ConcurReport {
    private String concurStatusCode;
    private String workflowURI;    
    private List<ConcurAccountInfo> accountInfos;

    public ConcurReport(String concurStatusCode, String workflowURI, List<ConcurAccountInfo> accountInfos) {
        this.concurStatusCode = concurStatusCode;
        this.workflowURI = workflowURI;
        this.accountInfos = accountInfos;
    }
    
    public String getConcurStatusCode() {
        return concurStatusCode;
    }

    public void setConcurStatusCode(String concurStatusCode) {
        this.concurStatusCode = concurStatusCode;
    }

    public String getWorkflowURI() {
        return workflowURI;
    }

    public void setWorkflowURI(String workflowURI) {
        this.workflowURI = workflowURI;
    }

    public List<ConcurAccountInfo> getAccountInfos() {
        return accountInfos;
    }

    public void setAccountInfos(List<ConcurAccountInfo> accountInfos) {
        this.accountInfos = accountInfos;
    }

}
