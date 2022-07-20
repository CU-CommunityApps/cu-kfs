package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.cornell.kfs.module.purap.util.cxml.CxmlConstants.CxmlDefaults;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contents"
})
@XmlRootElement(name = "DigitalSignature")
public class DigitalSignatureDTO {

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;

    @XmlAttribute(name = "encoding")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String encoding;

    @XmlMixed
    @XmlAnyElement
    private List<Object> contents;

    public DigitalSignatureDTO() {
        this(CxmlDefaults.SIGNATURE_PK7_SELF_CONTAINED, CxmlDefaults.ENCODING_BASE64);
    }

    public DigitalSignatureDTO(String type, String encoding) {
        this.type = type;
        this.encoding = encoding;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<Object> getContents() {
        return contents;
    }

    public void setContents(List<Object> contents) {
        this.contents = contents;
    }

}
