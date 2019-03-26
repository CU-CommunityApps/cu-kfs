package edu.cornell.kfs.rass.batch.service.impl;

import edu.cornell.kfs.rass.batch.service.RassService;

public class RassServiceImpl implements RassService {

	private String rassFilePath;

	@Override
	public void readXML() {

	}

	@Override
	public boolean updateKFS() {
		boolean successfullyUpdated = updateAgencies();
		successfullyUpdated &= updateProposals();
		successfullyUpdated &= updateAwards();
		return successfullyUpdated;
	}

	protected boolean updateAgencies() {
		return false;
	}

	protected boolean updateProposals() {
		return false;
	}

	protected boolean updateAwards() {
		return false;
	}

	public void setRassFilePath(String rassFilePath) {
		this.rassFilePath = rassFilePath;
	}

}
