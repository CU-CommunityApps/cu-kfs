package edu.cornell.kfs.concur.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

import edu.cornell.kfs.concur.ConcurConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "payee_id", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedPayeeIdEntry {
    
    @XmlValue 
    private String content;
    @XmlAttribute (name = "id_type", required = true)
    private String idType;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

}
