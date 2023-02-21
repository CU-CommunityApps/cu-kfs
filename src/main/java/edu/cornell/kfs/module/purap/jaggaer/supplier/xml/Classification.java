
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "internalName", "displayName" })
@XmlRootElement(name = "Classification")
public class Classification {

    @XmlElement(name = "InternalName")
    protected InternalName internalName;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;

    public InternalName getInternalName() {
        return internalName;
    }

    public void setInternalName(InternalName value) {
        this.internalName = value;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName value) {
        this.displayName = value;
    }

}
