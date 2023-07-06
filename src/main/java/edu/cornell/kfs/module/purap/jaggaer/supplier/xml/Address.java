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
        "active", "prefPurchaseOrderDeliveryMethod", "addressLine1", "addressLine2", "addressLine3", "city", "state",
        "postalCode", "isoCountryCode", "phone", "tollFreePhone", "fax", "notes", "assignedBusinessUnitsList" })
@XmlRootElement(name = "Address")
public class Address {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "ERPNumber")
    private ErpNumber erpNumber;
    @XmlElement(name = "OldERPNumber")
    private String oldERPNumber;
    @XmlElement(name = "SQIntegrationNumber")
    private SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    private ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "Name")
    private Name name;
    @XmlElement(name = "Active")
    private Active active;
    @XmlElement(name = "PrefPurchaseOrderDeliveryMethod")
    private PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod;
    @XmlElement(name = "AddressLine1")
    private AddressLine addressLine1;
    @XmlElement(name = "AddressLine2")
    private AddressLine addressLine2;
    @XmlElement(name = "AddressLine3")
    private AddressLine addressLine3;
    @XmlElement(name = "City")
    private City city;
    @XmlElement(name = "State")
    private State state;
    @XmlElement(name = "PostalCode")
    private PostalCode postalCode;
    @XmlElement(name = "IsoCountryCode")
    private IsoCountryCode isoCountryCode;
    @XmlElement(name = "Phone")
    private Phone phone;
    @XmlElement(name = "TollFreePhone")
    private TollFreePhone tollFreePhone;
    @XmlElement(name = "Fax")
    private Fax fax;
    @XmlElement(name = "Notes")
    private JaggaerBasicValue notes;
    @XmlElement(name = "AssignedBusinessUnitsList")
    private AssignedBusinessUnitsList assignedBusinessUnitsList;

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

    public ErpNumber getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(ErpNumber erpNumber) {
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

    public PrefPurchaseOrderDeliveryMethod getPrefPurchaseOrderDeliveryMethod() {
        return prefPurchaseOrderDeliveryMethod;
    }

    public void setPrefPurchaseOrderDeliveryMethod(PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod) {
        this.prefPurchaseOrderDeliveryMethod = prefPurchaseOrderDeliveryMethod;
    }

    public AddressLine getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public AddressLine getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public AddressLine getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(AddressLine addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCode postalCode) {
        this.postalCode = postalCode;
    }

    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(IsoCountryCode isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
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

    public AssignedBusinessUnitsList getAssignedBusinessUnitsList() {
        return assignedBusinessUnitsList;
    }

    public void setAssignedBusinessUnitsList(AssignedBusinessUnitsList assignedBusinessUnitsList) {
        this.assignedBusinessUnitsList = assignedBusinessUnitsList;
    }

}
