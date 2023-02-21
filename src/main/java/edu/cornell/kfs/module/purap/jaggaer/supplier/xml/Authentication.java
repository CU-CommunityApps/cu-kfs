
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    protected Identity identity;
    @XmlElement(name = "SharedSecret", required = true)
    protected SharedSecret sharedSecret;

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity value) {
        this.identity = value;
    }

    public SharedSecret getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(SharedSecret value) {
        this.sharedSecret = value;
    }

}
