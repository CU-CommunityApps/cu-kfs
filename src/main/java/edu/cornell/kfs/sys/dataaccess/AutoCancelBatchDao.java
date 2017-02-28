package edu.cornell.kfs.sys.dataaccess;

public interface AutoCancelBatchDao {

	/**
	 * Cancel FYI and Acknowledgement Documents that are eligible to be canceled.
	 *
	 * @return true if successful, false otherwise
	 * @throws Exception
	 */
	boolean cancelFYIsAndAcknowledgements() throws Exception;

	/**
	 * Super User Cancel Documents
	 * Use parameters to retrieve aging period and doc types to be canceled
	 *
	 * @throws Exception
	 */
	void cancelDocuments() throws Exception;
	
}
