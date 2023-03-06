
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


/**
 * 
 */
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

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the isChanged property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsChanged() {
        return isChanged;
    }

    /**
     * Sets the value of the isChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    /**
     * Gets the value of the poPaymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link POPaymentMethod }
     *     
     */
    public POPaymentMethod getPOPaymentMethod() {
        return poPaymentMethod;
    }

    /**
     * Sets the value of the poPaymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link POPaymentMethod }
     *     
     */
    public void setPOPaymentMethod(POPaymentMethod value) {
        this.poPaymentMethod = value;
    }

    /**
     * Gets the value of the blanketPOPaymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link BlanketPOPaymentMethod }
     *     
     */
    public BlanketPOPaymentMethod getBlanketPOPaymentMethod() {
        return blanketPOPaymentMethod;
    }

    /**
     * Sets the value of the blanketPOPaymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlanketPOPaymentMethod }
     *     
     */
    public void setBlanketPOPaymentMethod(BlanketPOPaymentMethod value) {
        this.blanketPOPaymentMethod = value;
    }

}
