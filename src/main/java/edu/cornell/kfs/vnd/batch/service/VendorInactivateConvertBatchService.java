package edu.cornell.kfs.vnd.batch.service;

public interface VendorInactivateConvertBatchService {

	/**
     * Validates and parses all files ready to go in the batch staging area.
     * Then process each vendor record accordingly.
     * @return
     */
    public boolean processVendorUpdates();
	
}
