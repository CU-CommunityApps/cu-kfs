package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ReportDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class ExpenseReportDetailsDTO {
    
    @XmlElement(name = "ReportId")
    private String reportId;
    
    @XmlElement(name = "ApsKey")
    private String concurStatusCode;
    
    @XmlElement(name = "WorkflowActionURL")
    private String workflowActionURL;
    
    @XmlElement(name = "OrgUnit1")
    private String orgUnit1;
    
    @XmlElement(name = "OrgUnit2")
    private String orgUnit2;
    
    @XmlElement(name = "OrgUnit3")
    private String orgUnit3;

    @XmlElementWrapper(name = "Entries")
    @XmlElement(name = "ExpenseEntry")
    private List<ExpenseEntryDTO> entries;

    public String getWorkflowActionURL() {
        return workflowActionURL;
    }

    public void setWorkflowActionURL(String workflowActionURL) {
        this.workflowActionURL = workflowActionURL;
    }

    public List<ExpenseEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<ExpenseEntryDTO> entries) {
        this.entries = entries;
    }

    public String getConcurStatusCode() {
        return concurStatusCode;
    }

    public void setConcurStatusCode(String concurStatusCode) {
        this.concurStatusCode = concurStatusCode;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getOrgUnit1() {
        return orgUnit1;
    }

    public void setOrgUnit1(String orgUnit1) {
        this.orgUnit1 = orgUnit1;
    }

    public String getOrgUnit2() {
        return orgUnit2;
    }

    public void setOrgUnit2(String orgUnit2) {
        this.orgUnit2 = orgUnit2;
    }

    public String getOrgUnit3() {
        return orgUnit3;
    }

    public void setOrgUnit3(String orgUnit3) {
        this.orgUnit3 = orgUnit3;
    }

}
