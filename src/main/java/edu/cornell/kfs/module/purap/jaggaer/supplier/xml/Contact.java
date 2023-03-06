
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
@XmlType(name = "", propOrder = {
    "erpNumber",
    "oldERPNumber",
    "sqIntegrationNumber",
    "thirdPartyRefNumber",
    "name",
    "active",
    "firstName",
    "lastName",
    "title",
    "email",
    "phone",
    "mobilePhone",
    "tollFreePhone",
    "fax",
    "notes",
    "associatedAddress"
})
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
    protected FirstName firstName;
    @XmlElement(name = "LastName")
    protected LastName lastName;
    @XmlElement(name = "Title")
    protected Title title;
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
    protected Notes notes;
    @XmlElement(name = "AssociatedAddress")
    protected AssociatedAddress associatedAddress;

    
    public String getType() {
        return type;
    }

    
    public void setType(String value) {
        this.type = value;
    }

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    
    public String getOldERPNumber() {
        return oldERPNumber;
    }

    
    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    
    public Name getName() {
        return name;
    }

    
    public void setName(Name value) {
        this.name = value;
    }

    
    public Active getActive() {
        return active;
    }

    
    public void setActive(Active value) {
        this.active = value;
    }

    
    public FirstName getFirstName() {
        return firstName;
    }

    
    public void setFirstName(FirstName value) {
        this.firstName = value;
    }

    
    public LastName getLastName() {
        return lastName;
    }

    
    public void setLastName(LastName value) {
        this.lastName = value;
    }

    
    public Title getTitle() {
        return title;
    }

    
    public void setTitle(Title value) {
        this.title = value;
    }

    
    public Email getEmail() {
        return email;
    }

    
    public void setEmail(Email value) {
        this.email = value;
    }

    
    public Phone getPhone() {
        return phone;
    }

    
    public void setPhone(Phone value) {
        this.phone = value;
    }

    
    public MobilePhone getMobilePhone() {
        return mobilePhone;
    }

    
    public void setMobilePhone(MobilePhone value) {
        this.mobilePhone = value;
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

    
    public Notes getNotes() {
        return notes;
    }

    
    public void setNotes(Notes value) {
        this.notes = value;
    }

    
    public AssociatedAddress getAssociatedAddress() {
        return associatedAddress;
    }

    
    public void setAssociatedAddress(AssociatedAddress value) {
        this.associatedAddress = value;
    }

}
