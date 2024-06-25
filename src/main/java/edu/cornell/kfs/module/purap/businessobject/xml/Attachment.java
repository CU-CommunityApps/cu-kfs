package edu.cornell.kfs.module.purap.businessobject.xml;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "mimeTypeCode", "fileName", "attachmentType" })
@XmlRootElement(name = "attachment", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
public class Attachment {

    @XmlElement(name = "mimeTypeCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String mimeTypeCode;

    @XmlElement(name = "fileName", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
    private String fileName;

    @XmlElement(name = "attachmentType", namespace = "http://www.kuali.org/kfs/purap/iWantDocument", required = true)
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
