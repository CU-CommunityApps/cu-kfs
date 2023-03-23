package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
    FirstParty.class,
    SecondParty.class
})
@XmlType(name = "", propOrder = {
    "name",
    "sciquestId",
    "erpNumber",
    "contactId",
    "addressId"
})
public abstract class ContractPartyBase {

    @XmlAttribute(name = "isPrimary")
    private boolean primary;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "SciquestId")
    private String sciquestId;

    @XmlElement(name = "ERPNumber")
    private String erpNumber;

    @XmlElement(name = "ContactId")
    private String contactId;

    @XmlElement(name = "AddressId")
    private String addressId;

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSciquestId() {
        return sciquestId;
    }

    public void setSciquestId(String sciquestId) {
        this.sciquestId = sciquestId;
    }

    public String getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(String erpNumber) {
        this.erpNumber = erpNumber;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

}
