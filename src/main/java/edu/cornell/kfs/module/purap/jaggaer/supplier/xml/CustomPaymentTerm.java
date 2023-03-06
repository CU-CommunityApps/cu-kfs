
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "CustomPaymentTerm")
public class CustomPaymentTerm {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "useCustomPaymentTerm")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useCustomPaymentTerm;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlValue
    protected String value;

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
     * Gets the value of the useCustomPaymentTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUseCustomPaymentTerm() {
        return useCustomPaymentTerm;
    }

    /**
     * Sets the value of the useCustomPaymentTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUseCustomPaymentTerm(String value) {
        this.useCustomPaymentTerm = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getvalue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setvalue(String value) {
        this.value = value;
    }

}
