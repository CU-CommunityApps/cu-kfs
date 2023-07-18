package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customElementIdentifier", "errorMessages" })
@XmlRootElement(name = "CustomElementErrors")
public class CustomElementError {
    @XmlElement(name = "CustomElementIdentifier", required = true)
    private JaggaerBasicValue customElementIdentifier;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessages;

    public JaggaerBasicValue getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    public void setCustomElementIdentifier(JaggaerBasicValue customElementIdentifier) {
        this.customElementIdentifier = customElementIdentifier;
    }

    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<ErrorMessage>();
        }
        return errorMessages;
    }
}
