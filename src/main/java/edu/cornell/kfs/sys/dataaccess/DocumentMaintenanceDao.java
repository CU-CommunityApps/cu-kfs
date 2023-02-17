package edu.cornell.kfs.sys.dataaccess;

import java.util.Collection;
import java.util.List;

public interface DocumentMaintenanceDao {

	/**
	 * Obtains and returns a list of documentIds for documents to requeue.
	 *
	 * @return list of documentIds
	 */
	Collection<String> getDocumentRequeueValues();
	
	/**
	 * Finds the actions list notes for the documents that will be re-queued.
	 * @return
	 */
	List<ActionItemNoteDetailDto> getActionNotesToBeRequeued();

	/**
	 * Finds the action list notes for a specific document that will be re-queued.
	 */
	List<ActionItemNoteDetailDto> getActionNotesToBeRequeuedForDocument(String documentId);

}
