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
    protected AreaCode areaCode;
    @XmlElement(name = "Number", required = true)
    protected Number number;
    @XmlElement(name = "Extension")
    protected Extension extension;

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

    public AreaCode getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(AreaCode value) {
        this.areaCode = value;
    }

    public Number getNumber() {
        return number;
    }

    public void setNumber(Number value) {
        this.number = value;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension value) {
        this.extension = value;
    }

}
