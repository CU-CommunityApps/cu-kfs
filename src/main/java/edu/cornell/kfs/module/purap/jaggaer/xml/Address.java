package edu.cornell.kfs.module.purap.jaggaer.xml;

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
        "active", "prefPurchaseOrderDeliveryMethod", "addressLine1", "addressLine2", "addressLine3", "city", "state",
        "postalCode", "isoCountryCode", "phone", "tollFreePhone", "fax", "notes", "assignedBusinessUnitsList" })
@XmlRootElement(name = "Address")
public class Address {

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
    @XmlElement(name = "PrefPurchaseOrderDeliveryMethod")
    protected PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod;
    @XmlElement(name = "AddressLine1")
    protected AddressLine addressLine1;
    @XmlElement(name = "AddressLine2")
    protected AddressLine addressLine2;
    @XmlElement(name = "AddressLine3")
    protected AddressLine addressLine3;
    @XmlElement(name = "City")
    protected City city;
    @XmlElement(name = "State")
    protected State state;
    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;
    @XmlElement(name = "IsoCountryCode")
    protected IsoCountryCode isoCountryCode;
    @XmlElement(name = "Phone")
    protected Phone phone;
    @XmlElement(name = "TollFreePhone")
    protected TollFreePhone tollFreePhone;
    @XmlElement(name = "Fax")
    protected Fax fax;
    @XmlElement(name = "Notes")
    protected Notes notes;
    @XmlElement(name = "AssignedBusinessUnitsList")
    protected AssignedBusinessUnitsList assignedBusinessUnitsList;

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

    public PrefPurchaseOrderDeliveryMethod getPrefPurchaseOrderDeliveryMethod() {
        return prefPurchaseOrderDeliveryMethod;
    }

    public void setPrefPurchaseOrderDeliveryMethod(PrefPurchaseOrderDeliveryMethod value) {
        this.prefPurchaseOrderDeliveryMethod = value;
    }

    public AddressLine getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine value) {
        this.addressLine1 = value;
    }

    public AddressLine getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine value) {
        this.addressLine2 = value;
    }

    public AddressLine getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(AddressLine value) {
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

    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(IsoCountryCode value) {
        this.isoCountryCode = value;
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

    public Notes getNotes() {
        return notes;
    }

    public void setNotes(Notes value) {
        this.notes = value;
    }

    public AssignedBusinessUnitsList getAssignedBusinessUnitsList() {
        return assignedBusinessUnitsList;
    }

    public void setAssignedBusinessUnitsList(AssignedBusinessUnitsList value) {
        this.assignedBusinessUnitsList = value;
    }

}
