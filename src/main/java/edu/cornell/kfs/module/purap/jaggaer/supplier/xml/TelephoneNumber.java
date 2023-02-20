
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "countryCode", "areaCode", "number", "extension" })
@XmlRootElement(name = "TelephoneNumber")
public class TelephoneNumber {

    @XmlElement(name = "CountryCode", required = true)
    protected CountryCode countryCode;
    @XmlElement(name = "AreaCode", required = true)
    protected AreaCode areaCode;
    @XmlElement(name = "Number", required = true)
    protected Number number;
    @XmlElement(name = "Extension")
    protected Extension extension;

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
