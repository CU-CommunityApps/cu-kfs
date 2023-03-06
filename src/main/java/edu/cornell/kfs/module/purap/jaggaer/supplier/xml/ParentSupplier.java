
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
    "erpNumber",
    "sqIntegrationNumber"
})
@XmlRootElement(name = "ParentSupplier")
public class ParentSupplier {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "ERPNumber")
    protected ERPNumber erpNumber;
    @XmlElement(name = "SQIntegrationNumber")
    protected SQIntegrationNumber sqIntegrationNumber;

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
     * Gets the value of the erpNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ERPNumber }
     *     
     */
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    /**
     * Sets the value of the erpNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERPNumber }
     *     
     */
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    /**
     * Gets the value of the sqIntegrationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    /**
     * Sets the value of the sqIntegrationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

}
