package edu.cornell.kfs.module.purap.jaggaer.xml;

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
    protected JaggaerBasicValue customElementIdentifier;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

    public JaggaerBasicValue getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    public void setCustomElementIdentifier(JaggaerBasicValue value) {
        this.customElementIdentifier = value;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
