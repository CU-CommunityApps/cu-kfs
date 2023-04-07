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
@XmlType(name = "", propOrder = { "poPaymentMethod", "blanketPOPaymentMethod" })
@XmlRootElement(name = "PaymentMethod")
public class PaymentMethod {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "POPaymentMethod")
    private POPaymentMethod poPaymentMethod;
    @XmlElement(name = "BlanketPOPaymentMethod")
    private BlanketPOPaymentMethod blanketPOPaymentMethod;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public POPaymentMethod getPoPaymentMethod() {
        return poPaymentMethod;
    }

    public void setPoPaymentMethod(POPaymentMethod poPaymentMethod) {
        this.poPaymentMethod = poPaymentMethod;
    }

    public BlanketPOPaymentMethod getBlanketPOPaymentMethod() {
        return blanketPOPaymentMethod;
    }

    public void setBlanketPOPaymentMethod(BlanketPOPaymentMethod blanketPOPaymentMethod) {
        this.blanketPOPaymentMethod = blanketPOPaymentMethod;
    }

}
