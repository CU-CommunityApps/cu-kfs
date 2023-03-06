
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    "lookupErrors"
})
@XmlRootElement(name = "LookupStatus")
public class LookupStatus {

    @XmlAttribute(name = "code", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String code;
    @XmlElement(name = "LookupErrors")
    protected LookupErrors lookupErrors;

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the lookupErrors property.
     * 
     * @return
     *     possible object is
     *     {@link LookupErrors }
     *     
     */
    public LookupErrors getLookupErrors() {
        return lookupErrors;
    }

    /**
     * Sets the value of the lookupErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link LookupErrors }
     *     
     */
    public void setLookupErrors(LookupErrors value) {
        this.lookupErrors = value;
    }

}
