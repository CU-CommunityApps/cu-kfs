
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
    "blanketPONumber"
})
@XmlRootElement(name = "BlanketPOPaymentMethod")
public class BlanketPOPaymentMethod {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "BlanketPONumber", required = true)
    protected BlanketPONumber blanketPONumber;

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
     * Gets the value of the blanketPONumber property.
     * 
     * @return
     *     possible object is
     *     {@link BlanketPONumber }
     *     
     */
    public BlanketPONumber getBlanketPONumber() {
        return blanketPONumber;
    }

    /**
     * Sets the value of the blanketPONumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlanketPONumber }
     *     
     */
    public void setBlanketPONumber(BlanketPONumber value) {
        this.blanketPONumber = value;
    }

}
