package org.kuali.kfs.ksr.web.form;

import org.kuali.rice.krad.web.bind.RequestAccessible;
import org.kuali.rice.krad.web.form.UifFormBase;

/**
 * ====
 * CU Customization:
 * Replaced the KSR Struts "wizard" form with this KRAD UIF "wizard" form.
 * ====
 */
public class SecurityRequestWizardForm extends UifFormBase {

    private static final long serialVersionUID = -9104876006523111213L;

    @RequestAccessible
    private String securityGroupId;

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

}
