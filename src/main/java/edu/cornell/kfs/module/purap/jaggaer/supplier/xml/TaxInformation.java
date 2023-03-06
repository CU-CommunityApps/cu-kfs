
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    "taxDocumentName",
    "taxDocumentYear",
    "taxDocument"
})
@XmlRootElement(name = "TaxInformation")
public class TaxInformation {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "TaxDocumentName")
    protected TaxDocumentName taxDocumentName;
    @XmlElement(name = "TaxDocumentYear")
    protected TaxDocumentYear taxDocumentYear;
    @XmlElement(name = "TaxDocument")
    protected TaxDocument taxDocument;

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
     * Gets the value of the taxDocumentName property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDocumentName }
     *     
     */
    public TaxDocumentName getTaxDocumentName() {
        return taxDocumentName;
    }

    /**
     * Sets the value of the taxDocumentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDocumentName }
     *     
     */
    public void setTaxDocumentName(TaxDocumentName value) {
        this.taxDocumentName = value;
    }

    /**
     * Gets the value of the taxDocumentYear property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDocumentYear }
     *     
     */
    public TaxDocumentYear getTaxDocumentYear() {
        return taxDocumentYear;
    }

    /**
     * Sets the value of the taxDocumentYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDocumentYear }
     *     
     */
    public void setTaxDocumentYear(TaxDocumentYear value) {
        this.taxDocumentYear = value;
    }

    /**
     * Gets the value of the taxDocument property.
     * 
     * @return
     *     possible object is
     *     {@link TaxDocument }
     *     
     */
    public TaxDocument getTaxDocument() {
        return taxDocument;
    }

    /**
     * Sets the value of the taxDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxDocument }
     *     
     */
    public void setTaxDocument(TaxDocument value) {
        this.taxDocument = value;
    }

}
