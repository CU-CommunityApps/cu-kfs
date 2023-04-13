package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    private String isChanged;
    @XmlElement(name = "FlexField1")
    private JaggaerBasicValue flexField1;
    @XmlElement(name = "FlexField2")
    private JaggaerBasicValue flexField2;
    @XmlElement(name = "FlexField3")
    private JaggaerBasicValue flexField3;
    @XmlElement(name = "FlexField4")
    private JaggaerBasicValue flexField4;
    @XmlElement(name = "FlexField5")
    private JaggaerBasicValue flexField5;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getFlexField1() {
        return flexField1;
    }

    public void setFlexField1(JaggaerBasicValue flexField1) {
        this.flexField1 = flexField1;
    }

    public JaggaerBasicValue getFlexField2() {
        return flexField2;
    }

    public void setFlexField2(JaggaerBasicValue flexField2) {
        this.flexField2 = flexField2;
    }

    public JaggaerBasicValue getFlexField3() {
        return flexField3;
    }

    public void setFlexField3(JaggaerBasicValue flexField3) {
        this.flexField3 = flexField3;
    }

    public JaggaerBasicValue getFlexField4() {
        return flexField4;
    }

    public void setFlexField4(JaggaerBasicValue flexField4) {
        this.flexField4 = flexField4;
    }

    public JaggaerBasicValue getFlexField5() {
        return flexField5;
    }

    public void setFlexField5(JaggaerBasicValue flexField5) {
        this.flexField5 = flexField5;
    }

}
