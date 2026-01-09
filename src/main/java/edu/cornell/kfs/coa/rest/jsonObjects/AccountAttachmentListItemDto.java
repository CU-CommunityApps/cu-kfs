package edu.cornell.kfs.coa.rest.jsonObjects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountAttachmentListItemDto {

    private String attachmentId;
    private String attachmentNote;
    private String fileName;
    private String mimeTypeCode;
    private Long fileSizeInBytes;

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(final String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentNote() {
        return attachmentNote;
    }

    public void setAttachmentNote(final String attachmentNote) {
        this.attachmentNote = attachmentNote;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getMimeTypeCode() {
        return mimeTypeCode;
    }

    public void setMimeTypeCode(final String mimeTypeCode) {
        this.mimeTypeCode = mimeTypeCode;
    }

    public Long getFileSizeInBytes() {
        return fileSizeInBytes;
    }

    public void setFileSizeInBytes(final Long fileSizeInBytes) {
        this.fileSizeInBytes = fileSizeInBytes;
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
