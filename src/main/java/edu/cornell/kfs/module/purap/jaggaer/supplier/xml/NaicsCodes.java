
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "primaryNaicsOrSecondaryNaicsList"
})
@XmlRootElement(name = "NaicsCodes")
public class NaicsCodes {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElements({
        @XmlElement(name = "PrimaryNaics", type = PrimaryNaics.class),
        @XmlElement(name = "SecondaryNaicsList", type = SecondaryNaicsList.class)
    })
    protected List<Object> primaryNaicsOrSecondaryNaicsList;

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
     * Gets the value of the primaryNaicsOrSecondaryNaicsList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the primaryNaicsOrSecondaryNaicsList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrimaryNaicsOrSecondaryNaicsList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PrimaryNaics }
     * {@link SecondaryNaicsList }
     * 
     * 
     */
    public List<Object> getPrimaryNaicsOrSecondaryNaicsList() {
        if (primaryNaicsOrSecondaryNaicsList == null) {
            primaryNaicsOrSecondaryNaicsList = new ArrayList<Object>();
        }
        return this.primaryNaicsOrSecondaryNaicsList;
    }

}
