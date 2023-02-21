
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
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
@XmlType(name = "", propOrder = { "fulfillmentCenterName", "active", "externalId", "baselineExternalId",
        "thirdPartyRefNumber", "baselineThirdPartyRefNumber", "preferredRemitToId", "preferredRemitToThirdPartyId",
        "preferredTechnicalContactId", "preferredCustomerCareContactId", "fulfillmentCenterOrderingContactInfo",
        "fulfillmentCenterContactInfo", "paymentMethod", "shipping", "handling", "taxInfo", "receiving", "invoicing",
        "orderAcceptanceInstructions", "orderDistribution", "hiddenFulfillmentCenterConfiguration", "matching",
        "decimalQuantityPrecisionSetting", "businessUnitsAssigned" })
@XmlRootElement(name = "FulfillmentCenter")
public class FulfillmentCenter {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "usePreferred")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String usePreferred;
    @XmlAttribute(name = "preferredFulfillmentCenter")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String preferredFulfillmentCenter;
    @XmlElement(name = "FulfillmentCenterName")
    protected FulfillmentCenterName fulfillmentCenterName;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "ExternalId")
    protected ExternalId externalId;
    @XmlElement(name = "BaselineExternalId")
    protected BaselineExternalId baselineExternalId;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "BaselineThirdPartyRefNumber")
    protected BaselineThirdPartyRefNumber baselineThirdPartyRefNumber;
    @XmlElement(name = "PreferredRemitToId")
    protected PreferredRemitToId preferredRemitToId;
    @XmlElement(name = "PreferredRemitToThirdPartyId")
    protected PreferredRemitToThirdPartyId preferredRemitToThirdPartyId;
    @XmlElement(name = "PreferredTechnicalContactId")
    protected PreferredTechnicalContactId preferredTechnicalContactId;
    @XmlElement(name = "PreferredCustomerCareContactId")
    protected PreferredCustomerCareContactId preferredCustomerCareContactId;
    @XmlElement(name = "FulfillmentCenterOrderingContactInfo")
    protected FulfillmentCenterOrderingContactInfo fulfillmentCenterOrderingContactInfo;
    @XmlElement(name = "FulfillmentCenterContactInfo")
    protected List<FulfillmentCenterContactInfo> fulfillmentCenterContactInfo;
    @XmlElement(name = "PaymentMethod")
    protected PaymentMethod paymentMethod;
    @XmlElement(name = "Shipping")
    protected Shipping shipping;
    @XmlElement(name = "Handling")
    protected Handling handling;
    @XmlElement(name = "TaxInfo")
    protected TaxInfo taxInfo;
    @XmlElement(name = "Receiving")
    protected Receiving receiving;
    @XmlElement(name = "Invoicing")
    protected Invoicing invoicing;
    @XmlElement(name = "OrderAcceptanceInstructions")
    protected OrderAcceptanceInstructions orderAcceptanceInstructions;
    @XmlElement(name = "OrderDistribution")
    protected OrderDistribution orderDistribution;
    @XmlElement(name = "HiddenFulfillmentCenterConfiguration")
    protected HiddenFulfillmentCenterConfiguration hiddenFulfillmentCenterConfiguration;
    @XmlElement(name = "Matching")
    protected Matching matching;
    @XmlElement(name = "DecimalQuantityPrecisionSetting")
    protected DecimalQuantityPrecisionSetting decimalQuantityPrecisionSetting;
    @XmlElement(name = "BusinessUnitsAssigned")
    protected List<BusinessUnitsAssigned> businessUnitsAssigned;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getUsePreferred() {
        return usePreferred;
    }

    public void setUsePreferred(String value) {
        this.usePreferred = value;
    }

    public String getPreferredFulfillmentCenter() {
        return preferredFulfillmentCenter;
    }

    public void setPreferredFulfillmentCenter(String value) {
        this.preferredFulfillmentCenter = value;
    }

    public FulfillmentCenterName getFulfillmentCenterName() {
        return fulfillmentCenterName;
    }

    public void setFulfillmentCenterName(FulfillmentCenterName value) {
        this.fulfillmentCenterName = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public void setExternalId(ExternalId value) {
        this.externalId = value;
    }

    public BaselineExternalId getBaselineExternalId() {
        return baselineExternalId;
    }

    public void setBaselineExternalId(BaselineExternalId value) {
        this.baselineExternalId = value;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    public BaselineThirdPartyRefNumber getBaselineThirdPartyRefNumber() {
        return baselineThirdPartyRefNumber;
    }

    public void setBaselineThirdPartyRefNumber(BaselineThirdPartyRefNumber value) {
        this.baselineThirdPartyRefNumber = value;
    }

    public PreferredRemitToId getPreferredRemitToId() {
        return preferredRemitToId;
    }

    public void setPreferredRemitToId(PreferredRemitToId value) {
        this.preferredRemitToId = value;
    }

    public PreferredRemitToThirdPartyId getPreferredRemitToThirdPartyId() {
        return preferredRemitToThirdPartyId;
    }

    public void setPreferredRemitToThirdPartyId(PreferredRemitToThirdPartyId value) {
        this.preferredRemitToThirdPartyId = value;
    }

    public PreferredTechnicalContactId getPreferredTechnicalContactId() {
        return preferredTechnicalContactId;
    }

    public void setPreferredTechnicalContactId(PreferredTechnicalContactId value) {
        this.preferredTechnicalContactId = value;
    }

    public PreferredCustomerCareContactId getPreferredCustomerCareContactId() {
        return preferredCustomerCareContactId;
    }

    public void setPreferredCustomerCareContactId(PreferredCustomerCareContactId value) {
        this.preferredCustomerCareContactId = value;
    }

    public FulfillmentCenterOrderingContactInfo getFulfillmentCenterOrderingContactInfo() {
        return fulfillmentCenterOrderingContactInfo;
    }

    public void setFulfillmentCenterOrderingContactInfo(FulfillmentCenterOrderingContactInfo value) {
        this.fulfillmentCenterOrderingContactInfo = value;
    }

    public List<FulfillmentCenterContactInfo> getFulfillmentCenterContactInfo() {
        if (fulfillmentCenterContactInfo == null) {
            fulfillmentCenterContactInfo = new ArrayList<FulfillmentCenterContactInfo>();
        }
        return this.fulfillmentCenterContactInfo;
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

    public Receiving getReceiving() {
        return receiving;
    }

    public void setReceiving(Receiving value) {
        this.receiving = value;
    }

    public Invoicing getInvoicing() {
        return invoicing;
    }

    public void setInvoicing(Invoicing value) {
        this.invoicing = value;
    }

    public OrderAcceptanceInstructions getOrderAcceptanceInstructions() {
        return orderAcceptanceInstructions;
    }

    public void setOrderAcceptanceInstructions(OrderAcceptanceInstructions value) {
        this.orderAcceptanceInstructions = value;
    }

    public OrderDistribution getOrderDistribution() {
        return orderDistribution;
    }

    public void setOrderDistribution(OrderDistribution value) {
        this.orderDistribution = value;
    }

    public HiddenFulfillmentCenterConfiguration getHiddenFulfillmentCenterConfiguration() {
        return hiddenFulfillmentCenterConfiguration;
    }

    public void setHiddenFulfillmentCenterConfiguration(HiddenFulfillmentCenterConfiguration value) {
        this.hiddenFulfillmentCenterConfiguration = value;
    }

    public Matching getMatching() {
        return matching;
    }

    public void setMatching(Matching value) {
        this.matching = value;
    }

    public DecimalQuantityPrecisionSetting getDecimalQuantityPrecisionSetting() {
        return decimalQuantityPrecisionSetting;
    }

    public void setDecimalQuantityPrecisionSetting(DecimalQuantityPrecisionSetting value) {
        this.decimalQuantityPrecisionSetting = value;
    }

    public List<BusinessUnitsAssigned> getBusinessUnitsAssigned() {
        if (businessUnitsAssigned == null) {
            businessUnitsAssigned = new ArrayList<BusinessUnitsAssigned>();
        }
        return this.businessUnitsAssigned;
    }

}
