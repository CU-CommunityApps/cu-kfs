
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
    "identity",
    "sharedSecret"
})
@XmlRootElement(name = "Authentication")
public class Authentication {

    @XmlElement(name = "Identity", required = true)
    protected String identity;
    @XmlElement(name = "SharedSecret", required = true)
    protected String sharedSecret;

    /**
     * Gets the value of the identity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Sets the value of the identity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentity(String value) {
        this.identity = value;
    }

    /**
     * Gets the value of the sharedSecret property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSharedSecret() {
        return sharedSecret;
    }

    /**
     * Sets the value of the sharedSecret property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSharedSecret(String value) {
        this.sharedSecret = value;
    }

}
