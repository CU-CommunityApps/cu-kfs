package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.cornell.kfs.sys.xmladapters.DateTimeUTCOffsetStringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "CredentialMac")
public class CredentialMacDTO {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;

    @XmlAttribute(name = "algorithm", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String algorithm;

    @XmlAttribute(name = "creationDate", required = true)
    @XmlJavaTypeAdapter(DateTimeUTCOffsetStringToJavaDateAdapter.class)
    private Date creationDate;

    @XmlAttribute(name = "expirationDate", required = true)
    @XmlJavaTypeAdapter(DateTimeUTCOffsetStringToJavaDateAdapter.class)
    private Date expirationDate;

    @XmlValue
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
