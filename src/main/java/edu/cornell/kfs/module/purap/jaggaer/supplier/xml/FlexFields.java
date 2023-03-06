
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    "flexField1",
    "flexField2",
    "flexField3",
    "flexField4",
    "flexField5"
})
@XmlRootElement(name = "FlexFields")
public class FlexFields {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "FlexField1")
    protected FlexField1 flexField1;
    @XmlElement(name = "FlexField2")
    protected FlexField2 flexField2;
    @XmlElement(name = "FlexField3")
    protected FlexField3 flexField3;
    @XmlElement(name = "FlexField4")
    protected FlexField4 flexField4;
    @XmlElement(name = "FlexField5")
    protected FlexField5 flexField5;

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
     * Gets the value of the flexField1 property.
     * 
     * @return
     *     possible object is
     *     {@link FlexField1 }
     *     
     */
    public FlexField1 getFlexField1() {
        return flexField1;
    }

    /**
     * Sets the value of the flexField1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexField1 }
     *     
     */
    public void setFlexField1(FlexField1 value) {
        this.flexField1 = value;
    }

    /**
     * Gets the value of the flexField2 property.
     * 
     * @return
     *     possible object is
     *     {@link FlexField2 }
     *     
     */
    public FlexField2 getFlexField2() {
        return flexField2;
    }

    /**
     * Sets the value of the flexField2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexField2 }
     *     
     */
    public void setFlexField2(FlexField2 value) {
        this.flexField2 = value;
    }

    /**
     * Gets the value of the flexField3 property.
     * 
     * @return
     *     possible object is
     *     {@link FlexField3 }
     *     
     */
    public FlexField3 getFlexField3() {
        return flexField3;
    }

    /**
     * Sets the value of the flexField3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexField3 }
     *     
     */
    public void setFlexField3(FlexField3 value) {
        this.flexField3 = value;
    }

    /**
     * Gets the value of the flexField4 property.
     * 
     * @return
     *     possible object is
     *     {@link FlexField4 }
     *     
     */
    public FlexField4 getFlexField4() {
        return flexField4;
    }

    /**
     * Sets the value of the flexField4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexField4 }
     *     
     */
    public void setFlexField4(FlexField4 value) {
        this.flexField4 = value;
    }

    /**
     * Gets the value of the flexField5 property.
     * 
     * @return
     *     possible object is
     *     {@link FlexField5 }
     *     
     */
    public FlexField5 getFlexField5() {
        return flexField5;
    }

    /**
     * Sets the value of the flexField5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexField5 }
     *     
     */
    public void setFlexField5(FlexField5 value) {
        this.flexField5 = value;
    }

}
