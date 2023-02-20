
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
