package edu.cornell.kfs.tax.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class NoteLite extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

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

}
