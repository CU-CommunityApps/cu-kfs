
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
    "attachmentName",
    "attachmentURL",
    "attachmentSize",
    "xopInclude"
})
@XmlRootElement(name = "Attachment")
public class Attachment {

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlElement(name = "AttachmentName", required = true)
    protected String attachmentName;
    @XmlElement(name = "AttachmentURL")
    protected String attachmentURL;
    @XmlElement(name = "AttachmentSize")
    protected String attachmentSize;
    @XmlElement(name = "xop:Include")
    protected XopInclude xopInclude;

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
     * Gets the value of the attachmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentName() {
        return attachmentName;
    }

    /**
     * Sets the value of the attachmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentName(String value) {
        this.attachmentName = value;
    }

    /**
     * Gets the value of the attachmentURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentURL() {
        return attachmentURL;
    }

    /**
     * Sets the value of the attachmentURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentURL(String value) {
        this.attachmentURL = value;
    }

    /**
     * Gets the value of the attachmentSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachmentSize() {
        return attachmentSize;
    }

    /**
     * Sets the value of the attachmentSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachmentSize(String value) {
        this.attachmentSize = value;
    }

    /**
     * Gets the value of the xopInclude property.
     * 
     * @return
     *     possible object is
     *     {@link XopInclude }
     *     
     */
    public XopInclude getXopInclude() {
        return xopInclude;
    }

    /**
     * Sets the value of the xopInclude property.
     * 
     * @param value
     *     allowed object is
     *     {@link XopInclude }
     *     
     */
    public void setXopInclude(XopInclude value) {
        this.xopInclude = value;
    }

}
