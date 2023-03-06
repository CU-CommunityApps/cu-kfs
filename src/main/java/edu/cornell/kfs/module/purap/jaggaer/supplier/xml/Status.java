
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "statusCode",
    "statusText",
    "errors"
})
@XmlRootElement(name = "Status")
public class Status {

    @XmlElement(name = "StatusCode", required = true)
    protected String statusCode;
    @XmlElement(name = "StatusText", required = true)
    protected String statusText;
    @XmlElement(name = "Errors")
    protected Errors errors;

    
    public String getStatusCode() {
        return statusCode;
    }

    
    public void setStatusCode(String value) {
        this.statusCode = value;
    }

    
    public String getStatusText() {
        return statusText;
    }

    
    public void setStatusText(String value) {
        this.statusText = value;
    }

    
    public Errors getErrors() {
        return errors;
    }

    
    public void setErrors(Errors value) {
        this.errors = value;
    }

}
