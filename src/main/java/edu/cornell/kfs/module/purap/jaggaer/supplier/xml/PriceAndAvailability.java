
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "enabled", "messageProtocol", "url", "customerCode", "locationCodeType",
        "fallbackLocationCode", "identity", "sharedSecret" })
@XmlRootElement(name = "PriceAndAvailability")
public class PriceAndAvailability {

    @XmlElement(name = "Enabled")
    protected Enabled enabled;
    @XmlElement(name = "MessageProtocol")
    protected MessageProtocol messageProtocol;
    @XmlElement(name = "URL")
    protected URL url;
    @XmlElement(name = "CustomerCode")
    protected CustomerCode customerCode;
    @XmlElement(name = "LocationCodeType")
    protected LocationCodeType locationCodeType;
    @XmlElement(name = "FallbackLocationCode")
    protected FallbackLocationCode fallbackLocationCode;
    @XmlElement(name = "Identity")
    protected Identity identity;
    @XmlElement(name = "SharedSecret")
    protected SharedSecret sharedSecret;

    public Enabled getEnabled() {
        return enabled;
    }

    public void setEnabled(Enabled value) {
        this.enabled = value;
    }

    public MessageProtocol getMessageProtocol() {
        return messageProtocol;
    }

    public void setMessageProtocol(MessageProtocol value) {
        this.messageProtocol = value;
    }

    public URL getURL() {
        return url;
    }

    public void setURL(URL value) {
        this.url = value;
    }

    public CustomerCode getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(CustomerCode value) {
        this.customerCode = value;
    }

    public LocationCodeType getLocationCodeType() {
        return locationCodeType;
    }

    public void setLocationCodeType(LocationCodeType value) {
        this.locationCodeType = value;
    }

    public FallbackLocationCode getFallbackLocationCode() {
        return fallbackLocationCode;
    }

    public void setFallbackLocationCode(FallbackLocationCode value) {
        this.fallbackLocationCode = value;
    }

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
