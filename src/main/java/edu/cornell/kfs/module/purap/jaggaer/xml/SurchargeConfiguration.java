//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.02.14 at 07:50:06 AM EST 
//


package edu.cornell.kfs.module.purap.jaggaer.xml;

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
    "fee",
    "useOrderThreshold",
    "orderThreshold"
})
@XmlRootElement(name = "SurchargeConfiguration")
public class SurchargeConfiguration {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Fee")
    protected Fee fee;
    @XmlElement(name = "UseOrderThreshold")
    protected UseOrderThreshold useOrderThreshold;
    @XmlElement(name = "OrderThreshold")
    protected OrderThreshold orderThreshold;

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
     * Gets the value of the fee property.
     * 
     * @return
     *     possible object is
     *     {@link Fee }
     *     
     */
    public Fee getFee() {
        return fee;
    }

    /**
     * Sets the value of the fee property.
     * 
     * @param value
     *     allowed object is
     *     {@link Fee }
     *     
     */
    public void setFee(Fee value) {
        this.fee = value;
    }

    /**
     * Gets the value of the useOrderThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link UseOrderThreshold }
     *     
     */
    public UseOrderThreshold getUseOrderThreshold() {
        return useOrderThreshold;
    }

    /**
     * Sets the value of the useOrderThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link UseOrderThreshold }
     *     
     */
    public void setUseOrderThreshold(UseOrderThreshold value) {
        this.useOrderThreshold = value;
    }

    /**
     * Gets the value of the orderThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link OrderThreshold }
     *     
     */
    public OrderThreshold getOrderThreshold() {
        return orderThreshold;
    }

    /**
     * Sets the value of the orderThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderThreshold }
     *     
     */
    public void setOrderThreshold(OrderThreshold value) {
        this.orderThreshold = value;
    }

}