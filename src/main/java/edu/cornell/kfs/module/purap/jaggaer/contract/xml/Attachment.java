package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attachmentFile"
})
@XmlRootElement(name = "Attachment")
public class Attachment {

    @XmlElement(name = "AttachmentFile")
    private AttachmentFile attachmentFile;

    public AttachmentFile getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(AttachmentFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

}
