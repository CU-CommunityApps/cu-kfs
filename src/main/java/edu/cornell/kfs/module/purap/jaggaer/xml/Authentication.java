package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "identity", "sharedSecret" })
@XmlRootElement(name = "Authentication")
public class Authentication {

    @XmlElement(name = "Identity", required = true)
    protected String identity;
    @XmlElement(name = "SharedSecret", required = true)
    protected String sharedSecret;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String value) {
        this.identity = value;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String value) {
        this.sharedSecret = value;
    }

}
