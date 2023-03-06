
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
    "amount",
    "isoCurrencyCode"
})
@XmlRootElement(name = "SupplierCapital")
public class SupplierCapital {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Amount", required = true)
    protected Amount amount;
    @XmlElement(name = "IsoCurrencyCode", required = true)
    protected IsoCurrencyCode isoCurrencyCode;

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
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link Amount }
     *     
     */
    public Amount getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Amount }
     *     
     */
    public void setAmount(Amount value) {
        this.amount = value;
    }

    /**
     * Gets the value of the isoCurrencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link IsoCurrencyCode }
     *     
     */
    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    /**
     * Sets the value of the isoCurrencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link IsoCurrencyCode }
     *     
     */
    public void setIsoCurrencyCode(IsoCurrencyCode value) {
        this.isoCurrencyCode = value;
    }

}
