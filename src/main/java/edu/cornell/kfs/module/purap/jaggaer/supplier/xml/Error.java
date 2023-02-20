
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "location", "errorMessage" })
@XmlRootElement(name = "Error")
public class Error {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlElement(name = "Location")
    protected Location location;
    @XmlElement(name = "ErrorMessage")
    protected ErrorMessage errorMessage;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location value) {
        this.location = value;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(ErrorMessage value) {
        this.errorMessage = value;
    }

}
