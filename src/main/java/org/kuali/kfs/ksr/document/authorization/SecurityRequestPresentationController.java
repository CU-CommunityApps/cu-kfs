package org.kuali.kfs.ksr.document.authorization;

import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.TransactionalDocumentPresentationControllerBase;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Presentation controller implementation for the <code>SecurityRequestDocument</code>
 * 
 * @author rSmart Development Team
 */
public class SecurityRequestPresentationController extends TransactionalDocumentPresentationControllerBase {

	private static final long serialVersionUID = 646211390547700434L;

	/**
     * Override to only allow edit when the document in initiated or saved
     * 
     * @see org.kuali.rice.krad.document.DocumentPresentationControllerBase#canEdit(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canEdit(Document document) {
        boolean canEdit = false;
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            canEdit = true;
        }

        return canEdit;
    }

    /**
     * ====
     * CU Customization:
     * Override to allow reloads at any doc status except INITIATED.
     * ====
     * 
     * @see org.kuali.rice.krad.document.DocumentPresentationControllerBase#canReload(org.kuali.rice.krad.document.Document)
     */
    @Override
	public boolean canReload(Document document) {
		return !document.getDocumentHeader().getWorkflowDocument().isInitiated();
	}

}
