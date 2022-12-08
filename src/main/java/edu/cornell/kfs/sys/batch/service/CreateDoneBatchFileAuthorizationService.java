package edu.cornell.kfs.sys.batch.service;

import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.kim.impl.identity.Person;


public interface CreateDoneBatchFileAuthorizationService {
	
	public boolean canCreateDoneFile(BatchFile batchFile, Person user);

}
