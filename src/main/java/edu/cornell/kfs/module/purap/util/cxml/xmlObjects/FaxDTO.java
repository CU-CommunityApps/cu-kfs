package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contactInfos"
})
@XmlRootElement(name = "Fax")
public class FaxDTO {

    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;

    @XmlElements({
        @XmlElement(name = "TelephoneNumber", required = true, type = TelephoneNumberDTO.class),
        @XmlElement(name = "URL", required = true, type = UrlDTO.class),
        @XmlElement(name = "Email", required = true, type = EmailDTO.class)
    })
    private List<Object> contactInfos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getContactInfos() {
        return contactInfos;
    }

    public void setContactInfos(List<Object> contactInfos) {
        this.contactInfos = contactInfos;
    }

}
