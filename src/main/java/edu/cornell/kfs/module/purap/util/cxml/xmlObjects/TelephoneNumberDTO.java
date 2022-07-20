package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "countryCode",
    "areaOrCityCode",
    "number",
    "extension"
})
@XmlRootElement(name = "TelephoneNumber")
public class TelephoneNumberDTO {

    @XmlElement(name = "CountryCode", required = true)
    private CountryCodeDTO countryCode;

    @XmlElement(name = "AreaOrCityCode", required = true)
    private String areaOrCityCode;

    @XmlElement(name = "Number", required = true)
    private String number;

    @XmlElement(name = "Extension")
    private String extension;

    public CountryCodeDTO getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(CountryCodeDTO countryCode) {
        this.countryCode = countryCode;
    }

    public String getAreaOrCityCode() {
        return areaOrCityCode;
    }

    public void setAreaOrCityCode(String areaOrCityCode) {
        this.areaOrCityCode = areaOrCityCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}
