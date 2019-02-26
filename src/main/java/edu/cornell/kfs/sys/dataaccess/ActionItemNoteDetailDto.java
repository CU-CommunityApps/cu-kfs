package edu.cornell.kfs.sys.dataaccess;

import java.sql.Timestamp;

public class ActionItemNoteDetailDto {
    private String principleId;
    private String docHeaderId;
    private String actionNote;
    private Timestamp noteTimeStamp;
    
    public ActionItemNoteDetailDto() {
        
    }
    
    public ActionItemNoteDetailDto(String principleId, String docHeaderId, String actionNote, Timestamp noteTimeStamp) {
        this.principleId = principleId;
        this.docHeaderId = docHeaderId;
        this.actionNote = actionNote;
        this.noteTimeStamp = noteTimeStamp;
    }

    public String getPrincipleId() {
        return principleId;
    }

    public void setPrincipleId(String principleId) {
        this.principleId = principleId;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("principleId: '").append(principleId);
        sb.append("' docHeaderId: '").append(docHeaderId);
        sb.append("' noteTimeStamp: '").append(noteTimeStamp).append("'");
        sb.append("' actionNote: '").append(actionNote).append("'");
        return sb.toString();
    }

}
