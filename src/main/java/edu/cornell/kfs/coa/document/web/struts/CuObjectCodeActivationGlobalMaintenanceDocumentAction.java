package edu.cornell.kfs.coa.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.form.KualiMaintenanceForm;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.coa.businessobject.CuObjectCodeActivationGlobal;
import edu.cornell.kfs.sys.document.web.struts.CuFinancialMaintenanceDocumentAction;

public class CuObjectCodeActivationGlobalMaintenanceDocumentAction extends CuFinancialMaintenanceDocumentAction {
    protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuObjectCodeActivationGlobalMaintenanceDocumentAction.class);
    
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("copy, entering");
        if (isObjectCodeActivationGlobalMaintenaceDocument(form)) {
            return processObjectCodeActivationGlobalCopy(mapping, form, request, response);
        } else {
            return super.copy(mapping, form, request, response);
        }
    }
    
    protected boolean isObjectCodeActivationGlobalMaintenaceDocument(ActionForm form) {
        try {
            KualiMaintenanceForm kmForm = (KualiMaintenanceForm) form;
            return StringUtils.equalsIgnoreCase("OCAG", kmForm.getDocTypeName());
        } catch (Exception e) {
            LOG.error("isObjectCodeActivationGlobalMaintenaceDocument, had an error.", e);
            return false;
        }
    }

    protected ActionForward processObjectCodeActivationGlobalCopy(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiMaintenanceForm oldForm = (KualiMaintenanceForm) form;
        FinancialSystemMaintenanceDocument oldDoc = (FinancialSystemMaintenanceDocument) oldForm.getDocument();
        CuObjectCodeActivationGlobal oldGlobal = (CuObjectCodeActivationGlobal) oldDoc.getNewMaintainableObject().getBusinessObject();
        
        oldForm.setDocument(null);
        ActionForward newForward = super.start(mapping, form, request, response);
        
        KualiMaintenanceForm newForm = (KualiMaintenanceForm) form;
        FinancialSystemMaintenanceDocument newDoc = (FinancialSystemMaintenanceDocument) newForm.getDocument();
        CuObjectCodeActivationGlobal newGlobal = (CuObjectCodeActivationGlobal) newDoc.getNewMaintainableObject().getBusinessObject();
        
        newGlobal.copyDetailsFromOtherCuObjectCodeActivationGlobal(oldGlobal);
        
        Note newNote = newForm.getNewNote();
        newNote.setNotePostedTimestampToCurrent();
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        newNote.setNoteText("List of object codes copied from document number " + oldDoc.getDocumentNumber());
        if (kualiUser == null) {
            throw new IllegalStateException("Current UserSession has a null Person.");
        }
        getNoteService().createNote(newNote, newDoc.getNoteTarget(), kualiUser.getPrincipalId());
        return newForward;
    }
}
