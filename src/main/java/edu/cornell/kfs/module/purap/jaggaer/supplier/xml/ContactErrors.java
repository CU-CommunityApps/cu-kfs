
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
    "contactRef",
    "errorMessage"
})
@XmlRootElement(name = "ContactErrors")
public class ContactErrors {

    @XmlElement(name = "ContactRef", required = true)
    protected ContactRef contactRef;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

    /**
     * Gets the value of the contactRef property.
     * 
     * @return
     *     possible object is
     *     {@link ContactRef }
     *     
     */
    public ContactRef getContactRef() {
        return contactRef;
    }

    /**
     * Sets the value of the contactRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactRef }
     *     
     */
    public void setContactRef(ContactRef value) {
        this.contactRef = value;
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
