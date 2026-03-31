package edu.cornell.kfs.coa.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class RemappedAccountAttachment extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String legacyAccountCode;
    private String mismappedKfsChart;
    private String mismappedKfsAccount;
    private String correctKfsChart;
    private String correctKfsAccount;
    private String mismappedAccountObjectId;
    private String correctAccountObjectId;
    private Long noteIdentifier;
    private String noteText;
    private String attachmentFileName;

    public String getLegacyAccountCode() {
        return legacyAccountCode;
    }

    public void setLegacyAccountCode(final String legacyAccountCode) {
        this.legacyAccountCode = legacyAccountCode;
    }

    public String getMismappedKfsChart() {
        return mismappedKfsChart;
    }

    public void setMismappedKfsChart(final String mismappedKfsChart) {
        this.mismappedKfsChart = mismappedKfsChart;
    }

    public String getMismappedKfsAccount() {
        return mismappedKfsAccount;
    }

    public void setMismappedKfsAccount(final String mismappedKfsAccount) {
        this.mismappedKfsAccount = mismappedKfsAccount;
    }

    public String getCorrectKfsChart() {
        return correctKfsChart;
    }

    public void setCorrectKfsChart(final String correctKfsChart) {
        this.correctKfsChart = correctKfsChart;
    }

    public String getCorrectKfsAccount() {
        return correctKfsAccount;
    }

    public void setCorrectKfsAccount(final String correctKfsAccount) {
        this.correctKfsAccount = correctKfsAccount;
    }

    public String getMismappedAccountObjectId() {
        return mismappedAccountObjectId;
    }

    public void setMismappedAccountObjectId(final String mismappedAccountObjectId) {
        this.mismappedAccountObjectId = mismappedAccountObjectId;
    }

    public String getCorrectAccountObjectId() {
        return correctAccountObjectId;
    }

    public void setCorrectAccountObjectId(final String correctAccountObjectId) {
        this.correctAccountObjectId = correctAccountObjectId;
    }

    public Long getNoteIdentifier() {
        return noteIdentifier;
    }

    public void setNoteIdentifier(final Long noteIdentifier) {
        this.noteIdentifier = noteIdentifier;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(final String noteText) {
        this.noteText = noteText;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(final String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

}
