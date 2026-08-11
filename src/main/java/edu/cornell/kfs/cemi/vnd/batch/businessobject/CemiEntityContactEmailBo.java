package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiEntityContactEmailBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private Integer vendorContactGeneratedIdentifier;

    private String emailRowId;
    private String deleteEmail;
    private String doNotReplaceAllEmail;
    private String emailAddress;
    private String emailComment;
    private String existingEmailId;
    private String newEmailId;

    private List<CemiEntityContactGenericUsageBo> emailUsages;

    public Integer getVendorContactGeneratedIdentifier() {
        return vendorContactGeneratedIdentifier;
    }

    public void setVendorContactGeneratedIdentifier(final Integer vendorContactGeneratedIdentifier) {
        this.vendorContactGeneratedIdentifier = vendorContactGeneratedIdentifier;
    }

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

    public List<CemiEntityContactGenericUsageBo> getEmailUsages() {
        if (emailUsages == null) {
            emailUsages = new ArrayList<>();
        }
        return emailUsages;
    }

    public void setEmailUsages(final List<CemiEntityContactGenericUsageBo> emailUsages) {
        this.emailUsages = emailUsages;
    }

    public void addEmailUsage(final CemiEntityContactGenericUsageBo emailUsage) {
        getEmailUsages().add(emailUsage);
    }

}
