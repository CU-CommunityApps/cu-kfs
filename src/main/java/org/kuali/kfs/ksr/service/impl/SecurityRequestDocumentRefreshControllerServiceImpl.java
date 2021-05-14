package org.kuali.kfs.ksr.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.ksr.document.SecurityRequestDocument;
import org.kuali.kfs.ksr.service.SecurityRequestDocumentService;
import org.kuali.kfs.ksr.web.form.SecurityRequestDocumentForm;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADPropertyConstants;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.rice.krad.web.service.impl.RefreshControllerServiceImpl;
import org.springframework.web.servlet.ModelAndView;

/**
 * SecurityRequestDocument-specific RefreshControllerService implementation that,
 * when updating the principal on the document, will trigger a refresh
 * of the document's request role data to match that of the new principal.
 * 
 * The custom refresh operation is based upon that from the archaic SecurityRequestDocumentAction KNS class.
 */
public class SecurityRequestDocumentRefreshControllerServiceImpl extends RefreshControllerServiceImpl {

    protected SecurityRequestDocumentService securityRequestDocumentService;

    /**
     * Overridden to also force a refresh of the request role data
     * when returning a different principal from the Person lookup screen.
     * 
     * @see org.kuali.rice.krad.web.service.impl.RefreshControllerServiceImpl#refresh(org.kuali.rice.krad.web.form.UifFormBase)
     */
    @Override
    public ModelAndView refresh(UifFormBase form) {
        ModelAndView modelAndView = super.refresh(form);

        SecurityRequestDocumentForm documentForm = (SecurityRequestDocumentForm) form;
        SecurityRequestDocument document = (SecurityRequestDocument) documentForm.getDocument();

        if (!form.isUpdateComponentRequest() && !form.isUpdateDialogRequest() && !form.isUpdateNoneRequest()) {
            HttpServletRequest request = documentForm.getRequest();
            final String PRINCIPAL_ID_PARAMETER = KRADPropertyConstants.DOCUMENT + "." + KIMPropertyConstants.Person.PRINCIPAL_ID;
            String principalId = request.getParameter(PRINCIPAL_ID_PARAMETER);

            if (StringUtils.isNotBlank(principalId) && !StringUtils.equals(principalId, documentForm.getCurrentPrincipalId())) {
                refreshSecurityRequestForPrincipalChange(documentForm, document, principalId);
            }
        }
        
        return modelAndView;
    }

    protected void refreshSecurityRequestForPrincipalChange(SecurityRequestDocumentForm documentForm,
            SecurityRequestDocument document, String principalId) {
        document.setPrincipalId(principalId);
        securityRequestDocumentService.initiateSecurityRequestDocument(document, GlobalVariables.getUserSession().getPerson());
        documentForm.setCurrentPrincipalId(document.getPrincipalId());
    }

    public void setSecurityRequestDocumentService(SecurityRequestDocumentService securityRequestDocumentService) {
        this.securityRequestDocumentService = securityRequestDocumentService;
    }

}
