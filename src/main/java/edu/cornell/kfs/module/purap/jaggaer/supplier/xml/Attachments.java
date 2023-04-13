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
@XmlType(name = "", propOrder = { "attachment" })
@XmlRootElement(name = "Attachments")
public class Attachments {

    @XmlAttribute(name = "xmlns:xop")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String xmlnsXop;
    @XmlElement(name = "Attachment")
    private List<Attachment> attachment;

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

    public List<Attachment> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<Attachment>();
        }
        return this.attachment;
    }

}
