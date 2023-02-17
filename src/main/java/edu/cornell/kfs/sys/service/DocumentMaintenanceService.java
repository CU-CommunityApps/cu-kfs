package edu.cornell.kfs.sys.service;

import java.util.List;

import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;

public interface DocumentMaintenanceService {

	boolean requeueDocuments();

	List<ActionItemNoteDetailDto> getActionNotesToBeRequeuedForDocument(String documentId);

	void restoreActionNotesForRequeuedDocument(String documentId, List<ActionItemNoteDetailDto> actionNotes);

}