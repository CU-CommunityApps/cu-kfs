package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.krad.bo.Attachment;

public class BatchIWantAttachment extends Attachment{

    private String attachmentType;

    public BatchIWantAttachment() {}

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }


}
