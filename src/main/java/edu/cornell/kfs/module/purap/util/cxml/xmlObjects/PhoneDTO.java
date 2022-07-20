package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "telephoneNumber"
})
@XmlRootElement(name = "Phone")
public class PhoneDTO {

    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;

    @XmlElement(name = "TelephoneNumber", required = true)
    private TelephoneNumberDTO telephoneNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TelephoneNumberDTO getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(TelephoneNumberDTO telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

}
