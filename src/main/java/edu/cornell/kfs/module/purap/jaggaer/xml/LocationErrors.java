package edu.cornell.kfs.module.purap.jaggaer.xml;

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
    protected AddressRef addressRef;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

    public AddressRef getAddressRef() {
        return addressRef;
    }

    public void setAddressRef(AddressRef value) {
        this.addressRef = value;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
