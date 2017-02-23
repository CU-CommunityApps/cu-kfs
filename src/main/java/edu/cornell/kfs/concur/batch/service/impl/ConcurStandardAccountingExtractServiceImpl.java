package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {

	private String reimbursementFeedDirectory;
	
	@Override
	public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(File standardAccountingExtractFile) throws ValidationException {
		return new ConcurStandardAccountingExtractFile();
	}
	
	@Override
	public boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
		return true;
	}

	public String getReimbursementFeedDirectory() {
		return reimbursementFeedDirectory;
	}

	public void setReimbursementFeedDirectory(String reimbursementFeedDirectory) {
		this.reimbursementFeedDirectory = reimbursementFeedDirectory;
	}

}
