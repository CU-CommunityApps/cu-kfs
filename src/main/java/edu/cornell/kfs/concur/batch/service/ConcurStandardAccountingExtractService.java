package edu.cornell.kfs.concur.batch.service;

import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractService {

    ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFile(String standardAccountingExtractFileName) throws ValidationException;
    
    List<String> buildListOfFileNamesToBeProcessed();

    boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

    boolean extractCollectorFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

}
