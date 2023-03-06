
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "discountPercentOrDiscountAmountOrIsoCurrencyCode"
})
@XmlRootElement(name = "Discount")
public class Discount {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "unit", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String unit;
    @XmlElements({
        @XmlElement(name = "DiscountPercent", required = true, type = DiscountPercent.class),
        @XmlElement(name = "DiscountAmount", required = true, type = DiscountAmount.class),
        @XmlElement(name = "IsoCurrencyCode", required = true, type = IsoCurrencyCode.class)
    })
    protected List<Object> discountPercentOrDiscountAmountOrIsoCurrencyCode;

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
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Gets the value of the discountPercentOrDiscountAmountOrIsoCurrencyCode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the discountPercentOrDiscountAmountOrIsoCurrencyCode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDiscountPercentOrDiscountAmountOrIsoCurrencyCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DiscountPercent }
     * {@link DiscountAmount }
     * {@link IsoCurrencyCode }
     * 
     * 
     */
    public List<Object> getDiscountPercentOrDiscountAmountOrIsoCurrencyCode() {
        if (discountPercentOrDiscountAmountOrIsoCurrencyCode == null) {
            discountPercentOrDiscountAmountOrIsoCurrencyCode = new ArrayList<Object>();
        }
        return this.discountPercentOrDiscountAmountOrIsoCurrencyCode;
    }

}
