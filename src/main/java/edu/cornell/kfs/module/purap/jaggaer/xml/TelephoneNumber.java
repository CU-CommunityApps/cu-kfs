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
@XmlType(name = "", propOrder = { "countryCode", "areaCode", "number", "extension" })
@XmlRootElement(name = "TelephoneNumber")
public class TelephoneNumber {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "CountryCode", required = true)
    protected CountryCode countryCode;
    @XmlElement(name = "AreaCode", required = true)
    protected JaggaerBasicValue areaCode;
    @XmlElement(name = "Number", required = true)
    protected JaggaerBasicValue number;
    @XmlElement(name = "Extension")
    protected JaggaerBasicValue extension;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(CountryCode value) {
        this.countryCode = value;
    }

    public JaggaerBasicValue getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(JaggaerBasicValue value) {
        this.areaCode = value;
    }

    public JaggaerBasicValue getNumber() {
        return number;
    }

    public void setNumber(JaggaerBasicValue value) {
        this.number = value;
    }

    public JaggaerBasicValue getExtension() {
        return extension;
    }

    public void setExtension(JaggaerBasicValue value) {
        this.extension = value;
    }

}
