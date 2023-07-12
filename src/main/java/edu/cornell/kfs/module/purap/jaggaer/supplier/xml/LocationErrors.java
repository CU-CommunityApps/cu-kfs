package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "addressRef", "errorMessage" })
@XmlRootElement(name = "LocationErrors")
public class LocationErrors {
    @XmlElement(name = "AddressRef", required = true)
    private AddressRef addressRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessage;

    public AddressRef getAddressRef() {
        return addressRef;
    }

    public void setAddressRef(AddressRef addressRef) {
        this.addressRef = addressRef;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return errorMessage;
    }
}
