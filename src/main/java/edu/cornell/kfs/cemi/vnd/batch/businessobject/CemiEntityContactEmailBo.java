package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiEntityContactEmailBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = -3590276052795717039L;

    private String emailRowId;
    private String deleteEmail;
    private String doNotReplaceAllEmail;
    private String emailAddress;
    private String emailComment;
    private String emailUsageRowId;
    private String emailUsagePublic;
    private String emailUsageTypeRowId;
    private String emailUsageTypePrimary;
    private String emailUsageType;
    private String emailUseFor;
    private String emailUseForTenanted;
    private String emailUsageComments;
    private String existingEmailId;
    private String newEmailId;

    public String getEmailRowId() {
        return emailRowId;
    }

    public void setEmailRowId(final String emailRowId) {
        this.emailRowId = emailRowId;
    }

    public String getDeleteEmail() {
        return deleteEmail;
    }

    public void setDeleteEmail(final String deleteEmail) {
        this.deleteEmail = deleteEmail;
    }

    public String getDoNotReplaceAllEmail() {
        return doNotReplaceAllEmail;
    }

    public void setDoNotReplaceAllEmail(final String doNotReplaceAllEmail) {
        this.doNotReplaceAllEmail = doNotReplaceAllEmail;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailComment() {
        return emailComment;
    }

    public void setEmailComment(final String emailComment) {
        this.emailComment = emailComment;
    }

    public String getEmailUsageRowId() {
        return emailUsageRowId;
    }

    public void setEmailUsageRowId(final String emailUsageRowId) {
        this.emailUsageRowId = emailUsageRowId;
    }

    public String getEmailUsagePublic() {
        return emailUsagePublic;
    }

    public void setEmailUsagePublic(final String emailUsagePublic) {
        this.emailUsagePublic = emailUsagePublic;
    }

    public String getEmailUsageTypeRowId() {
        return emailUsageTypeRowId;
    }

    public void setEmailUsageTypeRowId(final String emailUsageTypeRowId) {
        this.emailUsageTypeRowId = emailUsageTypeRowId;
    }

    public String getEmailUsageTypePrimary() {
        return emailUsageTypePrimary;
    }

    public void setEmailUsageTypePrimary(final String emailUsageTypePrimary) {
        this.emailUsageTypePrimary = emailUsageTypePrimary;
    }

    public String getEmailUsageType() {
        return emailUsageType;
    }

    public void setEmailUsageType(final String emailUsageType) {
        this.emailUsageType = emailUsageType;
    }

    public String getEmailUseFor() {
        return emailUseFor;
    }

    public void setEmailUseFor(final String emailUseFor) {
        this.emailUseFor = emailUseFor;
    }

    public String getEmailUseForTenanted() {
        return emailUseForTenanted;
    }

    public void setEmailUseForTenanted(final String emailUseForTenanted) {
        this.emailUseForTenanted = emailUseForTenanted;
    }

    public String getEmailUsageComments() {
        return emailUsageComments;
    }

    public void setEmailUsageComments(final String emailUsageComments) {
        this.emailUsageComments = emailUsageComments;
    }

    public String getExistingEmailId() {
        return existingEmailId;
    }

    public void setExistingEmailId(final String existingEmailId) {
        this.existingEmailId = existingEmailId;
    }

    public String getNewEmailId() {
        return newEmailId;
    }

    public void setNewEmailId(final String newEmailId) {
        this.newEmailId = newEmailId;
    }

}
