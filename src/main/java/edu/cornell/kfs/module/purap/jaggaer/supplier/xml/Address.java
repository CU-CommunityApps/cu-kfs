
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "addressLine1", "addressLine2", "addressLine3", "city", "state", "postalCode",
        "country" })
@XmlRootElement(name = "Address")
public class Address {

    @XmlElement(name = "AddressLine1")
    protected AddressLine1 addressLine1;
    @XmlElement(name = "AddressLine2")
    protected AddressLine2 addressLine2;
    @XmlElement(name = "AddressLine3")
    protected AddressLine3 addressLine3;
    @XmlElement(name = "City")
    protected City city;
    @XmlElement(name = "State")
    protected State state;
    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;
    @XmlElement(name = "Country")
    protected Country country;

    public AddressLine1 getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine1 value) {
        this.addressLine1 = value;
    }

    public AddressLine2 getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine2 value) {
        this.addressLine2 = value;
    }

    public AddressLine3 getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(AddressLine3 value) {
        this.addressLine3 = value;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City value) {
        this.city = value;
    }

    public State getState() {
        return state;
    }

    public void setState(State value) {
        this.state = value;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country value) {
        this.country = value;
    }

}
