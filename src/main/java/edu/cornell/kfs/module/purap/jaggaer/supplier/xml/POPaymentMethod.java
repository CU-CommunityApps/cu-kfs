
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
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
     * Gets the value of the poPayment property.
     * 
     * @return
     *     possible object is
     *     {@link POPayment }
     *     
     */
    public POPayment getPOPayment() {
        return poPayment;
    }

    /**
     * Sets the value of the poPayment property.
     * 
     * @param value
     *     allowed object is
     *     {@link POPayment }
     *     
     */
    public void setPOPayment(POPayment value) {
        this.poPayment = value;
    }

    /**
     * Gets the value of the pCardPayment property.
     * 
     * @return
     *     possible object is
     *     {@link PCardPayment }
     *     
     */
    public PCardPayment getPCardPayment() {
        return pCardPayment;
    }

    /**
     * Sets the value of the pCardPayment property.
     * 
     * @param value
     *     allowed object is
     *     {@link PCardPayment }
     *     
     */
    public void setPCardPayment(PCardPayment value) {
        this.pCardPayment = value;
    }

    /**
     * Gets the value of the jpMorganVCardPayment property.
     * 
     * @return
     *     possible object is
     *     {@link JPMorganVCardPayment }
     *     
     */
    public JPMorganVCardPayment getJPMorganVCardPayment() {
        return jpMorganVCardPayment;
    }

    /**
     * Sets the value of the jpMorganVCardPayment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JPMorganVCardPayment }
     *     
     */
    public void setJPMorganVCardPayment(JPMorganVCardPayment value) {
        this.jpMorganVCardPayment = value;
    }

}
