package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "statusCode", "statusText", "errorMessages" })
@XmlRootElement(name = "Status")
public class Status {
    @XmlElement(name = "StatusCode", required = true)
    private String statusCode;
    @XmlElement(name = "StatusText", required = true)
    private String statusText;
    @XmlElementWrapper(name = "Errors")
    @XmlElement(name = "ErrorMessage", required = true)
    private List<ErrorMessage> errorMessages;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    
    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<ErrorMessage>();
        }
        return errorMessages;
    }

}
