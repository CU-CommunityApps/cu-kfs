//
package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "blanketPONumber" })
@XmlRootElement(name = "BlanketPOPaymentMethod")
public class BlanketPOPaymentMethod {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "BlanketPONumber", required = true)
    protected JaggaerBasicValue blanketPONumber;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getBlanketPONumber() {
        return blanketPONumber;
    }

    public void setBlanketPONumber(JaggaerBasicValue blanketPONumber) {
        this.blanketPONumber = blanketPONumber;
    }

}
