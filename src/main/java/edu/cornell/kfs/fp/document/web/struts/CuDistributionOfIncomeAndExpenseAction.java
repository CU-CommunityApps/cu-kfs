package edu.cornell.kfs.fp.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.web.struts.DistributionOfIncomeAndExpenseAction;
import org.kuali.kfs.fp.document.web.struts.DistributionOfIncomeAndExpenseForm;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.util.ConfidentialAttachmentUtil;

public class CuDistributionOfIncomeAndExpenseAction extends DistributionOfIncomeAndExpenseAction {
    
    /**
     * Overridden to treat "Confidential" add-attachment authorization failures as validation errors, rather than throwing an authorization exception.
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#insertBONote()
     */
    @SuppressWarnings("deprecation")
    @Override
    public ActionForward insertBONote(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DistributionOfIncomeAndExpenseForm diForm = (DistributionOfIncomeAndExpenseForm) form;
        final Note newNote = diForm.getNewNote();
        
         if (!ConfidentialAttachmentUtil.attachmentIsNonConfidentialOrCanAddConfAttachment(newNote, diForm.getDocument(), diForm.getAttachmentFile(),
                getDocumentHelperService().getDocumentAuthorizer(diForm.getDocument()))) {
             return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        return super.insertBONote(mapping, form, request, response);
    }

}
