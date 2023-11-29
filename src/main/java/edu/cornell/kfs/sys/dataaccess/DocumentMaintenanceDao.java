package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

public interface DocumentMaintenanceDao {

	/**
	 * Finds the action list notes for a specific document that will be re-queued.
	 */
	List<ActionItemNoteDetailDto> getActionNotesToBeRequeuedForDocument(String documentId);

}
