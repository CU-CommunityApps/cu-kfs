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
@XmlType(name = "", propOrder = { "erpNumber", "oldERPNumber", "sqIntegrationNumber", "thirdPartyRefNumber", "name",
        "description", "active", "locationActive", "primary", "prefPurchaseOrderDeliveryMethod",
        "locationEffectiveDate", "paymentMethod", "shipping", "handling", "taxInfo", "termsAndCondition",
        "orderDistributionList", "assignedBusinessUnitsList", "associatedAddressList", "associatedContactList",
        "customElementList" })
@XmlRootElement(name = "Location")
public class Location {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlAttribute(name = "supportsOrderFulfillment")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String supportsOrderFulfillment;
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
    @XmlElement(name = "Description")
    private JaggaerBasicValue description;
    @XmlElement(name = "Active")
    private Active active;
    @XmlElement(name = "LocationActive")
    private JaggaerBasicValue locationActive;
    @XmlElement(name = "Primary")
    private JaggaerBasicValue primary;
    @XmlElement(name = "PrefPurchaseOrderDeliveryMethod")
    private PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod;
    @XmlElement(name = "LocationEffectiveDate")
    private JaggaerBasicValue locationEffectiveDate;
    @XmlElement(name = "PaymentMethod")
    private PaymentMethod paymentMethod;
    @XmlElement(name = "Shipping")
    private Shipping shipping;
    @XmlElement(name = "Handling")
    private Handling handling;
    @XmlElement(name = "TaxInfo")
    private TaxInfo taxInfo;
    @XmlElement(name = "TermsAndCondition")
    private TermsAndCondition termsAndCondition;
    @XmlElement(name = "OrderDistributionList")
    private OrderDistributionList orderDistributionList;
    @XmlElement(name = "AssignedBusinessUnitsList")
    private AssignedBusinessUnitsList assignedBusinessUnitsList;
    @XmlElement(name = "AssociatedAddressList")
    private AssociatedAddressList associatedAddressList;
    @XmlElement(name = "AssociatedContactList")
    private AssociatedContactList associatedContactList;
    @XmlElement(name = "CustomElementList")
    private CustomElementList customElementList;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getSupportsOrderFulfillment() {
        return supportsOrderFulfillment;
    }

    public void setSupportsOrderFulfillment(String supportsOrderFulfillment) {
        this.supportsOrderFulfillment = supportsOrderFulfillment;
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

    public JaggaerBasicValue getDescription() {
        return description;
    }

    public void setDescription(JaggaerBasicValue description) {
        this.description = description;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public JaggaerBasicValue getLocationActive() {
        return locationActive;
    }

    public void setLocationActive(JaggaerBasicValue locationActive) {
        this.locationActive = locationActive;
    }

    public JaggaerBasicValue getPrimary() {
        return primary;
    }

    public void setPrimary(JaggaerBasicValue primary) {
        this.primary = primary;
    }

    public PrefPurchaseOrderDeliveryMethod getPrefPurchaseOrderDeliveryMethod() {
        return prefPurchaseOrderDeliveryMethod;
    }

    public void setPrefPurchaseOrderDeliveryMethod(PrefPurchaseOrderDeliveryMethod prefPurchaseOrderDeliveryMethod) {
        this.prefPurchaseOrderDeliveryMethod = prefPurchaseOrderDeliveryMethod;
    }

    public JaggaerBasicValue getLocationEffectiveDate() {
        return locationEffectiveDate;
    }

    public void setLocationEffectiveDate(JaggaerBasicValue locationEffectiveDate) {
        this.locationEffectiveDate = locationEffectiveDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public Handling getHandling() {
        return handling;
    }

    public void setHandling(Handling handling) {
        this.handling = handling;
    }

    public TaxInfo getTaxInfo() {
        return taxInfo;
    }

    public void setTaxInfo(TaxInfo taxInfo) {
        this.taxInfo = taxInfo;
    }

    public TermsAndCondition getTermsAndCondition() {
        return termsAndCondition;
    }

    public void setTermsAndCondition(TermsAndCondition termsAndCondition) {
        this.termsAndCondition = termsAndCondition;
    }

    public OrderDistributionList getOrderDistributionList() {
        return orderDistributionList;
    }

    public void setOrderDistributionList(OrderDistributionList orderDistributionList) {
        this.orderDistributionList = orderDistributionList;
    }

    public AssignedBusinessUnitsList getAssignedBusinessUnitsList() {
        return assignedBusinessUnitsList;
    }

    public void setAssignedBusinessUnitsList(AssignedBusinessUnitsList assignedBusinessUnitsList) {
        this.assignedBusinessUnitsList = assignedBusinessUnitsList;
    }

    public AssociatedAddressList getAssociatedAddressList() {
        return associatedAddressList;
    }

    public void setAssociatedAddressList(AssociatedAddressList associatedAddressList) {
        this.associatedAddressList = associatedAddressList;
    }

    public AssociatedContactList getAssociatedContactList() {
        return associatedContactList;
    }

    public void setAssociatedContactList(AssociatedContactList associatedContactList) {
        this.associatedContactList = associatedContactList;
    }

    public CustomElementList getCustomElementList() {
        return customElementList;
    }

    public void setCustomElementList(CustomElementList customElementList) {
        this.customElementList = customElementList;
    }

}
