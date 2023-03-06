
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
    "countryCode",
    "areaCode",
    "number",
    "extension"
})
@XmlRootElement(name = "TelephoneNumber")
public class TelephoneNumber {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "CountryCode", required = true)
    protected CountryCode countryCode;
    @XmlElement(name = "AreaCode", required = true)
    protected AreaCode areaCode;
    @XmlElement(name = "Number", required = true)
    protected Number number;
    @XmlElement(name = "Extension")
    protected Extension extension;

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
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link CountryCode }
     *     
     */
    public CountryCode getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CountryCode }
     *     
     */
    public void setCountryCode(CountryCode value) {
        this.countryCode = value;
    }

    /**
     * Gets the value of the areaCode property.
     * 
     * @return
     *     possible object is
     *     {@link AreaCode }
     *     
     */
    public AreaCode getAreaCode() {
        return areaCode;
    }

    /**
     * Sets the value of the areaCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link AreaCode }
     *     
     */
    public void setAreaCode(AreaCode value) {
        this.areaCode = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link Number }
     *     
     */
    public Number getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link Number }
     *     
     */
    public void setNumber(Number value) {
        this.number = value;
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link Extension }
     *     
     */
    public Extension getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link Extension }
     *     
     */
    public void setExtension(Extension value) {
        this.extension = value;
    }

}
