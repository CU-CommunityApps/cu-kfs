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
    "surchargeConfiguration"
})
@XmlRootElement(name = "Shipping")
public class Shipping {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "SurchargeConfiguration")
    protected SurchargeConfiguration surchargeConfiguration;

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
     * Gets the value of the surchargeConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link SurchargeConfiguration }
     *     
     */
    public SurchargeConfiguration getSurchargeConfiguration() {
        return surchargeConfiguration;
    }

    /**
     * Sets the value of the surchargeConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link SurchargeConfiguration }
     *     
     */
    public void setSurchargeConfiguration(SurchargeConfiguration value) {
        this.surchargeConfiguration = value;
    }

}
