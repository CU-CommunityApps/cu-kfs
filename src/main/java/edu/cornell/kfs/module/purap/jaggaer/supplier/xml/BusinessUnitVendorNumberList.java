
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "businessUnitVendorNumber"
})
@XmlRootElement(name = "BusinessUnitVendorNumberList")
public class BusinessUnitVendorNumberList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "BusinessUnitVendorNumber")
    protected List<BusinessUnitVendorNumber> businessUnitVendorNumber;

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
     * Gets the value of the businessUnitVendorNumber property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the businessUnitVendorNumber property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBusinessUnitVendorNumber().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BusinessUnitVendorNumber }
     * 
     * 
     */
    public List<BusinessUnitVendorNumber> getBusinessUnitVendorNumber() {
        if (businessUnitVendorNumber == null) {
            businessUnitVendorNumber = new ArrayList<BusinessUnitVendorNumber>();
        }
        return this.businessUnitVendorNumber;
    }

}
