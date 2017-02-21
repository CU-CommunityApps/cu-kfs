package edu.cornell.kfs.concur.service;

import org.kuali.kfs.sys.batch.InitiateDirectory;

/**
 * Handles file level processing tasks related to the 
 * writing of the outgoing PDP feed files that create KFS Cash Advances.
 */
public interface ConcurCashAdvancePdpFeedFileService extends InitiateDirectory {
	
	public void createDoneFileFor(String concurCashAdvancePdpFeedFileName);

}
