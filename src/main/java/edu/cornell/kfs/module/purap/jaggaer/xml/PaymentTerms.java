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

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount value) {
        this.discount = value;
    }

    public JaggaerBasicValue getDays() {
        return days;
    }

    public void setDays(JaggaerBasicValue value) {
        this.days = value;
    }

    public JaggaerBasicValue getNet() {
        return net;
    }

    public void setNet(JaggaerBasicValue value) {
        this.net = value;
    }

    public CustomPaymentTerm getCustomPaymentTerm() {
        return customPaymentTerm;
    }

    public void setCustomPaymentTerm(CustomPaymentTerm value) {
        this.customPaymentTerm = value;
    }

    public JaggaerBasicValue getFOB() {
        return fob;
    }

    public void setFOB(JaggaerBasicValue value) {
        this.fob = value;
    }

    public JaggaerBasicValue getStandardPaymentTermsCode() {
        return standardPaymentTermsCode;
    }

    public void setStandardPaymentTermsCode(JaggaerBasicValue value) {
        this.standardPaymentTermsCode = value;
    }

    public JaggaerBasicValue getTermsType() {
        return termsType;
    }

    public void setTermsType(JaggaerBasicValue value) {
        this.termsType = value;
    }

    public JaggaerBasicValue getDaysAfter() {
        return daysAfter;
    }

    public void setDaysAfter(JaggaerBasicValue value) {
        this.daysAfter = value;
    }

}
