
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "paymentMethod", "shipping", "handling", "taxInfo", "receiving", "invoicing",
        "orderAcceptanceInstructions", "orderDistribution", "hiddenFulfillmentCenterConfiguration", "matching",
        "decimalQuantityPrecisionSetting" })
@XmlRootElement(name = "DefaultFCConfig")
public class DefaultFCConfig {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
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

}
