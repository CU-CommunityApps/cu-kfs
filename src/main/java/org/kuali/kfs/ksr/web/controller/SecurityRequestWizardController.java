package org.kuali.kfs.ksr.web.controller;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.web.form.SecurityRequestWizardForm;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * ====
 * CU Customization:
 * Replaced KSR Struts "wizard" action with this Spring MVC "wizard" controller.
 * ====
 */
@Controller
@RequestMapping(value = KsrConstants.SECURITY_REQUEST_WIZARD_KRAD_URL)
public class SecurityRequestWizardController extends UifControllerBase {

	@Override
	protected SecurityRequestWizardForm createInitialForm() {
		return new SecurityRequestWizardForm();
	}

	@RequestMapping(params = "methodToCall=processWizard")
    public ModelAndView processWizard(@ModelAttribute("KualiForm") SecurityRequestWizardForm wizardForm, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
		
		String baseUrl = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.KRAD_URL_KEY) + KsrConstants.SECURITY_REQUEST_DOC_KRAD_URL;
		
		Properties parameters = new Properties();
		parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.DOC_HANDLER_METHOD);
		parameters.put(KewApiConstants.COMMAND_PARAMETER, KewApiConstants.INITIATE_COMMAND);
		parameters.put(KewApiConstants.DOCTYPE_PARAMETER, KsrConstants.SECURITY_REQUEST_DOC_TYPE_NAME);
		parameters.put(KsrConstants.SECURITY_GROUP_ID, wizardForm.getSecurityGroupId());
		
		return performRedirect(wizardForm, baseUrl, parameters);
	}
}
