
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "erpNumber",
    "sqIntegrationNumber",
    "thirdPartyRefNumber"
})
@XmlRootElement(name = "ContactRef")
public class ContactRef {

    @XmlElement(name = "ERPNumber")
    protected ERPNumber erpNumber;
    @XmlElement(name = "SQIntegrationNumber")
    protected SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;

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

    /**
     * Gets the value of the thirdPartyRefNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    /**
     * Sets the value of the thirdPartyRefNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

}
