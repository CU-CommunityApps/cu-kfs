
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



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

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public String getSupportsOrderFulfillment() {
        return supportsOrderFulfillment;
    }

    
    public void setSupportsOrderFulfillment(String value) {
        this.supportsOrderFulfillment = value;
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

    
    public Description getDescription() {
        return description;
    }

    
    public void setDescription(Description value) {
        this.description = value;
    }

    
    public Active getActive() {
        return active;
    }

    
    public void setActive(Active value) {
        this.active = value;
    }

    
    public LocationActive getLocationActive() {
        return locationActive;
    }

    
    public void setLocationActive(LocationActive value) {
        this.locationActive = value;
    }

    
    public Primary getPrimary() {
        return primary;
    }

    
    public void setPrimary(Primary value) {
        this.primary = value;
    }

    
    public PrefPurchaseOrderDeliveryMethod getPrefPurchaseOrderDeliveryMethod() {
        return prefPurchaseOrderDeliveryMethod;
    }

    
    public void setPrefPurchaseOrderDeliveryMethod(PrefPurchaseOrderDeliveryMethod value) {
        this.prefPurchaseOrderDeliveryMethod = value;
    }

    
    public LocationEffectiveDate getLocationEffectiveDate() {
        return locationEffectiveDate;
    }

    
    public void setLocationEffectiveDate(LocationEffectiveDate value) {
        this.locationEffectiveDate = value;
    }

    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    
    public void setPaymentMethod(PaymentMethod value) {
        this.paymentMethod = value;
    }

    
    public Shipping getShipping() {
        return shipping;
    }

    
    public void setShipping(Shipping value) {
        this.shipping = value;
    }

    
    public Handling getHandling() {
        return handling;
    }

    
    public void setHandling(Handling value) {
        this.handling = value;
    }

    
    public TaxInfo getTaxInfo() {
        return taxInfo;
    }

    
    public void setTaxInfo(TaxInfo value) {
        this.taxInfo = value;
    }

    
    public TermsAndCondition getTermsAndCondition() {
        return termsAndCondition;
    }

    
    public void setTermsAndCondition(TermsAndCondition value) {
        this.termsAndCondition = value;
    }

    
    public OrderDistributionList getOrderDistributionList() {
        return orderDistributionList;
    }

    
    public void setOrderDistributionList(OrderDistributionList value) {
        this.orderDistributionList = value;
    }

    
    public AssignedBusinessUnitsList getAssignedBusinessUnitsList() {
        return assignedBusinessUnitsList;
    }

    
    public void setAssignedBusinessUnitsList(AssignedBusinessUnitsList value) {
        this.assignedBusinessUnitsList = value;
    }

    
    public AssociatedAddressList getAssociatedAddressList() {
        return associatedAddressList;
    }

    
    public void setAssociatedAddressList(AssociatedAddressList value) {
        this.associatedAddressList = value;
    }

    
    public AssociatedContactList getAssociatedContactList() {
        return associatedContactList;
    }

    
    public void setAssociatedContactList(AssociatedContactList value) {
        this.associatedContactList = value;
    }

    
    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    
    public void setCustomElementList(CustomElementList value) {
        this.customElementList = value;
    }

}
