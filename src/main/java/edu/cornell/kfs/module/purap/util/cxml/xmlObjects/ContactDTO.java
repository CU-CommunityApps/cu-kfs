package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "postalAddresses",
    "emails",
    "phones",
    "faxes",
    "urls"
})
@XmlRootElement(name = "Contact")
public class ContactDTO {

    @XmlAttribute(name = "role")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String role;

    @XmlAttribute(name = "addressID")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String addressID;

    @XmlElement(name = "Name", required = true)
    private NameDTO name;

    @XmlElement(name = "PostalAddress")
    private List<PostalAddressDTO> postalAddresses;

    @XmlElement(name = "Email")
    private List<EmailDTO> emails;

    @XmlElement(name = "Phone")
    private List<PhoneDTO> phones;

    @XmlElement(name = "Fax")
    private List<FaxDTO> faxes;

    @XmlElement(name = "URL")
    private List<UrlDTO> urls;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public List<PostalAddressDTO> getPostalAddresses() {
        return postalAddresses;
    }

    public void setPostalAddresses(List<PostalAddressDTO> postalAddresses) {
        this.postalAddresses = postalAddresses;
    }

    public List<EmailDTO> getEmails() {
        return emails;
    }

    public void setEmails(List<EmailDTO> emails) {
        this.emails = emails;
    }

    public List<PhoneDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneDTO> phones) {
        this.phones = phones;
    }

    public List<FaxDTO> getFaxes() {
        return faxes;
    }

    public void setFaxes(List<FaxDTO> faxes) {
        this.faxes = faxes;
    }

    public List<UrlDTO> getUrls() {
        return urls;
    }

    public void setUrls(List<UrlDTO> urls) {
        this.urls = urls;
    }

}
