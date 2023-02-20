
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
@XmlType(name = "", propOrder = { "contactAddress" })
@XmlRootElement(name = "FulfillmentCenterOrderingContactInfo")
public class FulfillmentCenterOrderingContactInfo {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlElement(name = "ContactAddress")
    protected ContactAddress contactAddress;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public ContactAddress getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(ContactAddress value) {
        this.contactAddress = value;
    }

}
