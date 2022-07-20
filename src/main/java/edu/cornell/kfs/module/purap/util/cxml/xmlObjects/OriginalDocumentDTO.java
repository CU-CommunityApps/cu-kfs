package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "OriginalDocument")
public class OriginalDocumentDTO {

    @XmlAttribute(name = "payloadID", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String payloadID;

    public String getPayloadID() {
        return payloadID;
    }

    public void setPayloadID(String payloadID) {
        this.payloadID = payloadID;
    }

}
