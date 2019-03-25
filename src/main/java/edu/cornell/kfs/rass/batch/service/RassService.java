package edu.cornell.kfs.rass.batch.service;

public interface RassService {
	
	void readXML();
	
	boolean updateKFS();
	
	boolean updateAgencies();
	
	boolean updateProposals();
	
	boolean updateAwards();

}
