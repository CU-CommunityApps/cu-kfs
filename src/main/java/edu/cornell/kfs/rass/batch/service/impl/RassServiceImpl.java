package edu.cornell.kfs.rass.batch.service.impl;

import edu.cornell.kfs.rass.batch.service.RassService;

public class RassServiceImpl implements RassService {

	@Override
	public void readXML() {

	}
	
	@Override
	public boolean updateKFS() {
		updateAgencies();
		updateProposals();
		updateAwards();
		return false;
	}

	@Override
	public boolean updateAgencies() {
		return false;
	}

	@Override
	public boolean updateProposals() {
		return false;
	}

	@Override
	public boolean updateAwards() {
		return false;
	}

}
