package edu.cornell.kfs.concur.batch.service;

import java.io.File;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.dto.ConcurStandardAccountingExtractDTO;

public interface ConcurStandardAccountingExtractService {

	List<ConcurStandardAccountingExtractDTO> parseStandardAccoutingExtractFile(File standardAccountingExtractFile) throws ValidationException;

	boolean proccessConcurStandardAccountExtractDTOs(List<ConcurStandardAccountingExtractDTO> concurStandardAccountingExtractDTOs);

}
