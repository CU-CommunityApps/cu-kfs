package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.Date;
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

import edu.cornell.kfs.module.purap.util.cxml.CxmlConstants.CxmlDefaults;
import edu.cornell.kfs.sys.xmladapters.DateTimeUTCOffsetStringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cxmlSections",
    "dsSignatures"
})
@XmlRootElement(name = "cXML")
public class CxmlDTO {

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String version;

    @XmlAttribute(name = "payloadID", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String payloadID;

    @XmlAttribute(name = "timestamp", required = true)
    @XmlJavaTypeAdapter(DateTimeUTCOffsetStringToJavaDateAdapter.class)
    private Date timestamp;

    @XmlAttribute(name = "signatureVersion")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String signatureVersion;

    @XmlAttribute(name = "xml:lang")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String xmlLang;

    @XmlElements({
        @XmlElement(name = "Header", required = true, type = HeaderDTO.class),
        @XmlElement(name = "Message", required = true, type = IgnoredElementDTO.class),
        @XmlElement(name = "Request", required = true, type = RequestDTO.class),
        @XmlElement(name = "Response", required = true, type = IgnoredElementDTO.class)
    })
    private List<Object> cxmlSections;

    @XmlElement(name = "ds:Signature")
    private List<IgnoredElementDTO> dsSignatures;

    public CxmlDTO() {
        this(CxmlDefaults.CXML_VERSION_1_2_019);
    }

    public CxmlDTO(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPayloadID() {
        return payloadID;
    }

    public void setPayloadID(String payloadID) {
        this.payloadID = payloadID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getXmlLang() {
        return xmlLang;
    }

    public void setXmlLang(String xmlLang) {
        this.xmlLang = xmlLang;
    }

    public List<Object> getCxmlSections() {
        return cxmlSections;
    }

    public void setCxmlSections(List<Object> cxmlSections) {
        this.cxmlSections = cxmlSections;
    }

    public List<IgnoredElementDTO> getDsSignatures() {
        return dsSignatures;
    }

    public void setDsSignatures(List<IgnoredElementDTO> dsSignatures) {
        this.dsSignatures = dsSignatures;
    }

}
