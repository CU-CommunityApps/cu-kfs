package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "identity",
    "secretValues"
})
@XmlRootElement(name = "Credential")
public class CredentialDTO {

    @XmlAttribute(name = "domain", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String domain;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String type;

    @XmlElement(name = "Identity", required = true)
    private IdentityDTO identity;

    @XmlElements({
        @XmlElement(name = "SharedSecret", type = SharedSecretDTO.class),
        @XmlElement(name = "DigitalSignature", type = DigitalSignatureDTO.class),
        @XmlElement(name = "CredentialMac", type = CredentialMacDTO.class)
    })
    private List<Object> secretValues;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IdentityDTO getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityDTO identity) {
        this.identity = identity;
    }

    public List<Object> getSecretValues() {
        return secretValues;
    }

    public void setSecretValues(List<Object> secretValues) {
        this.secretValues = secretValues;
    }

}
