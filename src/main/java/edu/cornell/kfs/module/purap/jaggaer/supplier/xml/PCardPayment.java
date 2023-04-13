package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "active", "poNumberSelection", "requireCardSecurityCode" })
@XmlRootElement(name = "PCardPayment")
public class PCardPayment {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "Active")
    private Active active;
    @XmlElement(name = "PONumberSelection")
    private PONumberSelection poNumberSelection;
    @XmlElement(name = "RequireCardSecurityCode")
    private JaggaerBasicValue requireCardSecurityCode;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public PONumberSelection getPoNumberSelection() {
        return poNumberSelection;
    }

    public void setPoNumberSelection(PONumberSelection poNumberSelection) {
        this.poNumberSelection = poNumberSelection;
    }

    public JaggaerBasicValue getRequireCardSecurityCode() {
        return requireCardSecurityCode;
    }

    public void setRequireCardSecurityCode(JaggaerBasicValue requireCardSecurityCode) {
        this.requireCardSecurityCode = requireCardSecurityCode;
    }

}
