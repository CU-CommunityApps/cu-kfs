
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
    "messageId",
    "relatedMessageId",
    "timestamp",
    "authentication"
})
@XmlRootElement(name = "Header")
public class Header {

    @XmlElement(name = "MessageId", required = true)
    protected String messageId;
    @XmlElement(name = "RelatedMessageId")
    protected String relatedMessageId;
    @XmlElement(name = "Timestamp", required = true)
    protected String timestamp;
    @XmlElement(name = "Authentication")
    protected Authentication authentication;

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the relatedMessageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelatedMessageId() {
        return relatedMessageId;
    }

    /**
     * Sets the value of the relatedMessageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelatedMessageId(String value) {
        this.relatedMessageId = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the authentication property.
     * 
     * @return
     *     possible object is
     *     {@link Authentication }
     *     
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Sets the value of the authentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link Authentication }
     *     
     */
    public void setAuthentication(Authentication value) {
        this.authentication = value;
    }

}
