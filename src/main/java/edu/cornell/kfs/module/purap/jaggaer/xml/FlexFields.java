package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "flexField1", "flexField2", "flexField3", "flexField4", "flexField5" })
@XmlRootElement(name = "FlexFields")
public class FlexFields {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "FlexField1")
    protected JaggaerBasicValue flexField1;
    @XmlElement(name = "FlexField2")
    protected JaggaerBasicValue flexField2;
    @XmlElement(name = "FlexField3")
    protected JaggaerBasicValue flexField3;
    @XmlElement(name = "FlexField4")
    protected JaggaerBasicValue flexField4;
    @XmlElement(name = "FlexField5")
    protected JaggaerBasicValue flexField5;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public JaggaerBasicValue getFlexField1() {
        return flexField1;
    }

    public void setFlexField1(JaggaerBasicValue value) {
        this.flexField1 = value;
    }

    public JaggaerBasicValue getFlexField2() {
        return flexField2;
    }

    public void setFlexField2(JaggaerBasicValue value) {
        this.flexField2 = value;
    }

    public JaggaerBasicValue getFlexField3() {
        return flexField3;
    }

    public void setFlexField3(JaggaerBasicValue value) {
        this.flexField3 = value;
    }

    public JaggaerBasicValue getFlexField4() {
        return flexField4;
    }

    public void setFlexField4(JaggaerBasicValue value) {
        this.flexField4 = value;
    }

    public JaggaerBasicValue getFlexField5() {
        return flexField5;
    }

    public void setFlexField5(JaggaerBasicValue value) {
        this.flexField5 = value;
    }

}
