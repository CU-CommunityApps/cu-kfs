
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
    "serviceAreaInternalName",
    "stateServiceAreaList"
})
@XmlRootElement(name = "ServiceArea")
public class ServiceArea {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "ServiceAreaInternalName", required = true)
    protected ServiceAreaInternalName serviceAreaInternalName;
    @XmlElement(name = "StateServiceAreaList")
    protected List<StateServiceAreaList> stateServiceAreaList;

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
     * Gets the value of the serviceAreaInternalName property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceAreaInternalName }
     *     
     */
    public ServiceAreaInternalName getServiceAreaInternalName() {
        return serviceAreaInternalName;
    }

    /**
     * Sets the value of the serviceAreaInternalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceAreaInternalName }
     *     
     */
    public void setServiceAreaInternalName(ServiceAreaInternalName value) {
        this.serviceAreaInternalName = value;
    }

    /**
     * Gets the value of the stateServiceAreaList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stateServiceAreaList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStateServiceAreaList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StateServiceAreaList }
     * 
     * 
     */
    public List<StateServiceAreaList> getStateServiceAreaList() {
        if (stateServiceAreaList == null) {
            stateServiceAreaList = new ArrayList<StateServiceAreaList>();
        }
        return this.stateServiceAreaList;
    }

}
