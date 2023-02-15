package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "lookupErrors" })
@XmlRootElement(name = "LookupStatus")
public class LookupStatus {

    @XmlAttribute(name = "code", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String code;
    @XmlElement(name = "LookupErrors")
    protected LookupErrors lookupErrors;

    public String getCode() {
        return code;
    }

    public void setCode(String value) {
        this.code = value;
    }

    public LookupErrors getLookupErrors() {
        return lookupErrors;
    }

    public void setLookupErrors(LookupErrors value) {
        this.lookupErrors = value;
    }

}
