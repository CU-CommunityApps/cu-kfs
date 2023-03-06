
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
