package org.kuali.kfs.ksr.web.controller;

import org.kuali.kfs.ksr.KsrConstants;
import org.kuali.kfs.ksr.web.form.SecurityRequestDocumentForm;
import org.kuali.rice.krad.document.TransactionalDocumentControllerBase;
import org.kuali.rice.krad.web.service.ControllerService;
import org.kuali.rice.krad.web.service.RefreshControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ====
 * CU Customization:
 * Replaced KSR Struts security request doc action with this Spring MVC security request doc controller.
 * 
 * Most of the custom functionality has been moved into new controller service implementations.
 * ====
 */
@Controller
@RequestMapping(value = KsrConstants.SECURITY_REQUEST_DOC_KRAD_URL)
public class SecurityRequestDocumentController extends TransactionalDocumentControllerBase {

    @Override
    protected SecurityRequestDocumentForm createInitialForm() {
        return new SecurityRequestDocumentForm();
    }

    /*
     * Rice already auto-wires this service, so perform an auto-wire override on the setter
     * to switch to a different service implementation (like what other KRAD controllers do).
     */
    @Override
    @Autowired
    @Qualifier("securityRequestDocumentControllerService")
    public void setControllerService(ControllerService controllerService) {
        super.setControllerService(controllerService);
    }

    /*
     * Rice already auto-wires this service, so perform an auto-wire override on the setter
     * to switch to a different service implementation (like what other KRAD controllers do).
     */
    @Override
    @Autowired
    @Qualifier("securityRequestDocumentRefreshControllerService")
    public void setRefreshControllerService(RefreshControllerService refreshControllerService) {
        super.setRefreshControllerService(refreshControllerService);
    }

}
