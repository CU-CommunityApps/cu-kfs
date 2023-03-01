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
@XmlType(name = "", propOrder = { "internalName", "displayName" })
@XmlRootElement(name = "Classification")
public class Classification {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "InternalName", required = true)
    protected JaggaerBasicValue internalName;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getInternalName() {
        return internalName;
    }

    public void setInternalName(JaggaerBasicValue internalName) {
        this.internalName = internalName;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName displayName) {
        this.displayName = displayName;
    }

}
