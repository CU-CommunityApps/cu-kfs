
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


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "erpNumber",
    "oldERPNumber",
    "sqIntegrationNumber",
    "thirdPartyRefNumber",
    "name",
    "active",
    "prefPurchaseOrderDeliveryMethod",
    "addressLine1",
    "addressLine2",
    "addressLine3",
    "city",
    "state",
    "postalCode",
    "isoCountryCode",
    "phone",
    "tollFreePhone",
    "fax",
    "notes",
    "assignedBusinessUnitsList"
})
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

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the isChanged property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsChanged() {
        return isChanged;
    }

    /**
     * Sets the value of the isChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    /**
     * Gets the value of the erpNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ERPNumber }
     *     
     */
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    /**
     * Sets the value of the erpNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERPNumber }
     *     
     */
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    /**
     * Gets the value of the oldERPNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldERPNumber() {
        return oldERPNumber;
    }

    /**
     * Sets the value of the oldERPNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    /**
     * Gets the value of the sqIntegrationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    /**
     * Sets the value of the sqIntegrationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    /**
     * Gets the value of the thirdPartyRefNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    /**
     * Sets the value of the thirdPartyRefNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Active }
     *     
     */
    public Active getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Active }
     *     
     */
    public void setActive(Active value) {
        this.active = value;
    }

    /**
     * Gets the value of the prefPurchaseOrderDeliveryMethod property.
     * 
     * @return
     *     possible object is
     *     {@link PrefPurchaseOrderDeliveryMethod }
     *     
     */
    public PrefPurchaseOrderDeliveryMethod getPrefPurchaseOrderDeliveryMethod() {
        return prefPurchaseOrderDeliveryMethod;
    }

    /**
     * Sets the value of the prefPurchaseOrderDeliveryMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrefPurchaseOrderDeliveryMethod }
     *     
     */
    public void setPrefPurchaseOrderDeliveryMethod(PrefPurchaseOrderDeliveryMethod value) {
        this.prefPurchaseOrderDeliveryMethod = value;
    }

    /**
     * Gets the value of the addressLine1 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine1 }
     *     
     */
    public AddressLine1 getAddressLine1() {
        return addressLine1;
    }

    /**
     * Sets the value of the addressLine1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine1 }
     *     
     */
    public void setAddressLine1(AddressLine1 value) {
        this.addressLine1 = value;
    }

    /**
     * Gets the value of the addressLine2 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine2 }
     *     
     */
    public AddressLine2 getAddressLine2() {
        return addressLine2;
    }

    /**
     * Sets the value of the addressLine2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine2 }
     *     
     */
    public void setAddressLine2(AddressLine2 value) {
        this.addressLine2 = value;
    }

    /**
     * Gets the value of the addressLine3 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine3 }
     *     
     */
    public AddressLine3 getAddressLine3() {
        return addressLine3;
    }

    /**
     * Sets the value of the addressLine3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine3 }
     *     
     */
    public void setAddressLine3(AddressLine3 value) {
        this.addressLine3 = value;
    }

    /**
     * Gets the value of the city property.
     * 
     * @return
     *     possible object is
     *     {@link City }
     *     
     */
    public City getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     * 
     * @param value
     *     allowed object is
     *     {@link City }
     *     
     */
    public void setCity(City value) {
        this.city = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link State }
     *     
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link State }
     *     
     */
    public void setState(State value) {
        this.state = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link PostalCode }
     *     
     */
    public PostalCode getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalCode }
     *     
     */
    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

    /**
     * Gets the value of the isoCountryCode property.
     * 
     * @return
     *     possible object is
     *     {@link IsoCountryCode }
     *     
     */
    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    /**
     * Sets the value of the isoCountryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link IsoCountryCode }
     *     
     */
    public void setIsoCountryCode(IsoCountryCode value) {
        this.isoCountryCode = value;
    }

    /**
     * Gets the value of the phone property.
     * 
     * @return
     *     possible object is
     *     {@link Phone }
     *     
     */
    public Phone getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     * 
     * @param value
     *     allowed object is
     *     {@link Phone }
     *     
     */
    public void setPhone(Phone value) {
        this.phone = value;
    }

    /**
     * Gets the value of the tollFreePhone property.
     * 
     * @return
     *     possible object is
     *     {@link TollFreePhone }
     *     
     */
    public TollFreePhone getTollFreePhone() {
        return tollFreePhone;
    }

    /**
     * Sets the value of the tollFreePhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link TollFreePhone }
     *     
     */
    public void setTollFreePhone(TollFreePhone value) {
        this.tollFreePhone = value;
    }

    /**
     * Gets the value of the fax property.
     * 
     * @return
     *     possible object is
     *     {@link Fax }
     *     
     */
    public Fax getFax() {
        return fax;
    }

    /**
     * Sets the value of the fax property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fax }
     *     
     */
    public void setFax(Fax value) {
        this.fax = value;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link Notes }
     *     
     */
    public Notes getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Notes }
     *     
     */
    public void setNotes(Notes value) {
        this.notes = value;
    }

    /**
     * Gets the value of the assignedBusinessUnitsList property.
     * 
     * @return
     *     possible object is
     *     {@link AssignedBusinessUnitsList }
     *     
     */
    public AssignedBusinessUnitsList getAssignedBusinessUnitsList() {
        return assignedBusinessUnitsList;
    }

    /**
     * Sets the value of the assignedBusinessUnitsList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssignedBusinessUnitsList }
     *     
     */
    public void setAssignedBusinessUnitsList(AssignedBusinessUnitsList value) {
        this.assignedBusinessUnitsList = value;
    }

}
