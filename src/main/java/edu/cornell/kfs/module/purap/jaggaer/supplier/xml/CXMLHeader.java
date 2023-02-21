
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fromDomain", "fromIdentity", "toDomain", "toIdentity", "senderDomain",
        "senderIdentity", "sharedSecret", "userAgent" })
@XmlRootElement(name = "CXMLHeader")
public class CXMLHeader {

    @XmlElement(name = "FromDomain")
    protected FromDomain fromDomain;
    @XmlElement(name = "FromIdentity")
    protected FromIdentity fromIdentity;
    @XmlElement(name = "ToDomain")
    protected ToDomain toDomain;
    @XmlElement(name = "ToIdentity")
    protected ToIdentity toIdentity;
    @XmlElement(name = "SenderDomain")
    protected SenderDomain senderDomain;
    @XmlElement(name = "SenderIdentity")
    protected SenderIdentity senderIdentity;
    @XmlElement(name = "SharedSecret")
    protected SharedSecret sharedSecret;
    @XmlElement(name = "UserAgent")
    protected UserAgent userAgent;

    public FromDomain getFromDomain() {
        return fromDomain;
    }

    public void setFromDomain(FromDomain value) {
        this.fromDomain = value;
    }

    public FromIdentity getFromIdentity() {
        return fromIdentity;
    }

    public void setFromIdentity(FromIdentity value) {
        this.fromIdentity = value;
    }

    public ToDomain getToDomain() {
        return toDomain;
    }

    public void setToDomain(ToDomain value) {
        this.toDomain = value;
    }

    public ToIdentity getToIdentity() {
        return toIdentity;
    }

    public void setToIdentity(ToIdentity value) {
        this.toIdentity = value;
    }

    public SenderDomain getSenderDomain() {
        return senderDomain;
    }

    public void setSenderDomain(SenderDomain value) {
        this.senderDomain = value;
    }

    public SenderIdentity getSenderIdentity() {
        return senderIdentity;
    }

    public void setSenderIdentity(SenderIdentity value) {
        this.senderIdentity = value;
    }

    public SharedSecret getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(SharedSecret value) {
        this.sharedSecret = value;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(UserAgent value) {
        this.userAgent = value;
    }

}
