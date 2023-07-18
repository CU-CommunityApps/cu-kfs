package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "addressRef", "errorMessages" })
@XmlRootElement(name = "AddressErrors")
public class AddressError {

    @XmlElement(name = "AddressRef", required = true)
    private AddressRef addressRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessages;

    public AddressRef getAddressRef() {
        return addressRef;
    }

    public void setAddressRef(AddressRef addressRef) {
        this.addressRef = addressRef;
    }

    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        return errorMessages;
    }

}
