
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "statusCode", "statusText" })
@XmlRootElement(name = "Status")
public class Status {

    @XmlElement(name = "StatusCode")
    protected StatusCode statusCode;
    @XmlElement(name = "StatusText")
    protected StatusText statusText;

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode value) {
        this.statusCode = value;
    }

    public StatusText getStatusText() {
        return statusText;
    }

    public void setStatusText(StatusText value) {
        this.statusText = value;
    }

}
