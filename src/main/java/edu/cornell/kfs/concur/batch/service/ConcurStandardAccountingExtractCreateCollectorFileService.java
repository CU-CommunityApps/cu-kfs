package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractCreateCollectorFileService {

    String buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents);

}
