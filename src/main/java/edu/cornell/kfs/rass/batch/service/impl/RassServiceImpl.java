package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public class RassServiceImpl implements RassService {

	private String rassFilePath;

	@Override
	public List<RassXmlDocumentWrapper> readXML() {
	    List<RassXmlDocumentWrapper> wrappers = new ArrayList<RassXmlDocumentWrapper>();
	    return wrappers;
	}

	@Override
	public boolean updateKFS(List<RassXmlDocumentWrapper> rassXmlDocumentWrappers) {
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
