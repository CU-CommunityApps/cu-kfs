
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "internalName",
    "displayName",
    "additionalDataList",
    "dd214Certificate",
    "diversityCertificate"
})
@XmlRootElement(name = "DiversityClassification")
public class DiversityClassification {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "InternalName", required = true)
    protected InternalName internalName;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;
    @XmlElement(name = "AdditionalDataList")
    protected AdditionalDataList additionalDataList;
    @XmlElement(name = "DD-214Certificate")
    protected DD214Certificate dd214Certificate;
    @XmlElement(name = "DiversityCertificate")
    protected DiversityCertificate diversityCertificate;

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
     * Gets the value of the internalName property.
     * 
     * @return
     *     possible object is
     *     {@link InternalName }
     *     
     */
    public InternalName getInternalName() {
        return internalName;
    }

    /**
     * Sets the value of the internalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link InternalName }
     *     
     */
    public void setInternalName(InternalName value) {
        this.internalName = value;
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
     * Gets the value of the additionalDataList property.
     * 
     * @return
     *     possible object is
     *     {@link AdditionalDataList }
     *     
     */
    public AdditionalDataList getAdditionalDataList() {
        return additionalDataList;
    }

    /**
     * Sets the value of the additionalDataList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdditionalDataList }
     *     
     */
    public void setAdditionalDataList(AdditionalDataList value) {
        this.additionalDataList = value;
    }

    /**
     * Gets the value of the dd214Certificate property.
     * 
     * @return
     *     possible object is
     *     {@link DD214Certificate }
     *     
     */
    public DD214Certificate getDD214Certificate() {
        return dd214Certificate;
    }

    /**
     * Sets the value of the dd214Certificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link DD214Certificate }
     *     
     */
    public void setDD214Certificate(DD214Certificate value) {
        this.dd214Certificate = value;
    }

    /**
     * Gets the value of the diversityCertificate property.
     * 
     * @return
     *     possible object is
     *     {@link DiversityCertificate }
     *     
     */
    public DiversityCertificate getDiversityCertificate() {
        return diversityCertificate;
    }

    /**
     * Sets the value of the diversityCertificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiversityCertificate }
     *     
     */
    public void setDiversityCertificate(DiversityCertificate value) {
        this.diversityCertificate = value;
    }

}
