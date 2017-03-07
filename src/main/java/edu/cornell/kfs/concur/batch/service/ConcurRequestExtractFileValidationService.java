package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;

public interface ConcurRequestExtractFileValidationService {

    boolean requestExtractHeaderRowValidatesToFileContents(List<ConcurRequestExtractFile> requestExtractFiles);

}
