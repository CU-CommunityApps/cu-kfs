package edu.cornell.kfs.ksr.web.struts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;

import edu.cornell.kfs.ksr.KSRConstants;
import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.service.SecurityRequestDocumentService;

public class SecurityRequestWizardAction extends FinancialSystemTransactionalDocumentActionBase {

    public ActionForward wizard(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        List<SecurityGroup> securityGroups = SpringContext.getBean(SecurityRequestDocumentService.class).getActiveSecurityGroups();
        SecurityRequestWizardForm securityRequestWizardForm = (SecurityRequestWizardForm) form;
        securityRequestWizardForm.setSecurityGroups(securityGroups);
        if (securityGroups.size() != 0) {
            securityRequestWizardForm.setSecurityGroup(securityGroups.get(0));
        }
        return mapping.findForward(KSRConstants.SECURITY_REQUEST_WIZARD);
    }

    public ActionForward processWizard(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        SecurityRequestWizardForm securityRequestWizardForm = (SecurityRequestWizardForm) form;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.DOC_HANDLER_METHOD);
        parameters.put(KFSConstants.PARAMETER_COMMAND, KFSConstants.INITIATE_METHOD);
        parameters.put(KewApiConstants.DOCTYPE_PARAMETER, KSRConstants.SECURITY_REQUEST_DOC_TYPE_NAME);
        parameters.put(KSRPropertyConstants.SECURITY_GROUP_ID, securityRequestWizardForm.getSecurityGroup().getSecurityGroupId().toString());

        String applicationUrl = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY);
        String securityRequestUrl = applicationUrl + KSRConstants.KSR_PATH + KSRConstants.SECURITY_REQUEST_DOC_URL;
        return new ActionForward(UrlFactory.parameterizeUrl(securityRequestUrl, parameters), true);
    }

}
