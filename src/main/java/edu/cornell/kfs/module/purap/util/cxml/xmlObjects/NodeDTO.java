package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "credentials"
})
@XmlRootElement(name = "Node")
public class NodeDTO {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String type;

    @XmlAttribute(name = "itemDetailsRequired")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String itemDetailsRequired;

    @XmlElement(name = "Credential", required = true)
    private List<CredentialDTO> credentials;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getItemDetailsRequired() {
        return itemDetailsRequired;
    }

    public void setItemDetailsRequired(String itemDetailsRequired) {
        this.itemDetailsRequired = itemDetailsRequired;
    }

    public List<CredentialDTO> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<CredentialDTO> credentials) {
        this.credentials = credentials;
    }

}
