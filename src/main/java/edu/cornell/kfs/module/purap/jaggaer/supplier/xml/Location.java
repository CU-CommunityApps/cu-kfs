
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
    "description",
    "active",
    "locationActive",
    "primary",
    "prefPurchaseOrderDeliveryMethod",
    "locationEffectiveDate",
    "paymentMethod",
    "shipping",
    "handling",
    "taxInfo",
    "termsAndCondition",
    "orderDistributionList",
    "assignedBusinessUnitsList",
    "associatedAddressList",
    "associatedContactList",
    "customElementList"
})
@XmlRootElement(name = "Location")
public class Location {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "supportsOrderFulfillment")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String supportsOrderFulfillment;
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
    @XmlElement(name = "Description")
    protected Description description;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "LocationActive")
    protected LocationActive locationActive;
    @XmlElement(name = "Primary")
    protected Primary primary;
    @XmlElement(name = "PrefPurchaseOrderDeliveryMethod")
    protected PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod;
    @XmlElement(name = "LocationEffectiveDate")
    protected LocationEffectiveDate locationEffectiveDate;
    @XmlElement(name = "PaymentMethod")
    protected PaymentMethod paymentMethod;
    @XmlElement(name = "Shipping")
    protected Shipping shipping;
    @XmlElement(name = "Handling")
    protected Handling handling;
    @XmlElement(name = "TaxInfo")
    protected TaxInfo taxInfo;
    @XmlElement(name = "TermsAndCondition")
    protected TermsAndCondition termsAndCondition;
    @XmlElement(name = "OrderDistributionList")
    protected OrderDistributionList orderDistributionList;
    @XmlElement(name = "AssignedBusinessUnitsList")
    protected AssignedBusinessUnitsList assignedBusinessUnitsList;
    @XmlElement(name = "AssociatedAddressList")
    protected AssociatedAddressList associatedAddressList;
    @XmlElement(name = "AssociatedContactList")
    protected AssociatedContactList associatedContactList;
    @XmlElement(name = "CustomElementList")
    protected CustomElementList customElementList;

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
     * Gets the value of the supportsOrderFulfillment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupportsOrderFulfillment() {
        return supportsOrderFulfillment;
    }

    /**
     * Sets the value of the supportsOrderFulfillment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupportsOrderFulfillment(String value) {
        this.supportsOrderFulfillment = value;
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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
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
     * Gets the value of the locationActive property.
     * 
     * @return
     *     possible object is
     *     {@link LocationActive }
     *     
     */
    public LocationActive getLocationActive() {
        return locationActive;
    }

    /**
     * Sets the value of the locationActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationActive }
     *     
     */
    public void setLocationActive(LocationActive value) {
        this.locationActive = value;
    }

    /**
     * Gets the value of the primary property.
     * 
     * @return
     *     possible object is
     *     {@link Primary }
     *     
     */
    public Primary getPrimary() {
        return primary;
    }

    /**
     * Sets the value of the primary property.
     * 
     * @param value
     *     allowed object is
     *     {@link Primary }
     *     
     */
    public void setPrimary(Primary value) {
        this.primary = value;
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
     * Gets the value of the locationEffectiveDate property.
     * 
     * @return
     *     possible object is
     *     {@link LocationEffectiveDate }
     *     
     */
    public LocationEffectiveDate getLocationEffectiveDate() {
        return locationEffectiveDate;
    }

    /**
     * Sets the value of the locationEffectiveDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationEffectiveDate }
     *     
     */
    public void setLocationEffectiveDate(LocationEffectiveDate value) {
        this.locationEffectiveDate = value;
    }

    /**
     * Gets the value of the paymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link PaymentMethod }
     *     
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentMethod }
     *     
     */
    public void setPaymentMethod(PaymentMethod value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the shipping property.
     * 
     * @return
     *     possible object is
     *     {@link Shipping }
     *     
     */
    public Shipping getShipping() {
        return shipping;
    }

    /**
     * Sets the value of the shipping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Shipping }
     *     
     */
    public void setShipping(Shipping value) {
        this.shipping = value;
    }

    /**
     * Gets the value of the handling property.
     * 
     * @return
     *     possible object is
     *     {@link Handling }
     *     
     */
    public Handling getHandling() {
        return handling;
    }

    /**
     * Sets the value of the handling property.
     * 
     * @param value
     *     allowed object is
     *     {@link Handling }
     *     
     */
    public void setHandling(Handling value) {
        this.handling = value;
    }

    /**
     * Gets the value of the taxInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TaxInfo }
     *     
     */
    public TaxInfo getTaxInfo() {
        return taxInfo;
    }

    /**
     * Sets the value of the taxInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxInfo }
     *     
     */
    public void setTaxInfo(TaxInfo value) {
        this.taxInfo = value;
    }

    /**
     * Gets the value of the termsAndCondition property.
     * 
     * @return
     *     possible object is
     *     {@link TermsAndCondition }
     *     
     */
    public TermsAndCondition getTermsAndCondition() {
        return termsAndCondition;
    }

    /**
     * Sets the value of the termsAndCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link TermsAndCondition }
     *     
     */
    public void setTermsAndCondition(TermsAndCondition value) {
        this.termsAndCondition = value;
    }

    /**
     * Gets the value of the orderDistributionList property.
     * 
     * @return
     *     possible object is
     *     {@link OrderDistributionList }
     *     
     */
    public OrderDistributionList getOrderDistributionList() {
        return orderDistributionList;
    }

    /**
     * Sets the value of the orderDistributionList property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderDistributionList }
     *     
     */
    public void setOrderDistributionList(OrderDistributionList value) {
        this.orderDistributionList = value;
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

    /**
     * Gets the value of the associatedAddressList property.
     * 
     * @return
     *     possible object is
     *     {@link AssociatedAddressList }
     *     
     */
    public AssociatedAddressList getAssociatedAddressList() {
        return associatedAddressList;
    }

    /**
     * Sets the value of the associatedAddressList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssociatedAddressList }
     *     
     */
    public void setAssociatedAddressList(AssociatedAddressList value) {
        this.associatedAddressList = value;
    }

    /**
     * Gets the value of the associatedContactList property.
     * 
     * @return
     *     possible object is
     *     {@link AssociatedContactList }
     *     
     */
    public AssociatedContactList getAssociatedContactList() {
        return associatedContactList;
    }

    /**
     * Sets the value of the associatedContactList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssociatedContactList }
     *     
     */
    public void setAssociatedContactList(AssociatedContactList value) {
        this.associatedContactList = value;
    }

    /**
     * Gets the value of the customElementList property.
     * 
     * @return
     *     possible object is
     *     {@link CustomElementList }
     *     
     */
    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    /**
     * Sets the value of the customElementList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomElementList }
     *     
     */
    public void setCustomElementList(CustomElementList value) {
        this.customElementList = value;
    }

}
