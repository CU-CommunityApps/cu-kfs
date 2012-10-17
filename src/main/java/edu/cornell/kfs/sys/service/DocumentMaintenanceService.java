/**
 * 
 */
package edu.cornell.kfs.sys.service;

import java.util.List;

/**
 * @author dwf5
 *
 */
public interface DocumentMaintenanceService {

	/**
	 * 
	 * @return
	 */
	public boolean requeueDocuments();
	
	/**
	 * 
	 * @return
	 */
	public boolean reindexDocuments();
	
}