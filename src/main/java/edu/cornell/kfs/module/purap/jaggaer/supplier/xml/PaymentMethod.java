
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
@XmlType(name = "", propOrder = {
    "poPaymentMethod",
    "blanketPOPaymentMethod"
})
@XmlRootElement(name = "PaymentMethod")
public class PaymentMethod {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "POPaymentMethod")
    protected POPaymentMethod poPaymentMethod;
    @XmlElement(name = "BlanketPOPaymentMethod")
    protected BlanketPOPaymentMethod blanketPOPaymentMethod;

    
    public String getType() {
        return type;
    }

    
    public void setType(String value) {
        this.type = value;
    }

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public POPaymentMethod getPOPaymentMethod() {
        return poPaymentMethod;
    }

    
    public void setPOPaymentMethod(POPaymentMethod value) {
        this.poPaymentMethod = value;
    }

    
    public BlanketPOPaymentMethod getBlanketPOPaymentMethod() {
        return blanketPOPaymentMethod;
    }

    
    public void setBlanketPOPaymentMethod(BlanketPOPaymentMethod value) {
        this.blanketPOPaymentMethod = value;
    }

}
