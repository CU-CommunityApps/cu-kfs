package edu.cornell.kfs.kew.api.document;

import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;

/**
 * This interface is only needed to support the legacy CU-specific Document Requeuer Job.
 * Once we are ready to remove that batch job, this interface should be removed.
 */
public interface CuDocumentRefreshQueue extends DocumentRefreshQueue {

    void refreshDocumentWithoutRestoringActionNotes(String documentId);

}
