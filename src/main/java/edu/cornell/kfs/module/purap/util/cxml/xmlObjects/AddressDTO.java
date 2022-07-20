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
    "name",
    "postalAddress",
    "email",
    "phone",
    "fax",
    "url"
})
@XmlRootElement(name = "Address")
public class AddressDTO {

    @XmlAttribute(name = "isoCountryCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String isoCountryCode;

    @XmlAttribute(name = "addressID")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String addressID;

    @XmlElement(name = "Name", required = true)
    private NameDTO name;

    @XmlElement(name = "PostalAddress")
    private PostalAddressDTO postalAddress;

    @XmlElement(name = "Email")
    private EmailDTO email;

    @XmlElement(name = "Phone")
    private PhoneDTO phone;

    @XmlElement(name = "Fax")
    private FaxDTO fax;

    @XmlElement(name = "URL")
    private UrlDTO url;

    public String getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(String isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    public NameDTO getName() {
        return name;
    }

    public void setName(NameDTO name) {
        this.name = name;
    }

    public PostalAddressDTO getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddressDTO postalAddress) {
        this.postalAddress = postalAddress;
    }

    public EmailDTO getEmail() {
        return email;
    }

    public void setEmail(EmailDTO email) {
        this.email = email;
    }

    public PhoneDTO getPhone() {
        return phone;
    }

    public void setPhone(PhoneDTO phone) {
        this.phone = phone;
    }

    public FaxDTO getFax() {
        return fax;
    }

    public void setFax(FaxDTO fax) {
        this.fax = fax;
    }

    public UrlDTO getURL() {
        return url;
    }

    public void setURL(UrlDTO url) {
        this.url = url;
    }

}
