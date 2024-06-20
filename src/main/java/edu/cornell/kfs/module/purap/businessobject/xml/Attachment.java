package edu.cornell.kfs.module.purap.businessobject.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "mimeTypeCode", "fileName", "attachmentType" })
@XmlRootElement(name = "attachment")
public class Attachment {

    private String mimeTypeCode;
    @XmlElement(required = true)
    private String fileName;
    @XmlElement(required = true)
    private String attachmentType;

    public String getMimeTypeCode() {
        return mimeTypeCode;
    }

    public void setMimeTypeCode(String mimeTypeCode) {
        this.mimeTypeCode = mimeTypeCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }
    
    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }
}
