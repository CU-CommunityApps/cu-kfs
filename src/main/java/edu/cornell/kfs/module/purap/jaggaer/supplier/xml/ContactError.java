package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "contactRef", "errorMessages" })
@XmlRootElement(name = "ContactErrors")
public class ContactError {
    @XmlElement(name = "ContactRef", required = true)
    private ContactRef contactRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessages;

    public ContactRef getContactRef() {
        return contactRef;
    }

    public void setContactRef(ContactRef contactRef) {
        this.contactRef = contactRef;
    }

    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<ErrorMessage>();
        }
        return errorMessages;
    }
}
