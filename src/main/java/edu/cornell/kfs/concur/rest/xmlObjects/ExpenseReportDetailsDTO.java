package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

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
    
    @XmlElement(name = "OrgUnit4")
    private String orgUnit4;
    
    @XmlElement(name = "OrgUnit5")
    private String orgUnit5;

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

    public String getOrgUnit4() {
        return orgUnit4;
    }

    public void setOrgUnit4(String orgUnit4) {
        this.orgUnit4 = orgUnit4;
    }

    public String getOrgUnit5() {
        return orgUnit5;
    }

    public void setOrgUnit5(String orgUnit5) {
        this.orgUnit5 = orgUnit5;
    }

}
