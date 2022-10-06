package edu.cornell.kfs.concur.rest.request.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.concur.ConcurConstants.ConcurXmlNamespaces;

@XmlRootElement(namespace = ConcurXmlNamespaces.TRAVEL_REQUEST, name = "TravelRequestDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class TravelRequestDetailsDTO {

    @XmlElement(name = "RequestID")
    private String requestID;
    
    @XmlElement(name = "ApprovalStatusKey")
    private String concurStatucCode;
    
    @XmlElement(name = "WorkflowActionURL")
    private String workflowActionURL;

    @XmlElement(name = "Custom1")
    private String custom1;

    @XmlElement(name = "Custom2")
    private String custom2;

    @XmlElement(name = "Custom3")
    private String custom3;

    @XmlElement(name = "Custom4")
    private String custom4;

    @XmlElement(name = "Custom5")
    private String custom5;

    @XmlElement(name = "Custom6")
    private String custom6;

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public String getCustom4() {
        return custom4;
    }

    public void setCustom4(String custom4) {
        this.custom4 = custom4;
    }

    public String getCustom5() {
        return custom5;
    }

    public void setCustom5(String custom5) {
        this.custom5 = custom5;
    }

    public String getCustom6() {
        return custom6;
    }

    public void setCustom6(String custom6) {
        this.custom6 = custom6;
    }

    public String getWorkflowActionURL() {
        return workflowActionURL;
    }

    public void setWorkflowActionURL(String workflowActionURL) {
        this.workflowActionURL = workflowActionURL;
    }

    public String getConcurStatucCode() {
        return concurStatucCode;
    }

    public void setConcurStatucCode(String concurStatucCode) {
        this.concurStatucCode = concurStatucCode;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
}
