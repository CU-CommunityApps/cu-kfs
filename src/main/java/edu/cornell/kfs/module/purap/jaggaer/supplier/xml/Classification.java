
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
