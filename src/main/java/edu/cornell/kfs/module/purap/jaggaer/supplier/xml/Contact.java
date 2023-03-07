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
@XmlType(name = "", propOrder = { "erpNumber", "oldERPNumber", "sqIntegrationNumber", "thirdPartyRefNumber", "name",
        "active", "firstName", "lastName", "title", "email", "phone", "mobilePhone", "tollFreePhone", "fax", "notes",
        "associatedAddress" })
@XmlRootElement(name = "Contact")
public class Contact {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "ERPNumber")
    protected ERPNumber erpNumber;
    @XmlElement(name = "OldERPNumber")
    protected String oldERPNumber;
    @XmlElement(name = "SQIntegrationNumber")
    protected SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "FirstName")
    protected JaggaerBasicValue firstName;
    @XmlElement(name = "LastName")
    protected JaggaerBasicValue lastName;
    @XmlElement(name = "Title")
    protected JaggaerBasicValue title;
    @XmlElement(name = "Email")
    protected Email email;
    @XmlElement(name = "Phone")
    protected Phone phone;
    @XmlElement(name = "MobilePhone")
    protected MobilePhone mobilePhone;
    @XmlElement(name = "TollFreePhone")
    protected TollFreePhone tollFreePhone;
    @XmlElement(name = "Fax")
    protected Fax fax;
    @XmlElement(name = "Notes")
    protected JaggaerBasicValue notes;
    @XmlElement(name = "AssociatedAddress")
    protected AssociatedAddress associatedAddress;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public ERPNumber getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(ERPNumber erpNumber) {
        this.erpNumber = erpNumber;
    }

    public String getOldERPNumber() {
        return oldERPNumber;
    }

    public void setOldERPNumber(String oldERPNumber) {
        this.oldERPNumber = oldERPNumber;
    }

    public SQIntegrationNumber getSqIntegrationNumber() {
        return sqIntegrationNumber;
    }

    public void setSqIntegrationNumber(SQIntegrationNumber sqIntegrationNumber) {
        this.sqIntegrationNumber = sqIntegrationNumber;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber thirdPartyRefNumber) {
        this.thirdPartyRefNumber = thirdPartyRefNumber;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public JaggaerBasicValue getFirstName() {
        return firstName;
    }

    public void setFirstName(JaggaerBasicValue firstName) {
        this.firstName = firstName;
    }

    public JaggaerBasicValue getLastName() {
        return lastName;
    }

    public void setLastName(JaggaerBasicValue lastName) {
        this.lastName = lastName;
    }

    public JaggaerBasicValue getTitle() {
        return title;
    }

    public void setTitle(JaggaerBasicValue title) {
        this.title = title;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public MobilePhone getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(MobilePhone mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public TollFreePhone getTollFreePhone() {
        return tollFreePhone;
    }

    public void setTollFreePhone(TollFreePhone tollFreePhone) {
        this.tollFreePhone = tollFreePhone;
    }

    public Fax getFax() {
        return fax;
    }

    public void setFax(Fax fax) {
        this.fax = fax;
    }

    public JaggaerBasicValue getNotes() {
        return notes;
    }

    public void setNotes(JaggaerBasicValue notes) {
        this.notes = notes;
    }

    public AssociatedAddress getAssociatedAddress() {
        return associatedAddress;
    }

    public void setAssociatedAddress(AssociatedAddress associatedAddress) {
        this.associatedAddress = associatedAddress;
    }

}
