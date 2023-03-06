
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attachment"
})
@XmlRootElement(name = "Attachments")
public class Attachments {

    @XmlAttribute(name = "xmlns:xop")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlnsXop;
    @XmlElement(name = "Attachment")
    protected List<Attachment> attachment;

    /**
     * Gets the value of the xmlnsXop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlnsXop() {
        if (xmlnsXop == null) {
            return "http://www.w3.org/2004/08/xop/include/";
        } else {
            return xmlnsXop;
        }
    }

    /**
     * Sets the value of the xmlnsXop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlnsXop(String value) {
        this.xmlnsXop = value;
    }

    /**
     * Gets the value of the attachment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attachment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttachment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attachment }
     * 
     * 
     */
    public List<Attachment> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<Attachment>();
        }
        return this.attachment;
    }

}
