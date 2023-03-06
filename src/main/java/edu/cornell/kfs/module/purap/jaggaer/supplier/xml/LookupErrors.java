
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
    "lookupErrorMessage"
})
@XmlRootElement(name = "LookupErrors")
public class LookupErrors {

    @XmlElement(name = "LookupErrorMessage", required = true)
    protected List<LookupErrorMessage> lookupErrorMessage;

    /**
     * Gets the value of the lookupErrorMessage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lookupErrorMessage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLookupErrorMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LookupErrorMessage }
     * 
     * 
     */
    public List<LookupErrorMessage> getLookupErrorMessage() {
        if (lookupErrorMessage == null) {
            lookupErrorMessage = new ArrayList<LookupErrorMessage>();
        }
        return this.lookupErrorMessage;
    }

}
