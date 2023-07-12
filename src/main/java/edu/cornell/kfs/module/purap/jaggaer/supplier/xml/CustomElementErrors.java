package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customElementIdentifier", "errorMessage" })
@XmlRootElement(name = "CustomElementErrors")
public class CustomElementErrors {
    @XmlElement(name = "CustomElementIdentifier", required = true)
    private JaggaerBasicValue customElementIdentifier;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessage;

    public JaggaerBasicValue getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    public void setCustomElementIdentifier(JaggaerBasicValue customElementIdentifier) {
        this.customElementIdentifier = customElementIdentifier;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return errorMessage;
    }
}
