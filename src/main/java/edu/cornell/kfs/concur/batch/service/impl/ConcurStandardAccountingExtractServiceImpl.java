package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.dto.ConcurStandardAccountingExtractDTO;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {

	private String reimbursementFeedDirectory;

	public List<ConcurStandardAccountingExtractDTO> parseStandardAccoutingExtractFile(File standardAccountingExtractFile) throws ValidationException {
		List<ConcurStandardAccountingExtractDTO> dtos = new ArrayList<ConcurStandardAccountingExtractDTO>();
		return dtos;
	}

	public boolean proccessConcurStandardAccountExtractDTOs(List<ConcurStandardAccountingExtractDTO> concurStandardAccountingExtractDTOs) {
		return true;
	}

	public String getReimbursementFeedDirectory() {
		return reimbursementFeedDirectory;
	}

	public void setReimbursementFeedDirectory(String reimbursementFeedDirectory) {
		this.reimbursementFeedDirectory = reimbursementFeedDirectory;
	}

}
