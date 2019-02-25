package edu.cornell.kfs.sys.dataaccess;

public class ActionItemNoteDetailDto {
    private String principleId;
    private String docHeaderId;
    private String actionNote;
    
    public ActionItemNoteDetailDto() {
        
    }
    
    public ActionItemNoteDetailDto(String principleId, String docHeaderId, String actionNote) {
        this.principleId = principleId;
        this.docHeaderId = docHeaderId;
        this.actionNote = actionNote;
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("principleId: '").append(principleId);
        sb.append("' docHeaderId: '").append(docHeaderId);
        sb.append("' actionNote: '").append(actionNote).append("'");
        return sb.toString();
    }

}
