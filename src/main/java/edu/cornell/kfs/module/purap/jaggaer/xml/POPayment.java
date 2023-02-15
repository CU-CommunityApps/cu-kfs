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
@XmlType(name = "", propOrder = { "active", "poNumberSelection", "allowFreeForm" })
@XmlRootElement(name = "POPayment")
public class POPayment {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "PONumberSelection")
    protected PONumberSelection poNumberSelection;
    @XmlElement(name = "AllowFreeForm", required = true)
    protected AllowFreeForm allowFreeForm;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public PONumberSelection getPONumberSelection() {
        return poNumberSelection;
    }

    public void setPONumberSelection(PONumberSelection value) {
        this.poNumberSelection = value;
    }

    public AllowFreeForm getAllowFreeForm() {
        return allowFreeForm;
    }

    public void setAllowFreeForm(AllowFreeForm value) {
        this.allowFreeForm = value;
    }

}
