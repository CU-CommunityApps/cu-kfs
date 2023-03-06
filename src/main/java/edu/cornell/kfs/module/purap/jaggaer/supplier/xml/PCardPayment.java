
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
    "active",
    "poNumberSelection",
    "requireCardSecurityCode"
})
@XmlRootElement(name = "PCardPayment")
public class PCardPayment {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "PONumberSelection")
    protected PONumberSelection poNumberSelection;
    @XmlElement(name = "RequireCardSecurityCode")
    protected RequireCardSecurityCode requireCardSecurityCode;

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
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Active }
     *     
     */
    public Active getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Active }
     *     
     */
    public void setActive(Active value) {
        this.active = value;
    }

    /**
     * Gets the value of the poNumberSelection property.
     * 
     * @return
     *     possible object is
     *     {@link PONumberSelection }
     *     
     */
    public PONumberSelection getPONumberSelection() {
        return poNumberSelection;
    }

    /**
     * Sets the value of the poNumberSelection property.
     * 
     * @param value
     *     allowed object is
     *     {@link PONumberSelection }
     *     
     */
    public void setPONumberSelection(PONumberSelection value) {
        this.poNumberSelection = value;
    }

    /**
     * Gets the value of the requireCardSecurityCode property.
     * 
     * @return
     *     possible object is
     *     {@link RequireCardSecurityCode }
     *     
     */
    public RequireCardSecurityCode getRequireCardSecurityCode() {
        return requireCardSecurityCode;
    }

    /**
     * Sets the value of the requireCardSecurityCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireCardSecurityCode }
     *     
     */
    public void setRequireCardSecurityCode(RequireCardSecurityCode value) {
        this.requireCardSecurityCode = value;
    }

}
