
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "contactHeader", "contactAddress" })
@XmlRootElement(name = "SupplierContactInfo")
public class SupplierContactInfo {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "usePreferred")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String usePreferred;
    @XmlElement(name = "ContactHeader")
    protected ContactHeader contactHeader;
    @XmlElement(name = "ContactAddress")
    protected ContactAddress contactAddress;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getUsePreferred() {
        return usePreferred;
    }

    public void setUsePreferred(String value) {
        this.usePreferred = value;
    }

    public ContactHeader getContactHeader() {
        return contactHeader;
    }

    public void setContactHeader(ContactHeader value) {
        this.contactHeader = value;
    }

    public ContactAddress getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(ContactAddress value) {
        this.contactAddress = value;
    }

}
