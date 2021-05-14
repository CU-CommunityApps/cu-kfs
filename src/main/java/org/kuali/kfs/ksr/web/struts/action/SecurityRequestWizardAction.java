package org.kuali.kfs.ksr.web.struts.action;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.kfs.ksr.web.struts.form.SecurityRequestWizardForm;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kns.web.struts.action.KualiTransactionalDocumentActionBase;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * Also deprecated this class; use SecurityRequestWizardController instead.
 * ====
 * 
 * Action class for the wizard.  It handles coming to the wizard and submitting the wizard to get o the document.
 * 
 * @deprecated
 * @author rSmart Development Team
 */
@Deprecated
public class SecurityRequestWizardAction extends KualiTransactionalDocumentActionBase {

    /**
     * Starting point to get to the wizard.  gets all active SecurityGroups and forward to the wizard's page
     */
    public ActionForward wizard(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	List<SecurityGroup> securityGroups = KSRServiceLocator.getSecurityRequestDocumentService().getActiveSecurityGroups();
    	SecurityRequestWizardForm securityRequestWizardForm = (SecurityRequestWizardForm)form;
    	securityRequestWizardForm.setSecurityGroups(securityGroups);
    	if (securityGroups.size() != 0){
    		securityRequestWizardForm.setSecurityGroup(securityGroups.get(0));
    	}
    	return mapping.findForward(KsrConstants.SECURITY_REQUEST_WIZARD);
    }

	
	/**
	 * Takes the results from the wizard's page and moves to the SecurityRequestDocument
	 */
	public ActionForward processWizard(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		SecurityRequestWizardForm securityRequestWizardForm = (SecurityRequestWizardForm) form;
	
		//?methodToCall=docHandler&command=initiate&docTypeName=SecurityRequestDocument&securityGroupId=1
		Properties parameters = new Properties();
		parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.DOC_HANDLER_METHOD);
		parameters.put("command", "initiate");
		parameters.put(KewApiConstants.DOCTYPE_PARAMETER, KsrConstants.SECURITY_REQUEST_DOC_TYPE_NAME);
		parameters.put(KsrConstants.SECURITY_GROUP_ID, securityRequestWizardForm.getSecurityGroup().getSecurityGroupId().toString());
		
		// TODO: don't check in, compile error
		String baseLookupUrl = getApplicationBaseUrl() + "/ksr/" + KsrConstants.SECURITY_REQUEST_DOC_URL;
		String documentUrl = UrlFactory.parameterizeUrl(baseLookupUrl, parameters);
        return new ActionForward(documentUrl, true);
	}

}
