package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "active", "discount", "days", "net", "customPaymentTerm", "fob",
        "standardPaymentTermsCode", "termsType", "daysAfter" })
@XmlRootElement(name = "PaymentTerms")
public class PaymentTerms {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "Discount")
    protected Discount discount;
    @XmlElement(name = "Days")
    protected JaggaerBasicValue days;
    @XmlElement(name = "Net")
    protected JaggaerBasicValue net;
    @XmlElement(name = "CustomPaymentTerm")
    protected CustomPaymentTerm customPaymentTerm;
    @XmlElement(name = "FOB")
    protected JaggaerBasicValue fob;
    @XmlElement(name = "StandardPaymentTermsCode")
    protected JaggaerBasicValue standardPaymentTermsCode;
    @XmlElement(name = "TermsType")
    protected JaggaerBasicValue termsType;
    @XmlElement(name = "DaysAfter")
    protected JaggaerBasicValue daysAfter;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public JaggaerBasicValue getDays() {
        return days;
    }

    public void setDays(JaggaerBasicValue days) {
        this.days = days;
    }

    public JaggaerBasicValue getNet() {
        return net;
    }

    public void setNet(JaggaerBasicValue net) {
        this.net = net;
    }

    public CustomPaymentTerm getCustomPaymentTerm() {
        return customPaymentTerm;
    }

    public void setCustomPaymentTerm(CustomPaymentTerm customPaymentTerm) {
        this.customPaymentTerm = customPaymentTerm;
    }

    public JaggaerBasicValue getFob() {
        return fob;
    }

    public void setFob(JaggaerBasicValue fob) {
        this.fob = fob;
    }

    public JaggaerBasicValue getStandardPaymentTermsCode() {
        return standardPaymentTermsCode;
    }

    public void setStandardPaymentTermsCode(JaggaerBasicValue standardPaymentTermsCode) {
        this.standardPaymentTermsCode = standardPaymentTermsCode;
    }

    public JaggaerBasicValue getTermsType() {
        return termsType;
    }

    public void setTermsType(JaggaerBasicValue termsType) {
        this.termsType = termsType;
    }

    public JaggaerBasicValue getDaysAfter() {
        return daysAfter;
    }

    public void setDaysAfter(JaggaerBasicValue daysAfter) {
        this.daysAfter = daysAfter;
    }

}
