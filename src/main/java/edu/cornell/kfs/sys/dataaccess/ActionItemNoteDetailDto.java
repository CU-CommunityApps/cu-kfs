package edu.cornell.kfs.sys.dataaccess;

import java.io.Serializable;
import java.sql.Timestamp;

public class ActionItemNoteDetailDto implements Serializable {
    private static final long serialVersionUID = 9115076028165986273L;
    private String principalId;
    private String docHeaderId;
    private String actionNote;
    private Timestamp noteTimeStamp;
    private String originalActionItemId;
    
    public ActionItemNoteDetailDto() {
        
    }
    
    public ActionItemNoteDetailDto(String principalId, String docHeaderId, String actionNote, String originalActionItemId, Timestamp noteTimeStamp) {
        this.principalId = principalId;
        this.docHeaderId = docHeaderId;
        this.actionNote = actionNote;
        this.originalActionItemId = originalActionItemId;
        this.noteTimeStamp = noteTimeStamp;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getDocHeaderId() {
        return docHeaderId;
    }

    public void setDocHeaderId(String docHeaderId) {
        this.docHeaderId = docHeaderId;
    }

    public String getActionNote() {
        return actionNote;
    }

    public void setActionNote(String actionNote) {
        this.actionNote = actionNote;
    }

    public Timestamp getNoteTimeStamp() {
        return noteTimeStamp;
    }

    public void setNoteTimeStamp(Timestamp noteTimeStamp) {
        this.noteTimeStamp = noteTimeStamp;
    }

    public String getOriginalActionItemId() {
        return originalActionItemId;
    }

    public void setOriginalActionItemId(String originalActionItemId) {
        this.originalActionItemId = originalActionItemId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(principalId: '").append(principalId);
        sb.append("' docHeaderId: '").append(docHeaderId);
        sb.append("' noteTimeStamp: '").append(noteTimeStamp);
        sb.append("' actionNote: '").append(actionNote);
        sb.append("' original action item id: '").append(originalActionItemId).append("')");
        return sb.toString();
    }

}
