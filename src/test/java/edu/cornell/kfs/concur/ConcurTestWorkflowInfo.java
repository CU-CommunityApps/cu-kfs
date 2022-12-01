package edu.cornell.kfs.concur;

import org.kuali.kfs.sys.KFSConstants;

public final class ConcurTestWorkflowInfo {

    public static final ConcurTestWorkflowInfo EMPTY = new ConcurTestWorkflowInfo(
            KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING);

    private final String actionTaken;
    private final String comment;

    public ConcurTestWorkflowInfo(String actionTaken, String comment) {
        this.actionTaken = actionTaken;
        this.comment = comment;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public String getComment() {
        return comment;
    }

}
