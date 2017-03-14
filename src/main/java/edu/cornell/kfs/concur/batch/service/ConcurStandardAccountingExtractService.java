package edu.cornell.kfs.concur.batch.service;

import java.io.IOException;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;

public interface ConcurStandardAccountingExtractService {

    ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFile(String standardAccountingExtractFileName) throws ValidationException;
    
    List<String> buildListOfFileNamesToBeProcessed();

    String extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);

    boolean extractCollectorFeedFromStandardAccountingExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile);
    
    void createDoneFileForPdpFile(String pdpFileName) throws IOException;

}
