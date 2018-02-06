package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class PaymentWorksAddressBaseDTO {

    @XmlElement(name = "street1")
    protected String street1;
    
    @XmlElement(name = "street2")
    protected String street2;
    
    @XmlElement(name = "city")
    protected String city;
    
    @XmlElement(name = "state")
    protected String state;
    
    @XmlElement(name = "country")
    protected String country;
    
    @XmlElement(name = "zipcode")
    protected String zipcode;
    
    @XmlElement(name = "validated")
    protected boolean validated;
    
    @XmlElement(name = "address_type")
    protected String addressType;

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public boolean getValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

}
