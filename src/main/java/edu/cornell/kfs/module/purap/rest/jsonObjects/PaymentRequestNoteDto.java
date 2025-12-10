package edu.cornell.kfs.module.purap.rest.jsonObjects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PaymentRequestNoteDto {
    private String noteText;
    private String noteType;

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
        return ToStringBuilder.reflectionToString(this);
    }
}
