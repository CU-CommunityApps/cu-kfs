
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
    "annualSalesYear",
    "annualSalesAmount",
    "isoCurrencyCode"
})
@XmlRootElement(name = "AnnualSales")
public class AnnualSales {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "AnnualSalesYear")
    protected AnnualSalesYear annualSalesYear;
    @XmlElement(name = "AnnualSalesAmount")
    protected AnnualSalesAmount annualSalesAmount;
    @XmlElement(name = "IsoCurrencyCode")
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
     * Gets the value of the annualSalesYear property.
     * 
     * @return
     *     possible object is
     *     {@link AnnualSalesYear }
     *     
     */
    public AnnualSalesYear getAnnualSalesYear() {
        return annualSalesYear;
    }

    /**
     * Sets the value of the annualSalesYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnnualSalesYear }
     *     
     */
    public void setAnnualSalesYear(AnnualSalesYear value) {
        this.annualSalesYear = value;
    }

    /**
     * Gets the value of the annualSalesAmount property.
     * 
     * @return
     *     possible object is
     *     {@link AnnualSalesAmount }
     *     
     */
    public AnnualSalesAmount getAnnualSalesAmount() {
        return annualSalesAmount;
    }

    /**
     * Sets the value of the annualSalesAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnnualSalesAmount }
     *     
     */
    public void setAnnualSalesAmount(AnnualSalesAmount value) {
        this.annualSalesAmount = value;
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
