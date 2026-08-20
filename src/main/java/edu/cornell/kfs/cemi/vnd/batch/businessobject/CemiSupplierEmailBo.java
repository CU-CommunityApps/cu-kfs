package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierEmailBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String emailId;
    private String emailAddress;
    private String emailPrimary;
    private String emailUseFor1;
    private String emailUseFor2;
    private String emailUseFor3;
    private String emailUseFor4;
    private String useForTenanted1;
    private String useForTenanted2;
    private String useForTenanted3;
    private String useForTenanted4;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailPrimary() {
        return emailPrimary;
    }

    public void setEmailPrimary(final String emailPrimary) {
        this.emailPrimary = emailPrimary;
    }

    public String getEmailUseFor1() {
        return emailUseFor1;
    }

    public void setEmailUseFor1(final String emailUseFor1) {
        this.emailUseFor1 = emailUseFor1;
    }

    public String getEmailUseFor2() {
        return emailUseFor2;
    }

    public void setEmailUseFor2(final String emailUseFor2) {
        this.emailUseFor2 = emailUseFor2;
    }

    public String getEmailUseFor3() {
        return emailUseFor3;
    }

    public void setEmailUseFor3(final String emailUseFor3) {
        this.emailUseFor3 = emailUseFor3;
    }

    public String getEmailUseFor4() {
        return emailUseFor4;
    }

    public void setEmailUseFor4(final String emailUseFor4) {
        this.emailUseFor4 = emailUseFor4;
    }

    public String getUseForTenanted1() {
        return useForTenanted1;
    }

    public void setUseForTenanted1(final String useForTenanted1) {
        this.useForTenanted1 = useForTenanted1;
    }

    public String getUseForTenanted2() {
        return useForTenanted2;
    }

    public void setUseForTenanted2(final String useForTenanted2) {
        this.useForTenanted2 = useForTenanted2;
    }

    public String getUseForTenanted3() {
        return useForTenanted3;
    }

    public void setUseForTenanted3(final String useForTenanted3) {
        this.useForTenanted3 = useForTenanted3;
    }

    public String getUseForTenanted4() {
        return useForTenanted4;
    }

    public void setUseForTenanted4(final String useForTenanted4) {
        this.useForTenanted4 = useForTenanted4;
    }

}
