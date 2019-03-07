package edu.cornell.kfs.sys.dataaccess;

import java.util.Collection;

public interface DocumentMaintenanceDao {

	/**
	 * Obtains and returns a list of documentIds for documents to requeue.
	 *
	 * @return list of documentIds
	 */
	Collection<String> getDocumentRequeueValues();

}
