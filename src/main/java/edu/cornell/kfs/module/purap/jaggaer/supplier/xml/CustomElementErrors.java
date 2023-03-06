
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
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
    "customElementIdentifier",
    "errorMessage"
})
@XmlRootElement(name = "CustomElementErrors")
public class CustomElementErrors {

    @XmlElement(name = "CustomElementIdentifier", required = true)
    protected CustomElementIdentifier customElementIdentifier;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

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
     * Gets the value of the errorMessage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorMessage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ErrorMessage }
     * 
     * 
     */
    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
