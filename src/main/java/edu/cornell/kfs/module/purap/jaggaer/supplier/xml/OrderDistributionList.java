
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
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
    "distributionLanguage",
    "distributionMethod"
})
@XmlRootElement(name = "OrderDistributionList")
public class OrderDistributionList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "DistributionLanguage")
    protected String distributionLanguage;
    @XmlElement(name = "DistributionMethod", required = true)
    protected List<DistributionMethod> distributionMethod;

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
     * Gets the value of the distributionLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDistributionLanguage() {
        return distributionLanguage;
    }

    /**
     * Sets the value of the distributionLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDistributionLanguage(String value) {
        this.distributionLanguage = value;
    }

    /**
     * Gets the value of the distributionMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the distributionMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDistributionMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DistributionMethod }
     * 
     * 
     */
    public List<DistributionMethod> getDistributionMethod() {
        if (distributionMethod == null) {
            distributionMethod = new ArrayList<DistributionMethod>();
        }
        return this.distributionMethod;
    }

}
