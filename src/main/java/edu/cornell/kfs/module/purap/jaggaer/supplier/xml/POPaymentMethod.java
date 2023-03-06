
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
    "poPayment",
    "pCardPayment",
    "jpMorganVCardPayment"
})
@XmlRootElement(name = "POPaymentMethod")
public class POPaymentMethod {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "POPayment")
    protected POPayment poPayment;
    @XmlElement(name = "PCardPayment")
    protected PCardPayment pCardPayment;
    @XmlElement(name = "JPMorganVCardPayment")
    protected JPMorganVCardPayment jpMorganVCardPayment;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public POPayment getPOPayment() {
        return poPayment;
    }

    
    public void setPOPayment(POPayment value) {
        this.poPayment = value;
    }

    
    public PCardPayment getPCardPayment() {
        return pCardPayment;
    }

    
    public void setPCardPayment(PCardPayment value) {
        this.pCardPayment = value;
    }

    
    public JPMorganVCardPayment getJPMorganVCardPayment() {
        return jpMorganVCardPayment;
    }

    
    public void setJPMorganVCardPayment(JPMorganVCardPayment value) {
        this.jpMorganVCardPayment = value;
    }

}
