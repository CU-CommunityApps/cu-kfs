
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
    "taxableByDefault",
    "tax1Active",
    "tax1",
    "tax2Active",
    "tax2",
    "taxShipping",
    "taxHandling"
})
@XmlRootElement(name = "TaxInfo")
public class TaxInfo {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "TaxableByDefault")
    protected TaxableByDefault taxableByDefault;
    @XmlElement(name = "Tax1Active")
    protected Tax1Active tax1Active;
    @XmlElement(name = "Tax1")
    protected Tax1 tax1;
    @XmlElement(name = "Tax2Active")
    protected Tax2Active tax2Active;
    @XmlElement(name = "Tax2")
    protected Tax2 tax2;
    @XmlElement(name = "TaxShipping")
    protected TaxShipping taxShipping;
    @XmlElement(name = "TaxHandling")
    protected TaxHandling taxHandling;

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
     * Gets the value of the taxableByDefault property.
     * 
     * @return
     *     possible object is
     *     {@link TaxableByDefault }
     *     
     */
    public TaxableByDefault getTaxableByDefault() {
        return taxableByDefault;
    }

    /**
     * Sets the value of the taxableByDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxableByDefault }
     *     
     */
    public void setTaxableByDefault(TaxableByDefault value) {
        this.taxableByDefault = value;
    }

    /**
     * Gets the value of the tax1Active property.
     * 
     * @return
     *     possible object is
     *     {@link Tax1Active }
     *     
     */
    public Tax1Active getTax1Active() {
        return tax1Active;
    }

    /**
     * Sets the value of the tax1Active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tax1Active }
     *     
     */
    public void setTax1Active(Tax1Active value) {
        this.tax1Active = value;
    }

    /**
     * Gets the value of the tax1 property.
     * 
     * @return
     *     possible object is
     *     {@link Tax1 }
     *     
     */
    public Tax1 getTax1() {
        return tax1;
    }

    /**
     * Sets the value of the tax1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tax1 }
     *     
     */
    public void setTax1(Tax1 value) {
        this.tax1 = value;
    }

    /**
     * Gets the value of the tax2Active property.
     * 
     * @return
     *     possible object is
     *     {@link Tax2Active }
     *     
     */
    public Tax2Active getTax2Active() {
        return tax2Active;
    }

    /**
     * Sets the value of the tax2Active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tax2Active }
     *     
     */
    public void setTax2Active(Tax2Active value) {
        this.tax2Active = value;
    }

    /**
     * Gets the value of the tax2 property.
     * 
     * @return
     *     possible object is
     *     {@link Tax2 }
     *     
     */
    public Tax2 getTax2() {
        return tax2;
    }

    /**
     * Sets the value of the tax2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tax2 }
     *     
     */
    public void setTax2(Tax2 value) {
        this.tax2 = value;
    }

    /**
     * Gets the value of the taxShipping property.
     * 
     * @return
     *     possible object is
     *     {@link TaxShipping }
     *     
     */
    public TaxShipping getTaxShipping() {
        return taxShipping;
    }

    /**
     * Sets the value of the taxShipping property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxShipping }
     *     
     */
    public void setTaxShipping(TaxShipping value) {
        this.taxShipping = value;
    }

    /**
     * Gets the value of the taxHandling property.
     * 
     * @return
     *     possible object is
     *     {@link TaxHandling }
     *     
     */
    public TaxHandling getTaxHandling() {
        return taxHandling;
    }

    /**
     * Sets the value of the taxHandling property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxHandling }
     *     
     */
    public void setTaxHandling(TaxHandling value) {
        this.taxHandling = value;
    }

}
