
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addressRef"
})
@XmlRootElement(name = "AssociatedAddress")
public class AssociatedAddress {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlElement(name = "AddressRef")
    protected AddressRef addressRef;

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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the addressRef property.
     * 
     * @return
     *     possible object is
     *     {@link AddressRef }
     *     
     */
    public AddressRef getAddressRef() {
        return addressRef;
    }

    /**
     * Sets the value of the addressRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressRef }
     *     
     */
    public void setAddressRef(AddressRef value) {
        this.addressRef = value;
    }

}
