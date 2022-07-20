package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
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
    "deliveryRecipients",
    "streetAddressLines",
    "city",
    "state",
    "postalCode",
    "country"
})
@XmlRootElement(name = "PostalAddress")
public class PostalAddressDTO {

    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;

    @XmlElement(name = "DeliverTo")
    private List<String> deliveryRecipients;

    @XmlElement(name = "Street", required = true)
    private List<String> streetAddressLines;

    @XmlElement(name = "City", required = true)
    private String city;

    @XmlElement(name = "State")
    private String state;

    @XmlElement(name = "PostalCode")
    private String postalCode;

    @XmlElement(name = "Country", required = true)
    private CountryDTO country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDeliveryRecipients() {
        return deliveryRecipients;
    }

    public void setDeliveryRecipients(List<String> deliveryRecipients) {
        this.deliveryRecipients = deliveryRecipients;
    }

    public List<String> getStreetAddressLines() {
        return streetAddressLines;
    }

    public void setStreetAddressLines(List<String> streetAddressLines) {
        this.streetAddressLines = streetAddressLines;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public void setCountry(CountryDTO country) {
        this.country = country;
    }

}
