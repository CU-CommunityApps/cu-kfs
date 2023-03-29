package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.Date;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;
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
    "attachmentDisplayName",
    "attachmentFileName",
    "attachmentType",
    "attachmentFTPpath",
    "attachmentBase64"
})
@XmlRootElement(name = "AttachmentFile")
public class AttachmentFile {

    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String id;

    @XmlAttribute(name = "version")
    private Integer version;

    @XmlAttribute(name = "size")
    private Integer size;

    /*
     * NOTE: For the phase 2 work (KFSPTS-26285), we need to revisit whether this attribute
     * should be treated as date-only. Jaggaer's documentation treats this attribute as
     * a date-time value but manually exported contract XML shows it as a date-only value.
     * Alternatively, we could remove this from our bean since we don't need it for processing;
     * it's just included here to provide more reference information for logging purposes.
     */
    @XmlAttribute(name = "dateUploaded")
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    private Date dateUploaded;

    @XmlElement(name = "AttachmentDisplayName", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String attachmentDisplayName;

    @XmlElement(name = "AttachmentFileName", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String attachmentFileName;

    @XmlElement(name = "AttachmentType", required = true)
    private String attachmentType;

    @XmlElement(name = "AttachmentFTPpath")
    private String attachmentFTPpath;

    @XmlElement(name = "AttachmentBase64")
    private String attachmentBase64;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getAttachmentDisplayName() {
        return attachmentDisplayName;
    }

    public void setAttachmentDisplayName(String attachmentDisplayName) {
        this.attachmentDisplayName = attachmentDisplayName;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentFTPpath() {
        return attachmentFTPpath;
    }

    public void setAttachmentFTPpath(String attachmentFTPpath) {
        this.attachmentFTPpath = attachmentFTPpath;
    }

    public String getAttachmentBase64() {
        return attachmentBase64;
    }

    public void setAttachmentBase64(String attachmentBase64) {
        this.attachmentBase64 = attachmentBase64;
    }

}
