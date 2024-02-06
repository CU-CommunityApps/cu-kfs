package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class IWantNoteDropdownValue extends PersistableBusinessObjectBase implements Comparable<IWantNoteDropdownValue>{
    private Integer noteIdentifier;
    private String noteText;
    private boolean active;

    public Integer getNoteIdentifier() {
        return noteIdentifier;
    }

    public void setNoteIdentifier(Integer noteIdentifier) {
        this.noteIdentifier = noteIdentifier;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(IWantNoteDropdownValue iWantNote) {
        return this.getNoteText().compareTo(iWantNote.getNoteText());
    }
}
