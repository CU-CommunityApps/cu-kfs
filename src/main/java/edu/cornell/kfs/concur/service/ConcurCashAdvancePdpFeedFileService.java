package edu.cornell.kfs.concur.service;

import org.kuali.kfs.sys.batch.InitiateDirectory;

public interface ConcurCashAdvancePdpFeedFileService extends InitiateDirectory {

	void createDoneFileFor(String concurCashAdvancePdpFeedFileName);

}
