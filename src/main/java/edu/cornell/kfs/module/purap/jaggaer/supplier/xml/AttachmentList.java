package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.module.purap.JaggaerConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "attachments" })
@XmlRootElement(name = "Attachments")
public class AttachmentList {

    @XmlAttribute(name = "xmlns:xop")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String xmlnsXop;
    @XmlElement(name = "Attachment")
    private List<Attachment> attachments;

    public String getXmlnsXop() {
        if (xmlnsXop == null) {
            return JaggaerConstants.DEFAULT_XML_NS_XOP;
        } else {
            return xmlnsXop;
        }
    }

    public void setXmlnsXop(String xmlnsXop) {
        this.xmlnsXop = xmlnsXop;
    }

    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<Attachment>();
        }
        return this.attachments;
    }

}
