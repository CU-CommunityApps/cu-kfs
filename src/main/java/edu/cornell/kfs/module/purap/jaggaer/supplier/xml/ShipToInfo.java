
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "deliverTo", "address", "phone" })
@XmlRootElement(name = "ShipToInfo")
public class ShipToInfo {

    @XmlElement(name = "DeliverTo")
    protected DeliverTo deliverTo;
    @XmlElement(name = "Address")
    protected Address address;
    @XmlElement(name = "Phone")
    protected Phone phone;

    public DeliverTo getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(DeliverTo value) {
        this.deliverTo = value;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address value) {
        this.address = value;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone value) {
        this.phone = value;
    }

}
