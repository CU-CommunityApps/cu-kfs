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
    
    @XmlElement(name = "ApsKey")
    private String concurStatusCode;
    
    @XmlElement(name = "WorkflowActionURL")
    private String workflowActionURL;

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

}
