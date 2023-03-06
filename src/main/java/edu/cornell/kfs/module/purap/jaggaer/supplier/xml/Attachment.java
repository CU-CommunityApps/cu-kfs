
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attachmentName",
    "attachmentURL",
    "attachmentSize",
    "xopInclude"
})
@XmlRootElement(name = "Attachment")
public class Attachment {

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlElement(name = "AttachmentName", required = true)
    protected String attachmentName;
    @XmlElement(name = "AttachmentURL")
    protected String attachmentURL;
    @XmlElement(name = "AttachmentSize")
    protected String attachmentSize;
    @XmlElement(name = "xop:Include")
    protected XopInclude xopInclude;

    
    public String getId() {
        return id;
    }

    
    public void setId(String value) {
        this.id = value;
    }

    
    public String getType() {
        return type;
    }

    
    public void setType(String value) {
        this.type = value;
    }

    
    public String getAttachmentName() {
        return attachmentName;
    }

    
    public void setAttachmentName(String value) {
        this.attachmentName = value;
    }

    
    public String getAttachmentURL() {
        return attachmentURL;
    }

    
    public void setAttachmentURL(String value) {
        this.attachmentURL = value;
    }

    
    public String getAttachmentSize() {
        return attachmentSize;
    }

    
    public void setAttachmentSize(String value) {
        this.attachmentSize = value;
    }

    
    public XopInclude getXopInclude() {
        return xopInclude;
    }

    
    public void setXopInclude(XopInclude value) {
        this.xopInclude = value;
    }

}
