/**
 * 
 */
package edu.cornell.kfs.sys.dataaccess;

/**
 * @author Admin-dwf5
 *
 */
public interface AutoCancelBatchDao {

	public boolean cancelFYIsAndAcknowledgements() throws Exception;
	
	public void cancelDocuments() throws Exception;
	
}
