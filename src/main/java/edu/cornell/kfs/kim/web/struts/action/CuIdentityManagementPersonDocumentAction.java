package edu.cornell.kfs.kim.web.struts.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.web.struts.action.IdentityManagementPersonDocumentAction;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kim.api.identity.CuPersonService;
import edu.cornell.kfs.kim.bo.ui.PersonDocumentAffiliation;
import edu.cornell.kfs.kim.rule.event.ui.AddAffiliationEvent;
import edu.cornell.kfs.kim.web.struts.form.CuIdentityManagementPersonDocumentForm;

public class CuIdentityManagementPersonDocumentAction extends IdentityManagementPersonDocumentAction {

    private CuPersonService cuPersonService;

    public ActionForward addAffiliation(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final CuIdentityManagementPersonDocumentForm docForm = (CuIdentityManagementPersonDocumentForm) form;
        final IdentityManagementPersonDocument document = (IdentityManagementPersonDocument) docForm.getDocument();
        final PersonDocumentAffiliation newAffiliation = docForm.getNewAffiliation();
        if (getKualiRuleService().applyRules(new AddAffiliationEvent("", document, newAffiliation))) {
            List<PersonDocumentAffiliation> affiliations = document.getPersonDocumentExtension().getAffiliations();
            newAffiliation.setDocumentNumber(document.getDocumentNumber());
            affiliations.add(newAffiliation);
            docForm.setNewAffiliation(new PersonDocumentAffiliation());
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    private CuPersonService getCuPersonService() {
        if (cuPersonService == null) {
            cuPersonService = (CuPersonService) KimApiServiceLocator.getPersonService();
        }
        return cuPersonService;
    }

}
