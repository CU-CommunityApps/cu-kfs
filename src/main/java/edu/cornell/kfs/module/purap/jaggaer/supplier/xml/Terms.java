
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "active", "discount", "days", "net", "standardPaymentTermsCode", "termsType",
        "daysAfter" })
@XmlRootElement(name = "Terms")
public class Terms {

    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "Discount", required = true)
    protected Discount discount;
    @XmlElement(name = "Days")
    protected Days days;
    @XmlElement(name = "Net")
    protected Net net;
    @XmlElement(name = "StandardPaymentTermsCode")
    protected StandardPaymentTermsCode standardPaymentTermsCode;
    @XmlElement(name = "TermsType")
    protected TermsType termsType;
    @XmlElement(name = "DaysAfter")
    protected DaysAfter daysAfter;

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

    public Days getDays() {
        return days;
    }

    public void setDays(Days value) {
        this.days = value;
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net value) {
        this.net = value;
    }

    public StandardPaymentTermsCode getStandardPaymentTermsCode() {
        return standardPaymentTermsCode;
    }

    public void setStandardPaymentTermsCode(StandardPaymentTermsCode value) {
        this.standardPaymentTermsCode = value;
    }

    public TermsType getTermsType() {
        return termsType;
    }

    public void setTermsType(TermsType value) {
        this.termsType = value;
    }

    public DaysAfter getDaysAfter() {
        return daysAfter;
    }

    public void setDaysAfter(DaysAfter value) {
        this.daysAfter = value;
    }

}
