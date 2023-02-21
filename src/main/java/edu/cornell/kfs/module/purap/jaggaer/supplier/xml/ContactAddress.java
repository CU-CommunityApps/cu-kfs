
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "phone", "tollFreePhone", "fax", "email", "address", "notes",
        "businessUnitsAssigned" })
@XmlRootElement(name = "ContactAddress")
public class ContactAddress {

    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "Phone")
    protected Phone phone;
    @XmlElement(name = "TollFreePhone")
    protected TollFreePhone tollFreePhone;
    @XmlElement(name = "Fax")
    protected Fax fax;
    @XmlElement(name = "Email")
    protected Email email;
    @XmlElement(name = "Address")
    protected Address address;
    @XmlElement(name = "Notes")
    protected Notes notes;
    @XmlElement(name = "BusinessUnitsAssigned")
    protected BusinessUnitsAssigned businessUnitsAssigned;

    public Name getName() {
        return name;
    }

    public void setName(Name value) {
        this.name = value;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone value) {
        this.phone = value;
    }

    public TollFreePhone getTollFreePhone() {
        return tollFreePhone;
    }

    public void setTollFreePhone(TollFreePhone value) {
        this.tollFreePhone = value;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address value) {
        this.address = value;
    }

    public Notes getNotes() {
        return notes;
    }

    public void setNotes(Notes value) {
        this.notes = value;
    }

    public BusinessUnitsAssigned getBusinessUnitsAssigned() {
        return businessUnitsAssigned;
    }

    public void setBusinessUnitsAssigned(BusinessUnitsAssigned value) {
        this.businessUnitsAssigned = value;
    }

}
