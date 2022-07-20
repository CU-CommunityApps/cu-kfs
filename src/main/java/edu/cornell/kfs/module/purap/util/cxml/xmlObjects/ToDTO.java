package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "credentials",
    "correspondent"
})
@XmlRootElement(name = "To")
public class ToDTO {

    @XmlElement(name = "Credential", required = true)
    private List<CredentialDTO> credentials;

    @XmlElement(name = "Correspondent")
    private CorrespondentDTO correspondent;

    public List<CredentialDTO> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<CredentialDTO> credentials) {
        this.credentials = credentials;
    }

    public CorrespondentDTO getCorrespondent() {
        return correspondent;
    }

    public void setCorrespondent(CorrespondentDTO correspondent) {
        this.correspondent = correspondent;
    }

}
