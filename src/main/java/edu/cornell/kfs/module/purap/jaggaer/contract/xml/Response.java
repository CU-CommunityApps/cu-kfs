package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "status",
    "errors"
})
@XmlRootElement(name = "Response")
public class Response {

    @XmlElement(name = "Status", required = true)
    private Status status;

    @XmlElement(name = "Error")
    private List<Error> errors;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Error> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

}
