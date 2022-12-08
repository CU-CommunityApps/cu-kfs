package edu.cornell.kfs.coa.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.form.KualiMaintenanceForm;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.coa.businessobject.CuObjectCodeActivationGlobal;
import edu.cornell.kfs.sys.document.web.struts.CuFinancialMaintenanceDocumentAction;

@SuppressWarnings("deprecation")
public class CuObjectCodeActivationGlobalMaintenanceDocumentAction extends CuFinancialMaintenanceDocumentAction {
	private static final Logger LOG = LogManager.getLogger(CuObjectCodeActivationGlobalMaintenanceDocumentAction.class);
    
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("copy, entering");
        KualiMaintenanceForm oldForm = (KualiMaintenanceForm) form;
        FinancialSystemMaintenanceDocument oldDoc = (FinancialSystemMaintenanceDocument) oldForm.getDocument();
        CuObjectCodeActivationGlobal oldGlobal = (CuObjectCodeActivationGlobal) oldDoc.getNewMaintainableObject().getBusinessObject();
        
        oldForm.setDocument(null);
        ActionForward newForward = super.start(mapping, form, request, response);
        
        KualiMaintenanceForm newForm = (KualiMaintenanceForm) form;
        FinancialSystemMaintenanceDocument newDoc = (FinancialSystemMaintenanceDocument) newForm.getDocument();
        CuObjectCodeActivationGlobal newGlobal = (CuObjectCodeActivationGlobal) newDoc.getNewMaintainableObject().getBusinessObject();
        
        newGlobal.copyDetailsFromOtherCuObjectCodeActivationGlobal(oldGlobal);
        addCopyNote(oldDoc, newForm, newDoc);
        return newForward;
    }

    protected void addCopyNote(FinancialSystemMaintenanceDocument oldDoc, KualiMaintenanceForm newForm,
            FinancialSystemMaintenanceDocument newDoc) {
        Note newNote = newForm.getNewNote();
        newNote.setNotePostedTimestampToCurrent();
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        newNote.setNoteText("List of object codes copied from document number " + oldDoc.getDocumentNumber());
        if (kualiUser == null) {
            throw new IllegalStateException("Current UserSession has a null Person.");
        }
        Note tmpNote = getNoteService().createNote(newNote, newDoc.getNoteTarget(), kualiUser.getPrincipalId());
        newDoc.addNote(tmpNote);
        newForm.setNewNote(new Note());
    }
    
}
