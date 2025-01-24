package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;

@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = Note.class)
})
public class NoteLite {

    @TaxDtoField
    private Long noteIdentifier;

    @TaxDtoField
    private String remoteObjectIdentifier;

    @TaxDtoField
    private String noteText;

    public Long getNoteIdentifier() {
        return noteIdentifier;
    }

    public void setNoteIdentifier(final Long noteIdentifier) {
        this.noteIdentifier = noteIdentifier;
    }

    public String getRemoteObjectIdentifier() {
        return remoteObjectIdentifier;
    }

    public void setRemoteObjectIdentifier(final String remoteObjectIdentifier) {
        this.remoteObjectIdentifier = remoteObjectIdentifier;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(final String noteText) {
        this.noteText = noteText;
    }

}
