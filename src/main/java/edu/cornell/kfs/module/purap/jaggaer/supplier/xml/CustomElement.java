
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "customElementIdentifier",
    "displayName",
    "customElementValueListOrAttachments"
})
@XmlRootElement(name = "CustomElement")
public class CustomElement {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "isActive")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isActive;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlElement(name = "CustomElementIdentifier")
    protected CustomElementIdentifier customElementIdentifier;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;
    @XmlElements({
        @XmlElement(name = "CustomElementValueList", type = CustomElementValueList.class),
        @XmlElement(name = "Attachments", type = Attachments.class)
    })
    protected List<Object> customElementValueListOrAttachments;

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
     * Gets the value of the isActive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsActive() {
        return isActive;
    }

    /**
     * Sets the value of the isActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsActive(String value) {
        this.isActive = value;
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
     * Gets the value of the customElementIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link CustomElementIdentifier }
     *     
     */
    public CustomElementIdentifier getCustomElementIdentifier() {
        return customElementIdentifier;
    }

    /**
     * Sets the value of the customElementIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomElementIdentifier }
     *     
     */
    public void setCustomElementIdentifier(CustomElementIdentifier value) {
        this.customElementIdentifier = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link DisplayName }
     *     
     */
    public DisplayName getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisplayName }
     *     
     */
    public void setDisplayName(DisplayName value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the customElementValueListOrAttachments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customElementValueListOrAttachments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomElementValueListOrAttachments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomElementValueList }
     * {@link Attachments }
     * 
     * 
     */
    public List<Object> getCustomElementValueListOrAttachments() {
        if (customElementValueListOrAttachments == null) {
            customElementValueListOrAttachments = new ArrayList<Object>();
        }
        return this.customElementValueListOrAttachments;
    }

}
