package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contacts",
    "extrinsics"
})
@XmlRootElement(name = "Correspondent")
public class CorrespondentDTO {

    @XmlAttribute(name = "preferredLanguage")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String preferredLanguage;

    @XmlElement(name = "Contact", required = true)
    private List<ContactDTO> contacts;

    @XmlElement(name = "Extrinsic")
    private List<ExtrinsicDTO> extrinsics;

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public List<ContactDTO> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactDTO> contacts) {
        this.contacts = contacts;
    }

    public List<ExtrinsicDTO> getExtrinsics() {
        return extrinsics;
    }

    public void setExtrinsics(List<ExtrinsicDTO> extrinsics) {
        this.extrinsics = extrinsics;
    }

}
