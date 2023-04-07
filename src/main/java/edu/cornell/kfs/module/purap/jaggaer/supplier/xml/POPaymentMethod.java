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
@XmlType(name = "", propOrder = { "poPayment", "pCardPayment", "jpMorganVCardPayment" })
@XmlRootElement(name = "POPaymentMethod")
public class POPaymentMethod {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "POPayment")
    private POPayment poPayment;
    @XmlElement(name = "PCardPayment")
    private PCardPayment pCardPayment;
    @XmlElement(name = "JPMorganVCardPayment")
    private JPMorganVCardPayment jpMorganVCardPayment;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public POPayment getPoPayment() {
        return poPayment;
    }

    public void setPoPayment(POPayment poPayment) {
        this.poPayment = poPayment;
    }

    public PCardPayment getpCardPayment() {
        return pCardPayment;
    }

    public void setpCardPayment(PCardPayment pCardPayment) {
        this.pCardPayment = pCardPayment;
    }

    public JPMorganVCardPayment getJpMorganVCardPayment() {
        return jpMorganVCardPayment;
    }

    public void setJpMorganVCardPayment(JPMorganVCardPayment jpMorganVCardPayment) {
        this.jpMorganVCardPayment = jpMorganVCardPayment;
    }

}
