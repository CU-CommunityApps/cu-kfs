package org.kuali.kfs.ksr.web.form;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.rice.krad.web.bind.RequestAccessible;

import edu.cornell.cynergy.krad.web.form.KnsLookupCompatibleTransactionalDocumentFormBase;

/**
 * ====
 * CU Customization:
 * Replaced the KSR Struts doc form with a KRAD doc form.
 * ====
 */
public class SecurityRequestDocumentForm extends KnsLookupCompatibleTransactionalDocumentFormBase {

    private static final long serialVersionUID = 356248034395900432L;

    @RequestAccessible
    private Long securityGroupId;

    private String currentPrincipalId;

    protected String getDefaultDocumentTypeName() {
        return KsrConstants.SECURITY_REQUEST_DOC_TYPE_NAME;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getCurrentPrincipalId() {
        return currentPrincipalId;
    }

    public void setCurrentPrincipalId(String currentPrincipalId) {
        this.currentPrincipalId = currentPrincipalId;
    }

}
