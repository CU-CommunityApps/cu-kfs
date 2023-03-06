
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
