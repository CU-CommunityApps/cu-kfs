package org.kuali.kfs.ksr.document.authorization;

import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.DocumentRequestAuthorizationCache;
import org.kuali.rice.krad.maintenance.MaintenanceDocumentPresentationControllerBase;

/**
 * ====
 * CU Customization:
 * Added custom maintenance presentation controller
 * that can allow the document "close" button.
 * ====
 */
public class KSRMaintenanceDocumentPresentationController extends MaintenanceDocumentPresentationControllerBase {

    private static final long serialVersionUID = 6153420020014740068L;

    /**
     * Overridden to return false if the document is in INITIATED or SAVED status, or true otherwise,
     * due to the inherited method always returning false.
     * 
     * @see org.kuali.rice.krad.document.DocumentPresentationControllerBase#canClose(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canClose(Document document) {
        DocumentRequestAuthorizationCache.WorkflowDocumentInfo workflowDocumentInfo =
                getDocumentRequestAuthorizationCache(document).getWorkflowDocumentInfo();
        
        return !workflowDocumentInfo.isInitiated() && !workflowDocumentInfo.isSaved();
    }

}
