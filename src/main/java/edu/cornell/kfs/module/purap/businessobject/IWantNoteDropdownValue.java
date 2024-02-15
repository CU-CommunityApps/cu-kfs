package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class IWantNoteDropdownValue extends PersistableBusinessObjectBase implements Comparable<IWantNoteDropdownValue>{
    private Integer id;
    private String text;
    private boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(IWantNoteDropdownValue iWantNote) {
        return this.getText().compareTo(iWantNote.getText());
    }
}
