/**
 * 
 */
package edu.cornell.kfs.sys.dataaccess;

import java.util.List;

/**
 * @author Admin-dwf5
 *
 */
public interface DocumentMaintenanceDao {

	/**
	 * 
	 * @return
	 */
	public List<String> getDocumentRequeueValues();

	/**
	 * 
	 * @return
	 */
	public List<String> getDocumentReindexValues();
	
}
