package edu.cornell.kfs.concur;

import org.kuali.kfs.sys.KFSConstants;

public final class ConcurTestWorkflowInfo {

    public static final ConcurTestWorkflowInfo EMPTY = new ConcurTestWorkflowInfo(
            KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, 0);

    private final String actionTaken;
    private final String comment;
    private final int versionNumber;

    public ConcurTestWorkflowInfo(String actionTaken, String comment, int versionNumber) {
        this.actionTaken = actionTaken;
        this.comment = comment;
        this.versionNumber = versionNumber;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public String getComment() {
        return comment;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

}
