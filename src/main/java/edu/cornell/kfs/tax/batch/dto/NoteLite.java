package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class NoteLite {

    private Long noteIdentifier;
    private String remoteObjectIdentifier;
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



    public enum NoteField implements TaxDtoFieldEnum {
        noteIdentifier,
        remoteObjectIdentifier,
        noteText;

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return Note.class;
        }

    }

}
