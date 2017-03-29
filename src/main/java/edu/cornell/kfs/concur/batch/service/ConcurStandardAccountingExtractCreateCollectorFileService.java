package edu.cornell.kfs.concur.batch.service;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractCreateCollectorFileService {

    boolean buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents);

}
