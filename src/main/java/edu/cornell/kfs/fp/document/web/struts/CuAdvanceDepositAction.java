package edu.cornell.kfs.fp.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.web.struts.AdvanceDepositAction;
import org.kuali.kfs.fp.document.web.struts.AdvanceDepositForm;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.util.ConfidentialAttachmentUtil;

public class CuAdvanceDepositAction extends AdvanceDepositAction {

    /**
     * Overridden to treat "Confidential" add-attachment authorization failures as validation errors, rather than throwing an authorization exception.
     * 
     * @see org.kuali.kfs.kns.web.struts.action.KualiDocumentActionBase#insertBONote(
     * org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("deprecation")
    @Override
    public ActionForward insertBONote(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AdvanceDepositForm adForm = (AdvanceDepositForm) form;
        final Note newNote = adForm.getNewNote();
        final Document document = adForm.getDocument();
        
        if (!ConfidentialAttachmentUtil.attachmentIsNonConfidentialOrCanAddConfAttachment(
                newNote, document, adForm.getAttachmentFile(), getDocumentHelperService().getDocumentAuthorizer(document))) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        return super.insertBONote(mapping, form, request, response);
    }

}
