package edu.cornell.kfs.module.purap.rest.jsonObjects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PaymentRequestNoteDto {
    private String noteText;
    private String noteType;
    private String attachmentFileName;
    private String attachmentMimeType;
    private String attachmentContent;

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(final String noteText) {
        this.noteText = noteText;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(final String noteType) {
        this.noteType = noteType;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(final String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

    public String getAttachmentMimeType() {
        return attachmentMimeType;
    }

    public void setAttachmentMimeType(final String attachmentMimeType) {
        this.attachmentMimeType = attachmentMimeType;
    }

    public String getAttachmentContent() {
        return attachmentContent;
    }

    public void setAttachmentContent(final String attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    public boolean hasAttachment() {
        return StringUtils.isNotBlank(attachmentFileName) 
                && StringUtils.isNotBlank(attachmentMimeType) 
                && StringUtils.isNotBlank(attachmentContent);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("noteText", noteText)
                .append("noteType", noteType)
                .append("attachmentFileName", attachmentFileName)
                .append("attachmentMimeType", attachmentMimeType)
                .append("attachmentContent", attachmentContent != null ? "[BASE64 CONTENT - " + attachmentContent.length() + " chars]" : null)
                .toString();
    }
}
