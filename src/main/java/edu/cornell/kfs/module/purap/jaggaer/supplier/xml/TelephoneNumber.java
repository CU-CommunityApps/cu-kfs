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
@XmlType(name = "", propOrder = { "countryCode", "areaCode", "number", "extension" })
@XmlRootElement(name = "TelephoneNumber")
public class TelephoneNumber {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "CountryCode", required = true)
    private JaggaerBasicValue countryCode;
    @XmlElement(name = "AreaCode", required = true)
    private JaggaerBasicValue areaCode;
    @XmlElement(name = "Number", required = true)
    private JaggaerBasicValue number;
    @XmlElement(name = "Extension")
    private JaggaerBasicValue extension;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(JaggaerBasicValue countryCode) {
        this.countryCode = countryCode;
    }

    public JaggaerBasicValue getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(JaggaerBasicValue areaCode) {
        this.areaCode = areaCode;
    }

    public JaggaerBasicValue getNumber() {
        return number;
    }

    public void setNumber(JaggaerBasicValue number) {
        this.number = number;
    }

    public JaggaerBasicValue getExtension() {
        return extension;
    }

    public void setExtension(JaggaerBasicValue extension) {
        this.extension = extension;
    }

}
