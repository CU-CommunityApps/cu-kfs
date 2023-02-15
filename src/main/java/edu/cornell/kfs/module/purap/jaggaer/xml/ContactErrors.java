package edu.cornell.kfs.module.purap.jaggaer.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "contactRef", "errorMessage" })
@XmlRootElement(name = "ContactErrors")
public class ContactErrors {

    @XmlElement(name = "ContactRef", required = true)
    protected ContactRef contactRef;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

    public ContactRef getContactRef() {
        return contactRef;
    }

    public void setContactRef(ContactRef value) {
        this.contactRef = value;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
