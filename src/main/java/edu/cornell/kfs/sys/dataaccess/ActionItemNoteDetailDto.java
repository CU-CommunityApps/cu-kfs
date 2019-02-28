package edu.cornell.kfs.sys.dataaccess;

import java.io.Serializable;
import java.sql.Timestamp;

public class ActionItemNoteDetailDto implements Serializable {
    private static final long serialVersionUID = 9115076028165986273L;
    private String principleId;
    private String docHeaderId;
    private String actionNote;
    private Timestamp noteTimeStamp;
    private String orginalActionItemId;
    
    public ActionItemNoteDetailDto() {
        
    }
    
    public ActionItemNoteDetailDto(String principleId, String docHeaderId, String actionNote, String orginalActionItemId, Timestamp noteTimeStamp) {
        this.principleId = principleId;
        this.docHeaderId = docHeaderId;
        this.actionNote = actionNote;
        this.orginalActionItemId = orginalActionItemId;
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

    public String getOrginalActionItemId() {
        return orginalActionItemId;
    }

    public void setOrginalActionItemId(String orginalActionItemId) {
        this.orginalActionItemId = orginalActionItemId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("principleId: '").append(principleId);
        sb.append("' docHeaderId: '").append(docHeaderId);
        sb.append("' noteTimeStamp: '").append(noteTimeStamp);
        sb.append("' actionNote: '").append(actionNote);
        sb.append("' original action item id: '").append(orginalActionItemId).append("'");
        return sb.toString();
    }

}
