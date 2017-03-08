package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractService {

    ConcurStandardAccountingExtractFile parseStandardAccountingExtractFile(String standardAccountingExtractFileName) throws ValidationException;
    
    List<String> buildListOfFileNamesToBeProcessed();

    // TODO: This needs to be refactored by KFSPTS-8040 to implement proper reporting.
    boolean extractPdpFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

    // TODO: This needs to be refactored by KFSPTS-8040 and/or KFSPTS-7912 to implement proper reporting.
    boolean extractCollectorFileFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

}
