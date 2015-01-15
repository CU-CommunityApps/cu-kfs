package edu.cornell.kfs.sys.batch.service;

import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.rice.kim.api.identity.Person;


public interface CreateDoneBatchFileAuthorizationService {
	
	public boolean canCreateDoneFile(BatchFile batchFile, Person user);

}
