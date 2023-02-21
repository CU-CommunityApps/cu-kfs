
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
@XmlType(name = "", propOrder = { "active", "fax", "email" })
@XmlRootElement(name = "DistributionMethod")
public class DistributionMethod {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "Fax")
    protected Fax fax;
    @XmlElement(name = "Email")
    protected Email email;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public Fax getFax() {
        return fax;
    }

    public void setFax(Fax value) {
        this.fax = value;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email value) {
        this.email = value;
    }

}
