package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class NoteExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
    private static final long serialVersionUID = 3659103869891847806L;

    private Long noteIdentifier;
    private boolean copyNoteIndicator;

    public Long getNoteIdentifier() {
        return noteIdentifier;
    }

    public void setNoteIdentifier(Long noteIdentifier) {
        this.noteIdentifier = noteIdentifier;
    }

    public boolean isCopyNoteIndicator() {
        return copyNoteIndicator;
    }

    public void setCopyNoteIndicator(boolean copyNoteIndicator) {
        this.copyNoteIndicator = copyNoteIndicator;
    }

}
