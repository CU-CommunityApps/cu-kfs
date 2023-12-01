package edu.cornell.kfs.kew.impl.document;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.kew.impl.document.DocumentRefreshQueueImpl;
import org.kuali.kfs.ksb.api.KsbApiServiceLocator;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.kew.api.document.CuDocumentRefreshQueue;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

public class CuDocumentRefreshQueueImpl extends DocumentRefreshQueueImpl implements CuDocumentRefreshQueue {

    private static final String DOCUMENT_MAINTENANCE_SERVICE = "documentMaintenanceService";

    /*
     * NOTE: If the superclass's two-arg refreshDocument() method ever gets updated to not call
     * this one-arg variant, then the two-arg variant will need to be overridden accordingly
     * to include the preserve-notes feature.
     */
    @Override
    public void refreshDocument(final String documentId) {
        Validate.isTrue(StringUtils.isNotBlank(documentId), "documentId must be supplied");
        
        final List<ActionItemNoteDetailDto> actionNotes = getDocumentMaintenanceService()
                .getActionNotesToBeRequeuedForDocument(documentId);
        super.refreshDocument(documentId);
        getAsynchronousDocumentMaintenanceService().restoreActionNotesForRequeuedDocument(documentId, actionNotes);
    }

    /*
     * We lazy-load DocumentMaintenanceService to prevent circular references between that service and this one.
     * Once we're ready to fully remove Document Requeuer Job, we could switch this to Spring injection instead.
     */
    private DocumentMaintenanceService getDocumentMaintenanceService() {
        return SpringContext.getBean(DocumentMaintenanceService.class);
    }

    /*
     * To prevent the potential blocking of the current transaction when restoring the action item notes,
     * that step is being performed asynchronously so that it won't be invoked until the document-requeuing
     * transaction gets committed. (It may be related to the workflow engine's DB lock on the document.)
     */
    private DocumentMaintenanceService getAsynchronousDocumentMaintenanceService() {
        return KsbApiServiceLocator.getMessageHelper().getServiceAsynchronously(DOCUMENT_MAINTENANCE_SERVICE);
    }

    /*
     * NOTE: This method is only here to support the legacy CU-specific Document Requeuer Job.
     * Once we are ready to fully remove that batch job, this helper method should be removed.
     */
    @Override
    public void refreshDocumentWithoutRestoringActionNotes(final String documentId) {
        super.refreshDocument(documentId);
    }

}
